# Validation — 18 November 2026

## Passed

- Android debug APK compilation (`:app:assembleDebug`).
- 12 detector unit tests; zero failures or errors (`:app:testDebugUnitTest`).
- Android lint: zero errors, two compatibility warnings. The warnings concern API-31-only configuration attributes intentionally ignored on older Android versions; older devices use `allowBackup=false` / `fullBackupContent=false` and the service is never designated an accessibility tool.
- APK signature verification with Android Build Tools `apksigner` (debug signature, v2).
- APK permissions inspection with `aapt`: no requested application permissions, including no internet permission. Accessibility and tile services use system-protected binding permissions.
- Synthetic native smoke checks on an Android 14 / API 34 arm64 emulator, 1080×2400:

```text
PASS: photo feed remains accessible
PASS: DMs remain accessible
PASS: Reel opened from DM returns to DM
PASS: Story with preloaded offscreen Reels player remains accessible
PASS: Back failure leaves an actionable blocker
PASS: overlay off switch dismisses blocker and allows player
PASS: leaving Instagram clears overlay
PASS: Quick Settings tile turns blocking off
PASS: Quick Settings tile turns blocking on
```

The smoke checks use a purpose-built fixture bearing Instagram’s package name, not the real Instagram app. The fixture is compiled only with `-PincludeFixture`. The user-facing APK does not contain it. Android’s stock `uiautomator dump` omits the non-focusable overlay, so the script checks its Window Manager registration and uses a visually verified button coordinate for the off action. The blocker and main screen were visually inspected from emulator screenshots.

## Not established

- Compatibility with any live, logged-in Instagram release, account or physical device.
- Blocking inline Reels, previews, or every Reels entry point.
- Zero frames or audio before detection; the overlay does not pause media, and failed Back navigation may leave audio playing.
- Multi-window, landscape, different phone geometries, older Android versions, or OEM battery management.
- iOS selective blocking inside the official Instagram app. A separate website-based iPhone client was subsequently implemented; it is outside this Android report. See `../ios/VALIDATION.md`.
- Google Play approval, public GitHub publication, or a signed production release.

The requested complete Android-and-iOS product is **not complete**. This deliverable is a tested Android prototype and a documented iOS feasibility boundary. The next Android validation step is the physical-device checklist in `device-testing.md`.
