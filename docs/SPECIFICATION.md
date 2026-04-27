# Product Specification

## Purpose

Duel Fire is an investor MVP for a two-player phone duel. It should demonstrate that two Android devices can join the same duel, exchange shots in real time, synchronize HP, and resolve the duel with clear win/loss feedback.

The MVP must stay simple. The first version is about proving the core duel loop, not building the final game.

## Non Goals For Version 0.1

- No login.
- No registration.
- No GPS.
- No Bluetooth.
- No camera.
- No AR.
- No payments.
- No admin panel.
- No anti-cheat.
- No tournaments.
- No production Firebase security model.

## Platforms

```text
Platform: Android
Minimum Android: 8.0 / API 26
Language: Kotlin
UI: Jetpack Compose
Realtime sync: Firebase Realtime Database
Package name: com.mvp.duelfire
```

## Screens

### Start Screen

Elements:

- title: `DUEL FIRE`;
- player name input;
- `Create Duel` button;
- duel code input;
- `Join Duel` button;
- `Demo Mode` button.

Rules:

- empty player name falls back to `Player 1` or `Player 2`;
- create generates a 4 digit code;
- join accepts a 4 digit code;
- failed create/join shows an error.

### Waiting Screen

For host:

```text
Duel Code: 4821
Waiting for opponent...
[Start Battle]
[Exit]
```

For guest:

```text
Connected to duel
Waiting for host to start...
[Exit]
```

Rules:

- only player1 can start;
- start is enabled only when player2 exists;
- after start, both devices move to Battle Screen.

### Battle Screen

Elements:

- title;
- own HP bar;
- enemy HP bar;
- status text;
- large FIRE button;
- Reset / Play Again button;
- Exit button;
- hit flash and damage text.

Statuses:

```text
READY
COOLDOWN
HIT
YOU WERE HIT
VICTORY
DEFEAT
WAITING
ERROR
```

### Result State

Result can be shown inside Battle Screen.

Victory:

```text
VICTORY
Enemy eliminated
[Play Again]
[Exit]
```

Defeat:

```text
DEFEAT
You were eliminated
[Play Again]
[Exit]
```

## Game Rules

Constants:

```kotlin
INITIAL_HP = 100
DAMAGE_PER_SHOT = 25
FIRE_COOLDOWN_MS = 2000L
```

Rules:

1. Each player starts with 100 HP.
2. FIRE damages the enemy by 25 HP.
3. FIRE can be used once every 2 seconds per player.
4. HP cannot go below 0.
5. If enemy HP reaches 0, duel status becomes `finished`.
6. Winner sees `VICTORY`.
7. Loser sees `DEFEAT`.
8. FIRE does nothing unless duel status is `active`.
9. Dead players cannot fire.
10. Finished duel disables FIRE.

## Firebase Data Model

Canonical location:

```text
/duels/{duelCode}
```

Example:

```json
{
  "duelCode": "4821",
  "status": "waiting",
  "createdAt": 1710000000000,
  "updatedAt": 1710000000000,
  "winnerPlayerId": null,
  "players": {
    "player1": {
      "id": "player1",
      "name": "Arsen",
      "hp": 100,
      "alive": true,
      "connected": true,
      "lastFireAt": 0
    },
    "player2": {
      "id": "player2",
      "name": "Opponent",
      "hp": 100,
      "alive": true,
      "connected": true,
      "lastFireAt": 0
    }
  },
  "lastEvent": {
    "type": "none",
    "byPlayerId": null,
    "targetPlayerId": null,
    "damage": 0,
    "timestamp": 0
  }
}
```

Statuses:

```text
waiting
active
finished
cancelled
```

Event types:

```text
none
hit
victory
reset
```

## Acceptance Criteria

- APK installs on two Android phones.
- Player 1 can create a duel.
- Player 1 sees a duel code.
- Player 2 can join with that code.
- Host can start battle after Player 2 joins.
- Both devices show own HP and enemy HP.
- FIRE on one phone reduces HP on the other.
- HP updates in real time.
- Cooldown prevents rapid repeated shots.
- After 4 hits, one device shows VICTORY.
- The other device shows DEFEAT.
- FIRE is disabled after finish.
- Demo Mode works on one phone.
