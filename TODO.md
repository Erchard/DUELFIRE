# TODO

This list is ordered by investor-demo priority.

## P0: Make Version 0.1 Buildable

- [x] Install or configure local JDK 17.
- [x] Install or configure Android SDK.
- [ ] Add or generate Gradle wrapper files.
- [ ] Add real `app/google-services.json`.
- [x] Run `gradle assembleDebug`.
- [x] Fix any compile errors from the first real Android build.
- [x] Produce `app/build/outputs/apk/debug/app-debug.apk`.

## P0: Verify On One Phone

- [x] Enable Developer Options on Android phone.
- [x] Enable USB Debugging.
- [x] Confirm `adb devices` shows the phone as `device`.
- [x] Install debug APK with `adb install -r`.
- [x] Open app.
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
- [ ] Add stronger hit flash animation.
- [ ] Add simple sound effect for FIRE.
- [ ] Add "Copy duel code" if useful.
- [ ] Add player names to Battle Screen.
- [ ] Add a small connection status label.

## P1: Safety And Stability

- [ ] Add simple repository tests for game rules.
- [ ] Add transaction-specific tests or manual race-condition checklist.
- [ ] Prevent joining with invalid code length.
- [ ] Improve handling when opponent exits.
- [ ] Add `cancelled` duel UI state.
- [ ] Avoid leaving stale waiting duels forever.
- [ ] Replace temporary Firebase rules before any public test.

## P2: Camera Aim Assist 0.2

- [ ] Add CameraX dependencies.
- [ ] Add `CAMERA` permission.
- [ ] Add camera permission request flow.
- [ ] Add full-screen camera Battle Screen.
- [ ] Draw center crosshair.
- [ ] Keep fixed 25 damage while camera is visible.
- [ ] Add fallback to non-camera battle screen.

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
