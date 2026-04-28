# Duel Fire Project Brief

## One Sentence

Duel Fire is a native Android MVP where two phones connect to the same duel, exchange shots through Firebase, and evolve toward a camera-assisted aiming experience.

## Investor Demo Goal

The immediate goal is a stable proof of concept that can be installed on two Android phones:

- one player creates a duel;
- the other joins by a short code;
- both see synchronized HP;
- pressing FIRE damages the opponent;
- the duel ends with VICTORY and DEFEAT.

This is the foundation. The wow layer is Camera Aim Assist.

If only one device is available, the product should present **Solo AR Demo**: the user sees the environment **through the camera**, **AR** adds a **virtual opponent** in the world, the user **shoots** at that opponent, and the opponent **shoots back**—same duel loop (HP, cooldown, VICTORY/DEFEAT) simulated locally. Until that is built, **Demo Mode** is the interim one-phone fallback (no camera/AR).

## Current Product Scope

Version 0.1 should stay narrow:

- Kotlin native Android app;
- Jetpack Compose UI;
- Firebase Realtime Database sync;
- two players only;
- no login;
- no payments;
- no GPS;
- no Bluetooth;
- no camera requirement;
- Demo Mode for one-phone fallback.

## Camera Aim Assist Concept

Camera Aim Assist means the phone camera becomes the aiming surface.

The battle screen becomes:

```text
Camera preview on full screen

Top:
YOUR HP: 100
ENEMY HP: 75

Center:
+ crosshair
target box / body overlay

Bottom:
[ FIRE ]

Status:
TARGET LOCKED / NO TARGET / HIT 82% / MISS
```

The MVP camera logic should not try to solve full identity recognition. For investor demo purposes:

- if one person is visible in frame, treat that person as the enemy;
- later add a QR/marker badge to confirm the target;
- later combine camera detection with Bluetooth or GPS proximity.

## Why Not OpenCV Contours First

OpenCV contour detection can find shapes and edges, but it does not understand that a shape is a person. Real-world people appear against changing backgrounds, lighting, clothes, shadows, and distance. That makes contour-only targeting fragile for a live demo.

The better first camera implementation is:

```text
CameraX preview
ML Kit Pose Detection
33 body landmarks
rough body box
crosshair distance to torso center
accuracy-based damage
```

## Accuracy Model

For Version 0.4:

```text
Aim point = center of screen
Body box = rectangle around detected pose landmarks
Torso center = midpoint of shoulders and hips
Accuracy = distance from aim point to torso center
```

Suggested damage:

```text
Accuracy >= 80% -> 35 damage
Accuracy >= 50% -> 25 damage
Accuracy >= 25% -> 10 damage
Otherwise -> miss
```

The final Firebase `fire` action can later accept a computed damage value instead of a fixed 25 damage, but Version 0.1 should keep fixed damage for stability.

## Milestones

### 0.1 Firebase Duel

The current app target:

- Create Duel
- Join Duel
- Waiting Screen
- Start Battle
- Battle Screen
- Fire
- Cooldown
- Victory / Defeat
- Reset / Exit
- Demo Mode

### 0.2 Camera Screen

Add camera without ML:

- request camera permission;
- show CameraX preview;
- draw crosshair overlay;
- keep current Firebase damage logic.

### 0.2A Solo AR Demo

One-device investor fallback:

- ARCore availability check;
- camera permission;
- place virtual enemy in AR;
- crosshair overlay;
- local hit / miss logic;
- enemy return fire;
- VICTORY / DEFEAT without Firebase.

### 0.3 Pose Detection

Add ML target awareness:

- ML Kit Pose Detection;
- target detected status;
- body box overlay;
- no accuracy damage yet.

### 0.4 Accuracy Damage

Turn detection into gameplay:

- compute aim accuracy;
- show accuracy percentage;
- use variable damage;
- send damage through Firebase.

### 0.5 Enemy Confirmation

Make demo targeting reliable:

- QR or marker badge on opponent;
- optional Bluetooth/GPS proximity confirmation later.

## External References

- [ML Kit Pose Detection for Android](https://developers.google.com/ml-kit/vision/pose-detection/android)
- [CameraX ML Kit Analyzer](https://developer.android.com/media/camera/camerax/mlkitanalyzer)
