# TODO

This list is ordered by investor-demo priority.

## P0: Make Version 0.1 Buildable

- [x] Install or configure local JDK 17.
- [x] Install or configure Android SDK.
- [x] Add or generate Gradle wrapper files.
- [ ] Add real `app/google-services.json`.
- [x] Run `gradle assembleDebug`.
- [x] Run `gradlew.bat assembleDebug`.
- [x] Fix any compile errors from the first real Android build.
- [x] Produce `app/build/outputs/apk/debug/app-debug.apk`.

## P0: Verify On One Phone

- [x] Enable Developer Options on Android phone.
- [x] Enable USB Debugging.
- [x] Confirm `adb devices` shows the phone as `device`.
- [x] Install debug APK with `adb install -r`.
- [x] Open app.
- [x] Verify app process starts without crash after install.
- [ ] Test Start Screen.
- [ ] Test Demo Mode.
- [ ] Verify FIRE reduces fake enemy HP.
- [ ] Verify fake enemy can damage player.
- [ ] Verify VICTORY and DEFEAT states in Demo Mode.
- [ ] Verify app survives rotate / minimize / reopen.

## P0: Verify Firebase Two-Phone Duel

- [ ] Create Firebase project.
- [ ] Add Android app with package `com.mvp.duelfire`.
- [ ] Download `google-services.json`.
- [ ] Enable Realtime Database.
- [ ] Set temporary demo rules.
- [ ] Install APK on phone A.
- [ ] Install APK on phone B.
- [ ] Create Duel on phone A.
- [ ] Join Duel from phone B using the code.
- [ ] Start Battle from phone A.
- [ ] Fire from phone A and verify phone B HP decreases.
- [ ] Fire from phone B and verify phone A HP decreases.
- [ ] Verify cooldown blocks fast repeated shots.
- [ ] Verify VICTORY / DEFEAT after 4 hits.
- [ ] Verify FIRE is disabled after finish.

## P1: Polish Version 0.1 Demo

- [ ] Improve error messages for Firebase unavailable / no internet.
- [ ] Show clear "Firebase not configured" state when `google-services.json` is missing.
- [ ] Add a visible cooldown timer.
- [x] Disable FIRE during cooldown.
- [ ] Add stronger hit flash animation.
- [ ] Add simple sound effect for FIRE.
- [ ] Add "Copy duel code" if useful.
- [ ] Add player names to Battle Screen.
- [ ] Add a small connection status label.

## P1: Safety And Stability

- [x] Document current technical debt and accepted MVP tradeoffs.
- [ ] Add simple repository tests for game rules.
- [ ] Add transaction-specific tests or manual race-condition checklist.
- [ ] Prevent joining with invalid code length.
- [ ] Improve handling when opponent exits.
- [ ] Add `cancelled` duel UI state.
- [ ] Avoid leaving stale waiting duels forever.
- [ ] Replace temporary Firebase rules before any public test.

## P1: Firebase production hardening (post-MVP)

MVP uses open RTDB rules in `database.rules.json`. For a real product, security is enforced on Firebase servers (rules + optional Functions); the Android app cannot replace that.

- [ ] Add **Firebase Authentication** (anonymous, phone, or other) and require `auth != null` in rules.
- [ ] Extend duel model with **participant identity** (e.g. `hostUid` / `guestUid` or `participants/{auth.uid}`) so rules can allow read/write only for players in that duel.
- [ ] Replace open `.read/.write: true` with **scoped rules** under `duels/{duelId}` (validate allowed children and types where practical).
- [ ] Enable **Firebase App Check** to reduce abuse from non-app clients.
- [ ] Add **Cloud Functions** (Admin SDK) or a small backend to **validate combat actions** (fire, HP, cooldown, finish) if competitive integrity matters; avoid trusting client-only HP writes for ranked play.
- [ ] Consider **rate limiting** / cleanup of stale duels (Functions or scheduled job).
- [ ] Document production `database.rules.json` (or multi-env rules) and deploy path (`firebase deploy --only database`).

## P2: Camera Aim Assist 0.2

- [ ] Add CameraX dependencies.
- [ ] Add `CAMERA` permission.
- [ ] Add camera permission request flow.
- [ ] Add full-screen camera Battle Screen.
- [ ] Draw center crosshair.
- [ ] Keep fixed 25 damage while camera is visible.
- [ ] Add fallback to non-camera battle screen.

