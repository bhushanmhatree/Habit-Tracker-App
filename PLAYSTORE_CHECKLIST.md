# Google Play Store Checklist

## App Readiness

- [ ] Choose final application ID.
- [ ] Create release keystore and store it securely outside the repository.
- [ ] Add release keystore values to GitHub Actions secrets for internal testing builds.
- [ ] Build signed Android App Bundle (`.aab`).
- [ ] Test on physical devices with and without step counter sensors.
- [ ] Confirm notification permission behavior on Android 13+.
- [ ] Confirm widget behavior after device restart and app update.
- [ ] Confirm reminder notifications after app restart and device reboot.
- [ ] Confirm edit/delete flows preserve expected streak history behavior.

## Privacy And Health Data

- [ ] Publish a privacy policy URL.
- [ ] Explain manual tracking, sensor tracking, Health Connect, and Strava data usage.
- [ ] Request only the Health Connect permissions actually used.
- [ ] Add in-app consent before syncing health or activity data.
- [ ] Verify Health Connect rationale and onboarding screens on Android 14+ and older supported devices.
- [ ] Complete Google Play Data Safety form accurately.
- [ ] Complete any required Health Apps declaration in Play Console.

## Strava

- [ ] Create Strava developer application.
- [ ] Add OAuth redirect URI.
- [ ] Store client ID in Gradle config or secure build config.
- [ ] Never commit client secret.
- [ ] Add disconnect/delete synced data controls.

## Release

- [ ] Add app icon and feature graphic.
- [ ] Add screenshots.
- [ ] Run release build.
- [ ] Upload internal testing release.
- [ ] Collect tester feedback before production.
