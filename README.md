
# Karaoke for Android and Google Glass

This is (yet and likely forever) minimalistic Karaoke pet project for Android and Google Glass devices. Uses <a href="http://ultrastardx.sourceforge.net/">Ultrastar Delux</a> song format.

## Building
Just clone and build in Android Studio. You most likely will be required to install Glass Development Kit add-on.
Project consist of 3 modules:
* **karaoke.core**, a shared module where all the logic and custom views are located
* **app** - version for phones/tablets
* **glass** - version Google Glass

## Running
* you'll need to obtain some songs in **Ultrastar Delux** format first. A few free ones could be downloaded <a href="https://sourceforge.net/projects/ultrastardx/files/Songs/">here</a>.
* put (unpacked) songs directories to the Music directory on the device. You can omit video files, since they are not supported anyway.
* launch the application:
  * on Google Glass, say "Show lyrics", or start the application from Home card menu.
  * on usual Android device just start the app.

On phones and tablets, usage of headphones is strongly recommended since the mic pick ups the music been playing, and messes up tone detection.

On Google glass, earbud is a good idea since it provides much better audio quality, and <a href="https://arxiv.org/abs/1404.1320">much lower battery consumption</a>.

## Notes

Application has two algorithms of pitch detection: autocorrelation, taken from Ultrastar Delux and optimized for integer math, and <a href="https://en.wikipedia.org/wiki/Goertzel_algorithm">Goertzel algorithm</a>, as described in Wikipedia. Toggle is only available from source code for now.
Autocorrelation seems to work really bad for 16000 sample rate and sine signal. 44100 is a bit better in reasonable range. Goertzel is pretty good for both rates. Not sure about real voice.
There is also a JNI version of Goertzel, used by default now. For Samsung S7, it 4-7 (debug/release builds) times faster than Java one.

Code quality is far from production: Weak error checking, using string hashes as unique ids, and so on.

## Future plans
* Since I am an awful singer, and can't match the pitch of any song to check the pitch detection, some test audio files with real performances are required. Adding a couple unit tests based on these would be nice.
* The way lyrics and pitch are displayed is different from other Karaoke software:
  * next line is not displayed until it played
  * there is no visual timing cue on long pauses
  * pitch plot width is matched to text width, which is unnecessary
* There is no scoring.
* Some songs are not loaded properly due to deviations in formating (broken files).
* A lot of UI work needed, especially on the phones.
* Since Google Glass seems to render UI in the software mode, there is a chance that using OpenGL for rendering can make things a bit faster. Or just optimize the plot/lyrics rendering by avoiding unnecessary invalidations.
