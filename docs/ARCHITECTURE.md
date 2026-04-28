# Architecture

## Style

The app uses a deliberately simple MVVM-style structure. This is an investor MVP, so the code should remain easy to change and avoid heavy abstractions.

```text
MainActivity
  creates repository
  provides ViewModel
  renders Compose screens

ui/
  DuelViewModel
  StartScreen
  WaitingScreen
  BattleScreen
  Components

data/
  DuelRepository
  FirebaseDuelRepository
  OfflineDuelRepository

domain/
  DuelModels
  GameConstants

util/
  CodeGenerator
  VibrationHelper
```

## State Flow

```text
Firebase Realtime Database
        |
FirebaseDuelRepository.observeDuel(code)
        |
DuelViewModel.currentDuel StateFlow
        |
Compose screens
```

User actions flow in the opposite direction:

```text
Button click
  -> DuelViewModel
  -> DuelRepository
  -> Firebase
  -> observeDuel update
  -> UI refresh
```

## Main Classes

### `MainActivity`

Location:

```text
app/src/main/java/com/mvp/duelfire/MainActivity.kt
```

Responsibilities:

- initialize Firebase repository when Firebase config exists;
- fall back to `OfflineDuelRepository` if Firebase is unavailable;
- create `DuelViewModel`;
- render the active Compose screen.

### `DuelViewModel`

Responsibilities:

- hold UI state;
- create duel;
- join duel;
- start battle;
- fire;
- reset duel;
- exit duel;
- run Demo Mode;
- observe Firebase updates;
- derive UI status messages.

### `DuelRepository`

Repository contract:

```kotlin
suspend fun createDuel(playerName: String): Result<String>
suspend fun joinDuel(code: String, playerName: String): Result<Unit>
suspend fun startBattle(code: String): Result<Unit>
suspend fun fire(code: String, myPlayerId: String): Result<Unit>
fun observeDuel(code: String): Flow<Duel?>
suspend fun resetDuel(code: String): Result<Unit>
suspend fun cancelDuel(code: String): Result<Unit>
```

### `FirebaseDuelRepository`

Responsibilities:

- create `/duels/{code}`;
- join as `player2`;
- set duel `active`;
- apply FIRE transaction;
- reset duel;
- stream updates.

The `fire` action uses a Firebase transaction to reduce race-condition risk.

### `OfflineDuelRepository`

Used when Firebase is not configured. It makes normal online actions fail clearly while still allowing ViewModel Demo Mode.

## Compose Screen Model

Screens are selected by:

```kotlin
enum class ScreenState {
    Start,
    Waiting,
    Battle
}
```

The app does not use navigation libraries yet. A simple enum is enough for Version 0.1.

## Design Rules

- Keep screens minimal.
- Keep Battle Screen readable at arm length.
- Use dark background and high contrast text.
- Keep FIRE button large.
- Avoid complex theme work until the core demo is verified.

## Future Architecture Notes

Camera Aim Assist should be added as a separate feature slice:

```text
camera/
  CameraBattleScreen
  CameraPreview
  PoseAnalyzer
  AimAssistState
  AccuracyCalculator
```

Do not mix CameraX / ML Kit code into `FirebaseDuelRepository`. The camera layer should compute a local shot result, then call repository fire logic with fixed or variable damage.

Solo AR Demo should be a separate local feature slice:

```text
ui/ar/
  SoloArScreen
  ArHudOverlay
  CrosshairOverlay

ar/
  ArAvailabilityChecker
  SoloArGameController
  ArHitCalculator
  EnemyNodeFactory
```

It should not depend on Firebase. It can reuse visual concepts from Battle Screen, but its game state should remain local.
