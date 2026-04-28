# Duel Fire

Duel Fire is a native Android proof of concept for a real-world phone duel. The first MVP lets two Android phones create and join a duel through Firebase, exchange FIRE actions, synchronize HP in real time, and end with VICTORY / DEFEAT.

The next product layer is **Camera Aim Assist**: the battle screen becomes a camera view with a crosshair, human target detection, accuracy, and variable damage.

For one-device demos, the **target** experience is **Solo AR Demo**: the user sees the real world through the camera, an AR layer draws a **virtual opponent** in the scene, the player **fires** at that opponent, and the opponent **fires back** (local HP / cooldown / win-loss loop). Until that ships, **Demo Mode** is a simplified 2D placeholder battle.

## Project Status

Current target: **Version 0.1 Firebase Duel MVP**.

Implemented in the codebase:

- Kotlin Android app.
- Jetpack Compose UI.
- Firebase Realtime Database repository.
- Lightweight Android build scripts.
- Verified debug APK build.
- Verified install and launch on Redmi Note 7.
- Start / Waiting / Battle screens.
- Create Duel / Join Duel / Start Battle.
- FIRE action with cooldown.
- HP synchronization model.
- Victory / Defeat state.
- Reset / Exit actions (Exit removes the current duel session from Firebase).
- One-phone Demo Mode.

Not yet implemented:

- real two-phone Firebase test;
- Solo AR Demo;
- CameraX battle screen;
- ML Kit Pose Detection;
- QR/marker target confirmation.

## Quick Links

- [Documentation index](docs/README.md)
- [Project brief](docs/PROJECT_BRIEF.md)
- [Product specification](docs/SPECIFICATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Firebase setup](docs/FIREBASE.md)
- [Build and run](docs/BUILD_AND_RUN.md)
- [Technical debt](docs/TECH_DEBT.md)
- [Test plan](docs/TEST_PLAN.md)
- [Camera Aim Assist specification](docs/CAMERA_AIM_ASSIST_SPEC.md)
- [Solo AR Demo specification](docs/SOLO_AR_DEMO_SPEC.md)
- [Investor demo script](docs/DEMO_SCRIPT.md)
- [TODO list](TODO.md)

## Tech Stack

```text
Language: Kotlin
UI: Jetpack Compose
Realtime sync: Firebase Realtime Database
Architecture: simple MVVM
Minimum Android: Android 8.0 / API 26
Package: com.mvp.duelfire
Build system: Gradle
```

## Main User Flow

1. Player 1 enters a name and taps `Create Duel`.
2. The app creates a Firebase duel and shows a 4 digit code.
3. Player 2 enters the code and taps `Join Duel`.
4. Player 1 taps `Start Battle`.
5. Both phones see their HP, enemy HP, status, and FIRE.
6. FIRE deals 25 damage to the opponent.
7. After 4 hits, one player sees VICTORY and the other sees DEFEAT.

## Build

Add a real Firebase config file:

```text
app/google-services.json
```

Then build:

```bash
gradle assembleDebug
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

More details: [Build and run](docs/BUILD_AND_RUN.md).