## P2: Solo AR Demo 0.2A

- [x] Add `Solo AR` entry on Start Screen.
- [ ] Confirm ARCore availability on Redmi Note 7 (and other test devices).
- [x] Add Google Play Services for AR (via SceneView / ARCore dependency).
- [x] Add `CAMERA` permission and AR permission flow (SceneView handler).
- [x] Add SceneView AR (`arsceneview`).
- [x] Add full-screen AR camera view.
- [x] Detect plane or stable tracking (horizontal plane + anchor).
- [x] Place simple virtual enemy in AR (procedural cube).
- [x] Draw center crosshair overlay.
- [x] Show player HP and virtual enemy HP (layout to compact — see backlog below).
- [ ] Implement screen-space hit / miss calculation (optional refinement).
- [ ] Implement variable damage by aim distance (optional).
- [x] Virtual enemy return fire on a timer (needs gating — see backlog).
- [x] Show VICTORY / DEFEAT locally (shared demo duel rules).
- [ ] Fall back to current Demo Mode if AR is unavailable.

## P2: Solo AR — backlog (playtest feedback)

Зафіксовано з реального тесту; виконувати по черзі.

- [ ] **FIRE лишається неактивною** — розібрати гейтинг: `anchorReady` / `TrackingState` / `fireBlockedUntilMs` / `fireEnabled` у `SoloArScreen`. Додати явні статуси («Шукаю поверхню…», «Стабілізую ціль…») коли кнопка вимкнена. За потреби послабити критерій готовності або розвести «плейсмент ворога» і «дозвіл стріляти».

- [ ] **Ворог стріляє занадто рано** — у Solo AR не запускати відповідний корутин (`demoEnemyJob`) або не викликати `demoEnemyFire`, доки гравець **хоча б раз** не влучив у ворога (прапорець у `DuelViewModel` для `SOLO_AR` / `isSoloArMode`). Після першого влучання залишити або збільшити інтервал пострілів.

- [ ] **Ворога майже не видно** — без Unity/Unreal найдешевше: збільшити куб і підняти над площиною (`CubeNode` size/center, scale); контрастніший матеріал і освітлення; за потреби один **безкоштовний GLB** у `assets` + `ModelNode` (відкриті паки на кшталт Poly Pizza / Kenney) — без окремого рушія. Перевірити відстань/висоту anchor відносно камери.

- [ ] **Приціл при влучному наведенні** — при hit-test з центру екрана по ворогу змінювати **колір і/або розмір** хрестика (`animateColorAsState` / `animateFloatAsState` у `SoloArScreen`).

- [ ] **Компактні HP зверху** — суттєво зменшити індикатори: один короткий рядок у **top** екрана (менші шрифти, тонші бари), без колонки що розтягується на всю висоту; AR лишається на весь екран під легким оверлеєм.

## P2: ML Target Detection 0.3

- [ ] Add ML Kit Pose Detection dependency.
- [ ] Add pose analyzer.
- [ ] Detect one prominent person in frame.
- [ ] Draw body box or landmark overlay.
- [ ] Show `TARGET DETECTED` / `NO TARGET`.
- [ ] Block FIRE when no target is detected in camera mode.

## P3: Accuracy Damage 0.4

- [ ] Compute torso center from shoulders and hips.
- [ ] Compare torso center to screen crosshair.
- [ ] Show accuracy percentage.
- [ ] Calculate variable damage.
- [ ] Change repository `fire` API to accept damage.
- [ ] Save actual damage in Firebase `lastEvent`.
- [ ] Show `HIT 82%`, `MISS`, and damage amount.

## P3: Enemy Confirmation 0.5

- [ ] Choose marker strategy: QR, ArUco-like marker, or colored badge.
- [ ] Detect marker in camera frame.
- [ ] Associate marker with opponent duel code or player ID.
- [ ] Count camera hit only on confirmed target.
- [ ] Document demo setup for marker placement.

## Later, Not For MVP

- [ ] GPS distance validation.
- [ ] Bluetooth proximity.
- [ ] BLE direct duel mode.
- [ ] Compass aiming.
- [ ] Arena mode.
- [ ] More than 2 players.
- [ ] Tournament mode.
- [ ] Admin panel.
- [ ] Payments.
- [ ] Anti-cheat.
