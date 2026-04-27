# Build And Run

## Required Tools

Install:

- JDK 17;
- Android SDK;
- Android SDK Platform Tools;
- Android SDK Build Tools;
- Android Platform API 35 or compatible;
- Gradle or Gradle Wrapper.

The easiest route is to install Android Studio once and let it install the Android SDK. The project can still be edited and built from Cursor/Codex.

## Firebase File

Add:

```text
app/google-services.json
```

Without this file, Firebase online duel mode will not work. Demo Mode can still be used for UI/gameplay presentation.

## Build APK

From project root:

```bash
gradle assembleDebug
```

If Gradle Wrapper is added:

```bash
./gradlew assembleDebug
```

Expected output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install On Android Phone

On the phone:

1. Enable Developer Options.
2. Enable USB Debugging.
3. Connect the phone by USB.
4. Accept the `Allow USB debugging?` prompt.

Check connection:

```bash
adb devices
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Two Phone Demo Setup

1. Install the same APK on both phones.
2. Ensure both phones have internet.
3. Open Duel Fire on phone A.
4. Tap `Create Duel`.
5. Open Duel Fire on phone B.
6. Enter the code from phone A.
7. Tap `Join Duel`.
8. Tap `Start Battle` on phone A.
9. Use FIRE on both phones.

## Known Local Environment Issue

At the time this documentation was written, this machine did not expose `java`, `gradle`, or `adb` in the current execution environment. The code exists, but the APK still needs a real Android build environment to verify compilation.
