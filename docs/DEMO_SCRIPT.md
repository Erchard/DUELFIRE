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

## One-Phone Fallback Demo

If Firebase, internet, or the second phone fails:

- open app;
- tap `Demo Mode`;
- fire at fake enemy;
- wait for fake enemy return shots;
- show VICTORY / DEFEAT.

Say:

```text
Demo Mode is the offline fallback for presentations. The two-phone Firebase mode is the real MVP path.
```

## Things Not To Promise Yet

Do not claim these are already implemented:

- camera aiming;
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
