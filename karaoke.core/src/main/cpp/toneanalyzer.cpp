#include "toneanalyzer.h"

#include <unordered_map>
#include <mutex>
#include <array>

#include <cmath>

namespace {

    constexpr auto NumHalftones = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_NumHalftones;
    constexpr auto HalftoneBase = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_HalftoneBase;
    constexpr auto BaseToneFreq = com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_BaseToneFreq;

    // Normalization is not really needed for goertzel, but it seems to give better results.
    // Android audio recording seems to keep the values under 16356
    const float norm = 1.0f / 16356.0f;

    struct CosWnk {
        float cos;
        float wnk;
    };

    typedef std::array<CosWnk, NumHalftones> table;

    auto generateTable(int sampleRate) {
        table res;
        for (int tone = 0; NumHalftones != tone; ++tone) {
            auto freq = BaseToneFreq * std::pow(HalftoneBase, tone);
            auto omega = float(2 * M_PI * freq / sampleRate);
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
            skn0 = cw.cos * skn1 - skn2 + x[i] * norm;
        }
        return std::abs(skn0 - cw.wnk * skn1);
    }

} // namespace

JNIEXPORT jint JNICALL
Java_com_damn_karaoke_core_controller_processing_GoertzelToneDetectorJNI_bestMatchTone(
        JNIEnv* env, jclass cls, jshortArray data, jint size, jint sampleRate) {

    const auto& table = get_frequency_table(sampleRate);

    auto b = env->GetShortArrayElements(data, nullptr);

    jint bestTone = 0;
    float maxPower = goertzel(b, size, table[bestTone]);

    for (auto tone = 1; NumHalftones != tone; ++tone) {
        float power = goertzel(b, size, table[tone]);
        if (power < maxPower)
            continue;
        maxPower = power;
        bestTone = tone;
    }

    env->ReleaseShortArrayElements(data, b, JNI_ABORT);

    return bestTone;
}
