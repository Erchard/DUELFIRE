# Camera Aim Assist Specification

## Purpose

Camera Aim Assist is the wow layer for Duel Fire. It makes the phone feel like a duel device rather than a button-only app.

The camera version should not be implemented before Version 0.1 is buildable and tested on real phones.

## Product Definition

Camera Aim Assist means:

- the camera preview fills the Battle Screen;
- a crosshair is drawn at the center;
- the app detects a person in the camera frame;
- the app shows target status;
- FIRE is more effective when the crosshair is closer to the detected body center.

This should not be described as "contour recognition" in the MVP. The practical implementation is pose-based target detection.

## Recommended Stack

```text
Camera: CameraX
Human detection: ML Kit Pose Detection
Overlay: Compose / Android View overlay
Future marker: QR or visual badge
```

References:

- [ML Kit Pose Detection for Android](https://developers.google.com/ml-kit/vision/pose-detection/android)
- [CameraX ML Kit Analyzer](https://developer.android.com/media/camera/camerax/mlkitanalyzer)

## Why Pose Detection

OpenCV contour detection finds shapes, not people. It can fail when:

- background is complex;
- clothes blend into the scene;
- lighting changes;
- shadows are strong;
- several people appear;
- the target is far away.

ML Kit Pose Detection gives body landmarks, which are more useful for gameplay:

- shoulders;
- elbows;
- hips;
- knees;
- approximate torso center;
- rough body bounding box.

## Version 0.2: Camera Screen

Goal: show the camera during battle without changing damage rules.

Requirements:

- request `CAMERA` permission;
- open full-screen CameraX preview;
- show HP overlay at the top;
- show crosshair at center;
- show FIRE button at bottom;
- keep fixed 25 damage;
- fallback to current Battle Screen if camera is unavailable.

No ML required in 0.2.

## Version 0.3: Target Detection

Goal: detect a human target in the frame.

Requirements:

- add ML Kit Pose Detection;
- use streaming mode;
- detect one prominent person;
- draw rough body box or body points;
- show `TARGET DETECTED` when pose confidence is sufficient;
- show `NO TARGET` otherwise.

Open question:

- Should FIRE be blocked when no target is detected, or should it become `MISS`?

Recommendation:

- for investor demo, show `MISS` instead of blocking; it feels more game-like.

## Version 0.4: Accuracy Damage

Goal: convert target detection into gameplay.

Inputs:

```text
Aim point = center of camera preview
Pose landmarks = ML Kit body points
Body box = rectangle around confident landmarks
Torso center = midpoint between shoulders and hips
```

Suggested calculation:

```text
distance = distance(aimPoint, torsoCenter)
maxDistance = bodyBox diagonal / 2
accuracy = 1 - clamp(distance / maxDistance, 0, 1)
```

Damage:

```text
accuracy >= 0.80 -> 35 damage
accuracy >= 0.50 -> 25 damage
accuracy >= 0.25 -> 10 damage
otherwise -> 0 damage / MISS
```

UI:

```text
TARGET LOCKED
ACCURACY: 82%
DAMAGE: 25
```

Firebase change:

```kotlin
suspend fun fire(code: String, myPlayerId: String, damage: Int): Result<Unit>
```

## Version 0.5: Enemy Confirmation

Problem:

Pose detection sees a person, not necessarily the actual opponent.

Demo-safe options:

1. Treat the only visible person as enemy.
2. Add a QR marker or visual badge on the opponent.
3. Later combine camera detection with Bluetooth/GPS proximity.

Recommendation:

- use option 1 for early investor demo;
- use option 2 for reliable staged demo;
- keep option 3 for real product direction.

## Camera Battle UI Draft

```text
------------------------------------------------
YOUR HP 100                         ENEMY HP 75
------------------------------------------------

                 [ body box ]
                     +
                  crosshair

Status: TARGET LOCKED | ACCURACY 82%

                    [ FIRE ]
------------------------------------------------
```

## Risks

- Camera permissions can break demo flow.
- Low light can reduce detection quality.
- Multiple people can confuse target selection.
- ML Kit model adds app size.
- First-frame model warmup can create latency.
- Pose landmarks may not match overlay if coordinate transforms are wrong.

## Mitigations

- Keep non-camera Battle Screen as fallback.
- Add Demo Mode.
- Use one visible target for investor demo.
- Use clear lighting.
- Use CameraX ML Kit Analyzer for coordinate transforms.
- Add QR/marker confirmation before public demos.
