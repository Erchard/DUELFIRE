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

**Important:** create **Realtime Database** in the Firebase project *before* or *then* re-download `google-services.json`. If `project_info` in the JSON has no `firebase_url`, the Android SDK cannot open the correct DB instance (logcat: `PersistentConnection` / *URL configured incorrectly*). After enabling RTDB, download the file again and replace `app/google-services.json`.

## App initialization

`MainActivity` picks the repository as follows:

- if `FirebaseApp.getApps(context)` is non-empty, or `initializeApp` succeeds, use `FirebaseDuelRepository`;
- otherwise use `OfflineDuelRepository` (demo without config).

## Realtime Database rules (required server-side step)

Security rules run **only** on Firebase’s servers. The Android app cannot publish or bypass them. A new database often ships with rules that **deny** client writes until you publish something else.

This repo includes:

- `database.rules.json` — MVP rules (open read/write for local demo)
- `firebase.json` — points the Firebase CLI at that file

**Option A — Console:** Firebase Console → **Realtime Database** → **Rules** → paste the contents of `database.rules.json` → **Publish**.

**Option B — CLI:** from the repo root, after `firebase login` and `firebase use <your-project-id>`:  
`firebase deploy --only database`

Until rules allow access to `duels`, RTDB operations fail with `Permission denied` in logcat; that is expected until the step above is done.

Gradle follows current Firebase guidance: **Google services plugin 4.4.4** (root `build.gradle.kts`), **Firebase BoM 34.12.0**, dependency **`com.google.firebase:firebase-database`** (not `firebase-database-ktx`; KTX APIs live in the main module since BoM 34 — see [Kotlin migration](https://firebase.google.com/docs/android/kotlin-migration)).

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
