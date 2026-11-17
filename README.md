# ReelGuard

An open-source Android companion that detects Instagram’s full-screen Reels player, covers it, and attempts to navigate back. Turn blocking on or off in the app, on the blocking screen, or with a Quick Settings tile. A separate [iPhone app prototype](ios/README.md) opens Instagram’s website inside a native app with Reels filtering; it cannot modify the official Instagram app.

**Prototype, not a complete Reels removal system.** Real Instagram verification is still required. Instagram’s interface changes across versions and accounts. Full-screen player detection is the initial scope. Inline Home-feed Reels, thumbnails, previews, and a brief frame or sound before detection may remain. This app does not modify Instagram or turn off Reels on its servers.

## Platforms

| Platform | Status |
| --- | --- |
| Android 8+ | Native prototype implemented; synthetic tests do not prove live Instagram compatibility |
| iPhone | Separate SwiftUI/WebKit client compiled for simulator and iPhone; unsigned IPA for personal signing. Live-device/Instagram testing remains pending. No control over official Instagram. |

See [the iPhone feasibility note](docs/ios-feasibility.md).

## Install the prototype

Use the supplied `reelguard-debug.apk` from the output folder, or build it below. It is a debug-signed development build, not a store release.

1. Install on an Android phone. Android may ask you to allow installation from the app opening the APK.
2. Open ReelGuard, review its disclosure, and choose **I agree**.
3. In Android Accessibility settings, enable **ReelGuard**. Some sideloaded installations require **App info → ⋮ → Allow restricted settings** first. Menu names vary by device.
4. Return to ReelGuard and turn **Block Reels** on.
5. Optionally add the **Block Reels** Quick Settings tile. Android 13+ offers an add button; older versions can add it by editing Quick Settings.

To install through a connected development device:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Reels opened from DMs are blocked in this first version. The surrounding conversation is not a blocking target. A future single-Reel exception needs separate investigation; nothing currently distinguishes a friend from another sender.

## Build and check

Requires JDK 17, Android SDK platform 34, and Build Tools 34.0.0. Point `ANDROID_HOME` at your SDK or create an untracked `local.properties` with `sdk.dir=...`.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The Gradle wrapper and Android Gradle Plugin versions are pinned. The first build downloads tools and dependencies. GitHub Actions runs compilation, detector tests and lint; it does not test a real Instagram session.

For a **disposable Android emulator only**, the optional synthetic fixture and smoke script exercise the real accessibility service, overlay and recovery controls:

```sh
./gradlew -PincludeFixture :app:assembleDebug :fixture:assembleDebug
python3 tools/emulator-smoke.py --serial emulator-5554
```

The fixture deliberately uses Instagram’s package name to exercise package filtering. The script refuses physical devices and refuses to overwrite a different installed Instagram application. It changes accessibility settings in the test emulator. Do not install the fixture on a personal phone. It does not contain Instagram code or connect to Instagram.

## Detection and privacy

- Accessibility events are filtered to `com.instagram.android`.
- The service examines package name, view resource identifier, visibility, and bounds. It never calls text/content-description or screenshot APIs.
- The initial rule requires an exact `clips_viewer_view_pager` identifier and a visible screen intersection of at least 80% width and 55% height. No generic `reel` substring matching: those names can refer to Stories.
- A hidden or offscreen preloaded player must not trigger blocking. Unrecognized screens are allowed to avoid disrupting posts or messages.
- The overlay uses `TYPE_ACCESSIBILITY_OVERLAY`; no separate “draw over other apps” permission is requested.
- A bounded traversal and throttled event handling limit processing. A lightweight polling check detects enable/disable changes and removes the overlay after leaving the player.
- The service rechecks the target before sending Back. If Back fails, the overlay offers Go back and Turn blocking off.
- No internet permission, backend, analytics, login, text capture, or screen dumps. Local preferences store only consent and the requested blocking state. Android accessibility itself grants broader capability than this implementation uses.

## Validation and limitations

See [device acceptance checklist](docs/device-testing.md) and [validation results](docs/validation.md). Unit tests cover offscreen and invisible players, wrong packages, thumbnails, Stories identifiers, unrelated feed/DM identifiers, and malformed metadata. The opt-in `fixture` module tests the Android lifecycle on a synthetic app; it is not Instagram and must never be installed on a personal device that has Instagram.

Known constraints:

- No physical device or logged-in Instagram session has been verified.
- Only the supported full-screen player is detected. No claim of blocking every Reel or preserving every Instagram version’s behavior.
- Split-screen, floating windows, landscape, localization, OEM battery management, and app updates need device testing. Screen-size thresholds may miss multi-window players.
- Detection is reactive. A short frame/audio leak is possible; no global audio settings are changed. The overlay itself does not pause media. If Back fails, audio may continue until the user leaves the viewer.
- Removing or disabling the accessibility service or switching blocking off is always allowed.
- Google Play distribution requires an accessibility declaration, disclosure, consent and review. This productivity app must not claim `isAccessibilityTool=true`.

## Contributing

Use a physical device to verify a detection change. Record Android/Instagram version and pass/fail results without collecting message contents. Add tests for both the blocked player and adjacent allowed surfaces. Do not add broad text matching, network interception, global muting, or account credentials to fix a detector.

The source is MIT licensed. Instagram is a Meta product; this project is independent and unaffiliated.

## References

- [Android accessibility services](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Google Play accessibility requirements](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Apple app sandbox](https://support.apple.com/en-ie/guide/security-pdf/sec15bfe098e/1/web/1)
- [Apple shielding API](https://developer.apple.com/documentation/managedsettings/shieldsettings)
- [Independent report of Instagram player IDs and offscreen preloading](https://github.com/mtpiz/BlockReels) — technical background only; implementation here is original. Its live-device observations are not validation of ReelGuard.
