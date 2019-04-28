#include "toneanalyzer.h"

#include <unordered_map>
#include <mutex>
#include <vector>
#include <array>

#include <cmath>

namespace {

    constexpr auto NumHalftones = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_NumHalftones;
    constexpr auto HalftoneBase = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_HalftoneBase;
    constexpr auto BaseToneFreq = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_BaseToneFreq;

    const float FM_PI = (float)M_PI;

    struct CosWnk {
        float cos;
        float wnk;
    };

    typedef std::array<CosWnk, NumHalftones> table;

    auto generateTones() {
        // std::pow is not constexpr so we can't generate the sequence as constexpr
        std::array<float, NumHalftones> res{0};
        for (int tone = 0; NumHalftones != tone; ++tone)
            res[tone] = (float) (BaseToneFreq * std::pow(HalftoneBase, tone));
        return res;
    }

    const auto sTones = generateTones();

    auto generateTable(int sampleRate) {
        table res;
        for (int tone = 0; NumHalftones != tone; ++tone) {
            auto omega = 2 * FM_PI * sTones[tone] / sampleRate;
            res[tone].cos = std::cos( omega) * 2;
            res[tone].wnk = std::exp(-omega);
        }
        return res;
    }

    const table& get_frequency_table(int sampleRate) {
        // we hide data inside the function since static init check is very cheap
        static std::unordered_map<int, table> data;
        static std::mutex mutex;

        // protection in case of multithreading
        // (not gonna happen for now, Android can't record 2 sources at once)
        std::lock_guard<std::mutex> lock(mutex);
        auto it = data.find(sampleRate);
        if(data.end() != it)
            return it->second;

        return data.insert(std::make_pair(sampleRate, generateTable(sampleRate))).first->second;
    }

    float goertzel(const short* x, int size, const CosWnk& cw) {
        float skn0 = 0;
        float skn1 = 0;
        float skn2 = 0;

        for (int i = 0; size != i; ++i) {
            skn2 = skn1;
            skn1 = skn0;
            skn0 = cw.cos * skn1 - skn2 + x[i] / 16356.0f;
        }
        return std::abs(skn0 - cw.wnk * skn1);
    }

} // namespace

JNIEXPORT jint JNICALL
Java_com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_bestMatchTone(
        JNIEnv* env, jclass cls, jbyteArray data, jint size, jint sampleRate) {

    const auto& table = get_frequency_table(sampleRate);

    int bestTone = -1;
    float maxPower = std::numeric_limits<float>::min();

    auto b = env->GetByteArrayElements(data, nullptr);

    for (int tone = 0; NumHalftones != tone; ++tone) {
        // I can cast byte* to short* since android audio record buffer is native byte order
        float power = goertzel((const short*)b, size / 2, table[tone]);
        if (power < maxPower)
            continue;
        maxPower = power;
        bestTone = tone;
    }

    env->ReleaseByteArrayElements(data, b, JNI_ABORT);

    return bestTone;
}
