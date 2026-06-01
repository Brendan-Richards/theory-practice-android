# Guitar Theory Trainer

A native Android version of the original Python/Flask music theory quiz app.

The Android app keeps the original quiz modes and config files:

- Intervals
- Guitar Triads
- Chord Spelling

The TOML configs are bundled in `app/src/main/assets/configs`, and the quiz logic has been ported to Java under `app/src/main/java/com/example/theorypractice`.

## Run

Open this repository in Android Studio and run the `app` configuration on an emulator or device.

From a machine with Java, Gradle, and the Android SDK installed, you can also build with:

```sh
gradle assembleDebug
```

The previous Flask implementation is still present under `src/theory_practice` for reference.
