# Habit Tracker

Habit Tracker is a minimal black-and-white Android app for manual and sensor-assisted habit tracking. The first version focuses on a small APK, a clean Threads-inspired interface, step-counter based walking habits, completion notifications, and a home-screen widget.

## Purpose

Help people track recurring habits with either manual entries or automatic progress from phone sensors. Example: create a walking habit for 1,000 steps every 2 hours, keep the phone with you, and receive a notification when the target is completed.

## Features in v0.2.0

- Manual habit creation and progress entry.
- Habit editing and deletion.
- Automatic walking progress using Android's step counter sensor.
- Real streak tracking from completion history.
- Reminder timer notifications per habit.
- Completion notification when a tracked habit reaches its target.
- Home-screen widgets with 2x1, 2x2, and 4x1 layouts.
- Widget display options for checklist, streak, target, score, and frequency.
- Local-first storage with no account required.
- Samsung Health-inspired interface with Poppins typography, soft cards, and clear action labels.
- Health Connect permissions, rationale, onboarding, availability check, and settings entry point.
- Google Play readiness checklist included.

## Design Preview

Open [design-preview.html](design-preview.html) in Cursor or a browser to review the app direction before generating APK files.

The preview mirrors the Android direction: Samsung One UI-style spacing, rounded health cards, Poppins text, named buttons, progress metrics, and 2x1, 2x2, and 4x1 widget concepts.

## v0.1.0 Scope

The app stores a target interval on each habit, such as 1,000 steps every 2 hours, and can track the active walking session from the phone's step counter. Background interval scheduling, Health Connect sync, and Strava OAuth are intentionally left as next-version work because they require release credentials, consent copy, and Play Console health data declarations.

## Planned Features

- Background interval reminders that restart each habit window automatically.
- Full Health Connect read/write sync after package signing and data safety review.
- Strava OAuth flow after a Strava developer app is created.
- Habit schedules, streaks, reminders, and richer analytics.
- Export and backup.

## Build

Open this folder in Android Studio and sync Gradle.

```bash
./gradlew assembleDebug
```

On Windows without the Gradle wrapper installed:

```powershell
gradle assembleDebug
```

GitHub Actions also builds downloadable artifacts on every push:

- Debug APK for direct phone testing.
- Unsigned release APK/AAB.
- Signed internal testing APK/AAB when release keystore secrets are configured.

Required GitHub secrets for signed internal builds:

- `HABIT_RELEASE_KEYSTORE_BASE64`
- `HABIT_RELEASE_STORE_PASSWORD`
- `HABIT_RELEASE_KEY_ALIAS`
- `HABIT_RELEASE_KEY_PASSWORD`

## Publishing Notes

- The app targets modern Android and requests only required permissions.
- Health/fitness data integrations require clear consent screens, a privacy policy, and Google Play Data Safety declarations before release.
- Strava sync requires a Strava API application and OAuth credentials.

See [PROJECT_LOG.md](PROJECT_LOG.md) and [PLAYSTORE_CHECKLIST.md](PLAYSTORE_CHECKLIST.md).
