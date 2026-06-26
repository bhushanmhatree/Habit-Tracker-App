# Habit Tracker

Habit Tracker is a minimal black-and-white Android app for manual and sensor-assisted habit tracking. The first version focuses on a small APK, a clean Threads-inspired interface, step-counter based walking habits, completion notifications, and a home-screen widget.

## Purpose

Help people track recurring habits with either manual entries or automatic progress from phone sensors. Example: create a walking habit for 1,000 steps every 2 hours, keep the phone with you, and receive a notification when the target is completed.

## Features in v0.1.0

- Manual habit creation and progress entry.
- Automatic walking progress using Android's step counter sensor.
- Completion notification when a tracked habit reaches its target.
- Home-screen widget with selectable habit and quick manual increment.
- Local-first storage with no account required.
- Black and white visual design inspired by Meta Threads.
- Integration screen prepared for Google Health Connect and Strava setup.
- Google Play readiness checklist included.

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

## Publishing Notes

- The app targets modern Android and requests only required permissions.
- Health/fitness data integrations require clear consent screens, a privacy policy, and Google Play Data Safety declarations before release.
- Strava sync requires a Strava API application and OAuth credentials.

See [PROJECT_LOG.md](PROJECT_LOG.md) and [PLAYSTORE_CHECKLIST.md](PLAYSTORE_CHECKLIST.md).
