# Test Plan

## Goal

Verify that Duel Fire is stable enough for an investor demo on one or two Android phones.

## Smoke Test

- [ ] App launches.
- [ ] Start Screen is visible.
- [ ] Player name field accepts text.
- [ ] Duel code field accepts 4 digits.
- [ ] Demo Mode opens Battle Screen.
- [ ] FIRE button is large and visible.
- [ ] Reset works.
- [ ] Exit returns to Start Screen.

## Demo Mode Test

- [ ] Tap `Demo Mode`.
- [ ] Verify player HP is 100.
- [ ] Verify enemy HP is 100.
- [ ] Tap FIRE once.
- [ ] Verify enemy HP becomes 75.
- [ ] Verify status shows `HIT` or cooldown state.
- [ ] Wait for fake enemy shot.
- [ ] Verify player HP decreases.
- [ ] Continue until VICTORY.
- [ ] Reset and let fake enemy win if possible.
- [ ] Verify DEFEAT state.

## Firebase Create Duel Test

- [ ] Add `google-services.json`.
- [ ] Enable temporary Realtime Database rules.
- [ ] Launch app on phone A.
- [ ] Enter name.
- [ ] Tap `Create Duel`.
- [ ] Verify 4 digit code is shown.
- [ ] Verify Firebase has `/duels/{code}`.
- [ ] Verify status is `waiting`.
- [ ] Verify `player1` exists.

## Firebase Join Duel Test

- [ ] Launch app on phone B.
- [ ] Enter player name.
- [ ] Enter code from phone A.
- [ ] Tap `Join Duel`.
- [ ] Verify phone B shows waiting for host.
- [ ] Verify phone A shows opponent connected.
- [ ] Verify Firebase has `player2`.

## Battle Sync Test

- [ ] Tap `Start Battle` on phone A.
- [ ] Verify both phones enter Battle Screen.
- [ ] Tap FIRE on phone A.
- [ ] Verify phone B own HP decreases by 25.
- [ ] Verify phone A enemy HP decreases by 25.
- [ ] Tap FIRE on phone B.
- [ ] Verify phone A own HP decreases by 25.
- [ ] Verify phone B enemy HP decreases by 25.

## Cooldown Test

- [ ] Tap FIRE twice quickly on one phone.
- [ ] Verify only one hit is applied.
- [ ] Verify status shows `COOLDOWN`.
- [ ] Wait 2 seconds.
- [ ] Verify another shot can be fired.

## Finish Test

- [ ] Hit one player 4 times.
- [ ] Verify loser HP becomes 0.
- [ ] Verify winner sees VICTORY.
- [ ] Verify loser sees DEFEAT.
- [ ] Verify FIRE button no longer applies damage.
- [ ] Tap Play Again / Reset.
- [ ] Verify HP returns to 100.

## Resilience Test

- [ ] Rotate phone during Start Screen.
- [ ] Rotate phone during Battle Screen.
- [ ] Minimize app during waiting state.
- [ ] Reopen app.
- [ ] Minimize app during active duel.
- [ ] Reopen app.
- [ ] Temporarily disable internet and observe error behavior.

## Camera Future Test Placeholder

For Version 0.2+:

- [ ] Camera permission request works.
- [ ] Camera preview opens.
- [ ] Crosshair stays centered.
- [ ] HP overlay remains readable.
- [ ] FIRE remains reachable.
- [ ] App falls back if camera permission is denied.
