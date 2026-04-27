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

## Fire Transaction Rules

The repository should verify before writing:

- duel exists;
- duel status is `active`;
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
