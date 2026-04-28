# Firebase Setup

## Required Firebase Product

Use Firebase Realtime Database.

## Android App

Create an Android app in Firebase with:

```text
Package name: com.mvp.duelfire
App nickname: Duel Fire
```

Download:

```text
google-services.json
```

Place it at:

```text
app/google-services.json
```

The real file is ignored by Git. Keep `app/google-services.example.json` as a placeholder only.

## App initialization

`MainActivity` picks the repository as follows:

- if `FirebaseApp.getApps(context)` is non-empty, or `initializeApp` succeeds, use `FirebaseDuelRepository`;
- otherwise use `OfflineDuelRepository` (demo without config).

## Temporary MVP Rules

For local demo only:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Do not use these rules for public testing or production.

### After MVP (guidance)

Public rules should at least scope writes under `duels` and validate field shapes. Full anti-cheat is a separate phase (see **Production Security Later** below).

## Database Shape

```text
duels
  {duelCode}
    duelCode
    status
    createdAt
    updatedAt
    winnerPlayerId
    players
      player1
      player2
    lastEvent
```

Example:

```json
{
  "duels": {
    "4821": {
      "duelCode": "4821",
      "status": "active",
      "createdAt": 1710000000000,
      "updatedAt": 1710000005000,
      "winnerPlayerId": null,
      "players": {
        "player1": {
          "id": "player1",
          "name": "Player 1",
          "hp": 100,
          "alive": true,
          "connected": true,
          "lastFireAt": 1710000003000
        },
        "player2": {
          "id": "player2",
          "name": "Player 2",
          "hp": 75,
          "alive": true,
          "connected": true,
          "lastFireAt": 0
        }
      },
      "lastEvent": {
        "type": "hit",
        "byPlayerId": "player1",
        "targetPlayerId": "player2",
        "damage": 25,
        "timestamp": 1710000003000
      }
    }
  }
}
```

## Repository behaviour

### Create duel

Duel code creation is **atomic** via a Firebase transaction: “slot free” and write happen in one step (no read-then-write race).

### Join / Start / Fire

- `join` and `startBattle` use transactions; if a transaction is rejected, one extra `get` runs to surface a clear user message (missing duel / full / not waiting).
- `fire` shares validation and damage with local demo code through `DuelRules.resolvePlayerFire` (single source of truth in app code).

### Exit / cleanup

**Exit** in lobby or battle calls `cancelDuel`: the `duels/{code}` node is **removed** (`removeValue`). The other device sees an empty snapshot; the client returns to the start screen with an explanatory message.

### Realtime listener

If data disappears or the listener is cancelled (network / rules), the update stream does not throw to the collector: a warning is logged and the UI returns to the start screen with a message.

## Fire Transaction Rules

The repository verifies before writing (aligned with `DuelRules` on the client):

- duel exists;
- duel status is `active` (for fire);
- shooter exists;
- target exists;
- shooter is alive;
- target is alive;
- shooter cooldown has passed;
- target HP is above 0.

The transaction writes:

- target HP;
- target alive flag if HP reaches 0;
- shooter `lastFireAt`;
- duel `status` if finished;
- `winnerPlayerId` if finished;
- `lastEvent`;
- `updatedAt`.

## Production Security Later

Before any public test, replace open rules with authenticated or session-based rules. Future production model should prevent:

- arbitrary writes to enemy HP;
- joining already full duels;
- editing `winnerPlayerId`;
- changing another player `lastFireAt`;
- spoofing `playerId`.
