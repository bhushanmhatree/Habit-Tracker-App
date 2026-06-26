# Project Log

## v0.1.0 - Initial Android App

Date: 2026-06-26

### Purpose

Create the first publishable foundation for a lightweight Android habit tracker that supports manual tracking, automatic step-based walking habits, notifications, and a home-screen widget.

### Added

- Native Android project structure.
- Minimal black-and-white user interface.
- Habit creation with title, target, unit, and interval.
- Manual progress updates.
- Step counter sensor tracking for walking habits.
- Completion notification channel and runtime notification permission request.
- App widget provider and widget configuration screen.
- Integration settings screen for Google Health Connect and Strava readiness.
- GitHub-ready README, project log, and Play Store checklist.

### Notes

- Health Connect and Strava are scaffolded as integration entry points. Full sync requires app-specific credentials, user consent wording, privacy policy URLs, and Play Console Data Safety declarations.
- Habit intervals are saved in v0.1.0, but recurring background reminders are planned for v0.2.0.
- The project intentionally avoids large UI libraries to keep the app size small.
