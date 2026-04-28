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

## Lightweight Local Setup

This machine uses a minimal portable toolchain instead of Android Studio:

```text
D:\AndroidTools\
  jdk17\
  android-sdk\
  gradle\
  gradle-cache\
```

Installed SDK packages:

```text
platform-tools
platforms;android-35
build-tools;35.0.0
```

Helper scripts:

```powershell
.\scripts\build-debug.ps1
.\scripts\install-debug.ps1
```

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

On this machine, prefer:

```powershell
.\scripts\build-debug.ps1
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

On this machine, prefer:

```powershell
.\scripts\install-debug.ps1
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

This repository was successfully built with the lightweight toolchain above. The debug APK was installed on a connected Redmi Note 7.
