# Technical Debt

This file separates real MVP blockers from future product work.

## Resolved

- Lightweight Android toolchain installed on `D:\AndroidTools`.
- Debug APK builds successfully.
- Debug APK installs on Redmi Note 7.
- Gradle Wrapper added, so the project no longer depends on a globally installed Gradle.
- FIRE cooldown now disables the button instead of only showing an error after tapping.

## MVP Blockers

### Firebase Configuration Missing

Status: open.

The app can run Demo Mode without Firebase, but two-phone online duels require:

```text
app/google-services.json
```

The file must come from a Firebase Android app with package:

```text
com.mvp.duelfire
```

### Two-Phone Test Not Done

Status: open.

The current APK has been built and installed on one Redmi Note 7. It still needs a second Android phone or a second install target to verify Firebase sync.

### Manual Demo Mode Verification

Status: open.

The app launches without a crash. Demo Mode still needs manual confirmation on the physical phone:

- FIRE reduces enemy HP;
- fake enemy return fire reduces player HP;
- VICTORY / DEFEAT work;
- reset works;
- rotate / minimize / reopen do not break the flow.

## Accepted MVP Tradeoffs

### Open Firebase Rules For Demo

Temporary Firebase rules are intentionally open for the investor MVP:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

This is not production-safe.

### Basic Player Identity

Players are only `player1` and `player2`. There is no auth, account, or device identity yet.

### Simple Race Handling

FIRE uses a Firebase transaction, which is enough for the MVP. Production would need stronger server-side validation.

### No Connection Presence

`connected` exists in the model, but there is no full presence system yet. Exiting the app does not reliably mark the player disconnected.

## Future Feature Debt

### Camera Aim Assist

Documented but not implemented. It should be developed after Firebase MVP verification.

### Solo AR Demo

Documented but not implemented. This is a strong one-device presentation path, but it should be implemented as a separate feature slice to avoid destabilizing Version 0.1.

### Production Security

Before public testing:

- add real auth/session model;
- lock Firebase writes by player role;
- validate damage and winner server-side or with trusted logic;
- prevent stale duel buildup.
