# Investor Demo Script

## Goal

Show that Duel Fire can become a real-world phone duel product, while proving the technical core with a simple stable MVP.

## Setup

Needed:

- two Android phones;
- same APK installed on both phones;
- internet on both phones;
- Firebase Realtime Database configured;
- temporary demo rules enabled;
- optional third phone or laptop to show Firebase data changing.

Fallback:

- one Android phone with Demo Mode.
- one Android phone with Solo AR Demo once implemented.

## Two-Phone Demo

### 1. Open

Say:

```text
This is Duel Fire. The first MVP proves the real-time duel loop between two phones.
```

### 2. Create Duel

On phone A:

- enter player name;
- tap `Create Duel`;
- show the 4 digit code.

Say:

```text
The first player creates a duel. The app writes a duel session to Firebase and gives a short code.
```

### 3. Join Duel

On phone B:

- enter player name;
- enter code;
- tap `Join Duel`.

Say:

```text
The second player joins by code. No login is needed for the MVP.
```

### 4. Start Battle

On phone A:

- tap `Start Battle`.

Say:

```text
Both phones are now looking at the same duel state in real time.
```

### 5. Exchange Fire

- tap FIRE on phone A;
- show HP drop on phone B;
- tap FIRE on phone B;
- show HP drop on phone A.

Say:

```text
FIRE is synchronized through Firebase. Damage, cooldown, and HP are shared between both devices.
```

### 6. Finish

- hit one player 4 times;
- show VICTORY on one phone;
- show DEFEAT on the other.

Say:

```text
When HP reaches zero, the duel is finished and both phones resolve the result automatically.
```

## Wow-Future Pitch

After showing the working duel loop, explain:

```text
The next layer is Camera Aim Assist. The battle screen becomes a camera view. The phone detects a person in frame, draws a target overlay, and calculates damage based on how close the crosshair is to the body center.
```

Then show the roadmap:

```text
0.1 Firebase duel
0.2 Camera screen
0.3 Pose detection
0.4 Accuracy-based damage
0.5 QR/marker confirmation
```

## One-Phone Demo (target experience)

What the user should see on a **single device** (once Solo AR is implemented):

- **Camera** fills the view with the real surroundings;
- **AR** places a **virtual opponent** in the scene;
- the user **aims** with the phone and **fires** at the opponent;
- the opponent **returns fire**; HP, cooldown, and **VICTORY / DEFEAT** behave like the online duel, locally.

Details: [Solo AR Demo specification](SOLO_AR_DEMO_SPEC.md).

### Interim: Demo Mode (current build)

If Firebase, internet, the second phone, or AR is unavailable:

- open app;
- tap `Demo Mode`;
- use the 2D battle screen: fire at the fake enemy, wait for return shots, show VICTORY / DEFEAT.

Say:

```text
Demo Mode is the placeholder one-phone flow until Solo AR ships. The two-phone Firebase path is the core MVP sync demo.
```

## One-Phone Wow Fallback: Solo AR Demo

Once implemented, use this when only one phone is available but the presentation needs stronger visual impact:

- open app;
- tap `Solo AR Demo`;
- scan a table or floor;
- place virtual enemy;
- aim with center crosshair;
- tap FIRE;
- show hit / miss, enemy HP, return fire, and VICTORY / DEFEAT.

Say:

```text
This is the one-device AR presentation mode. It shows the future feel of Duel Fire even when we do not have a second phone or live Firebase setup.
```

## Things Not To Promise Yet

Do not claim these are already implemented:

- camera aiming;
- Solo AR Demo;
- person detection;
- GPS validation;
- Bluetooth proximity;
- anti-cheat;
- real identity recognition;
- production security.

## Strongest Positioning

```text
Duel Fire starts as a simple synchronized duel and grows into a camera-assisted real-world battle experience.
```
