# Solo AR Demo Specification

## Purpose

Solo AR Demo is the best one-device fallback for an investor presentation. It lets one Android phone show the fantasy of Duel Fire without requiring a second player, second phone, or Firebase connection.

Instead of only fighting a fake enemy in a flat UI, the app places a virtual opponent in augmented reality. The player aims the phone at that virtual opponent and taps FIRE.

This mode is not the real multiplayer game. It is a presentation mode that sells the future feel of the product.

## Core Idea

```text
Phone camera opens
User scans the floor or table
App places a virtual enemy in AR
Crosshair stays in screen center
FIRE damages the virtual enemy if crosshair is close enough
Virtual enemy fires back on a timer
Player sees VICTORY or DEFEAT
```

## Why This Is Useful

Solo AR Demo solves a real demo problem:

- only one Android phone is available;
- internet or Firebase is unavailable;
- second player is unavailable;
- investor needs to feel the future product, not only see a button prototype.

It also creates a strong visual story:

```text
This starts as a synchronized duel app.
The next step is camera aiming.
The one-phone AR demo shows where the game is going.
```

## Recommended Technical Path

Use ARCore through either:

1. **Direct ARCore SDK**
   - more control;
   - more boilerplate;
   - good for long-term ownership.

2. **SceneView for Android**
   - Compose-friendly;
   - wraps ARCore and Filament rendering;
   - faster MVP path for placing and rendering a 3D enemy.

Recommended for MVP:

```text
SceneView + ARCore + simple 3D model or primitive enemy
```

References:

- [ARCore Android quickstart](https://developers.google.com/ar/develop/java/quickstart)
- [Enable ARCore in an Android app](https://developers.google.com/ar/develop/java/enable-arcore)
- [ARCore supported devices](https://developers.google.com/ar/discover/supported-devices)
- [SceneView Android](https://github.com/SceneView/sceneview-android)

## Device Feasibility

Google's ARCore supported devices list includes:

```text
Xiaomi Redmi Note 7
```

The current test phone is:

```text
Redmi Note 7
```

So the AR path is realistic for the current demo device, assuming Google Play Services for AR is installed or can be installed.

## MVP Scope

Version `0.2-ar-solo` should include:

- `Solo AR Demo` button on Start Screen;
- camera permission request;
- ARCore availability check;
- install/update Google Play Services for AR prompt if needed;
- plane detection;
- tap-to-place or auto-place virtual enemy;
- center crosshair overlay;
- enemy HP;
- player HP;
- FIRE button;
- hit / miss calculation;
- virtual enemy return fire;
- VICTORY / DEFEAT result.

Do not include in the first AR demo:

- networking;
- Firebase sync;
- multiplayer;
- real person detection;
- ML Kit pose detection;
- GPS;
- Bluetooth;
- complex 3D animation;
- complex enemy AI.

## Screen Flow

### Start Screen

Add:

```text
[ Solo AR Demo ]
```

### AR Setup State

Show camera preview and status:

```text
Move phone slowly
Looking for surface...
```

When plane is detected:

```text
Tap to place enemy
```

Alternative for faster demo:

```text
Auto-place enemy 1.5m in front of camera after tracking is stable
```

Recommendation:

- use tap-to-place first;
- add auto-place later if demo flow feels slow.

### AR Battle State

```text
Top:
YOU 100 HP
ENEMY 100 HP

Center:
crosshair
virtual enemy in AR

Bottom:
Status: TARGET LOCKED / MISS / HIT
[ FIRE ]
```

### AR Result State

Victory:

```text
VICTORY
Virtual enemy eliminated
[Restart AR Demo]
[Exit]
```

Defeat:

```text
DEFEAT
You were eliminated
[Restart AR Demo]
[Exit]
```

## Hit Detection

For the first AR version, do not implement full projectile physics.

Use screen-space hit testing:

```text
enemyScreenCenter = projection of enemy world position into screen coordinates
aimPoint = center of screen
distance = distance(aimPoint, enemyScreenCenter)
```

Damage:

```text
distance <= 60 px  -> 35 damage
distance <= 140 px -> 25 damage
distance <= 240 px -> 10 damage
otherwise          -> MISS
```

The exact numbers should be tuned on the Redmi Note 7.

## Enemy Behavior

The enemy should be simple:

- stands in one place;
- optionally rotates toward camera;
- fires back every 3 seconds;
- return fire deals 15 or 25 damage;
- stops firing after defeat.

Optional polish:

- red flash when player is hit;
- enemy hit flash;
- floating `-25 HP`;
- small recoil animation;
- simple sound effect.

## Data Model

This mode can stay local. It does not need Firebase.

Suggested state:

```kotlin
data class SoloArState(
    val playerHp: Int = 100,
    val enemyHp: Int = 100,
    val enemyPlaced: Boolean = false,
    val status: String = "LOOKING_FOR_SURFACE",
    val lastFireAt: Long = 0L,
    val finished: Boolean = false,
    val result: String? = null
)
```

## Implementation Structure

Suggested package:

```text
ui/ar/
  SoloArScreen.kt
  ArHudOverlay.kt
  CrosshairOverlay.kt

ar/
  ArAvailabilityChecker.kt
  SoloArGameController.kt
  ArHitCalculator.kt
  EnemyNodeFactory.kt
```

Keep AR code separate from Firebase duel code.

## Risks

- ARCore may require Google Play Services for AR update.
- ARCore can fail if the room has poor texture or bad lighting.
- Plane detection can take time.
- 3D model loading can add complexity.
- SceneView dependency version may require tuning with Compose/Gradle versions.
- Redmi Note 7 is supported, but performance is still older midrange hardware.

## Mitigations

- Keep current Demo Mode as fallback.
- Add a clear "AR unavailable" message.
- Use simple primitive enemy first, not a heavy model.
- Use low-poly GLB only after the primitive demo works.
- Keep the non-AR Firebase duel untouched.
- Test under good lighting on a textured floor or table.

## Recommended Development Steps

1. Add `Solo AR Demo` entry point.
2. Add ARCore availability check.
3. Add camera permission.
4. Add SceneView AR screen.
5. Detect plane and place simple primitive enemy.
6. Draw Compose HUD over AR view.
7. Implement screen-space hit detection.
8. Add enemy return fire timer.
9. Add result state.
10. Polish visuals only after the loop works.

## Success Criteria

- App opens Solo AR Demo on one phone.
- User can place or see virtual enemy.
- FIRE can hit or miss based on aim.
- Enemy HP decreases.
- Enemy fires back.
- Player HP decreases.
- VICTORY / DEFEAT can be reached.
- If AR is unavailable, app falls back gracefully to normal Demo Mode.
