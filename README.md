# Daybook

Daily planner for Android — Jetpack Compose, custom (non-Material) UI.

- Onboarding: name + "plan tomorrow" time (persisted in DataStore, asked once)
- Daily notification at that time
- Tasks (start/end) with **no-overlap** validation, stored in Room
- Alarms: 5-min reminder → live count-down notification (lockscreen-visible; opts in to Android 16 Live Updates) → "did you finish?" prompt that opens a bottom sheet (Complete / Incomplete capsule buttons, ✔ / ✕ badges)
- Journal: per-day report (completed / total + task list), plus a 23:55 summary notification
- Libraries used: Konfetti (completion burst), Swipe (swipe-to-delete). Animations are otherwise hand-rolled Compose.

## Build
Open in Android Studio (it will generate the Gradle wrapper), or `gradle :app:assembleRelease`.

## CI (`.github/workflows/release-apk.yml`)
Builds a minified + resource-shrunk release APK on every push to `main`, tag `v*`, or manual run.
Add these repo secrets to sign with your own key (otherwise a throwaway key is generated):
`KEYSTORE_BASE64` (`base64 -w0 release.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Tags `v*` also publish the APK as a GitHub Release.
