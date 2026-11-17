# ReelGuard for iPhone — separate app prototype

This is a native SwiftUI iPhone app containing a `WKWebView` that opens Instagram’s website with a Reels filter. It is **not** an extension for the official Instagram app and cannot selectively disable Reels there. Opening the official app or another browser bypasses this filter.

The source has been implemented. It has not been compiled for iOS, signed, installed on an iPhone, or tested against a live Instagram account. The current development Mac has Command Line Tools, but no full Xcode/iOS SDK and no valid signing identities. There is no IPA or TestFlight URL yet.

## Implemented behavior

- Native on/off switch, Posts and DMs buttons, loading/error states, and clear-login control.
- A native navigation policy cancels known `/reel/` and `/reels/` page loads while blocking is enabled.
- A script installed at document start hides identifiable Reel cards and navigation links, cancels Reel clicks and SPA history navigation, and pauses media in identified blocked cards/pages.
- Shared Reel cards are filtered in DMs while the surrounding conversation is retained.
- Turning blocking off removes the filter’s hiding attributes and restores normal links. Existing Instagram styles are preserved.
- Cookies are stored in the app’s default WebKit data store. There is no project backend or analytics. Instagram receives its normal website traffic. The filter never reads input values or sends messages/cookies to a project-owned service.
- All web video requires a user tap to play. External main-window links, app deep links, and external login popups are unavailable in this prototype.

## Open and install with Xcode

**Selected route: personal Apple ID.** Follow [Personal ID installation](PERSONAL-ID-INSTALL.md). The `Open iPhone Setup.command` helper checks prerequisites and opens the project when full Xcode and its iOS SDK are available. No TestFlight link is used for this route.

1. Install full Xcode on a Mac. Command Line Tools alone cannot compile the app.
2. Open `ReelGuard.xcodeproj` and select the **ReelGuard** scheme.
3. Under **Signing & Capabilities**, select your Apple team. Change `dev.reelguard.ios` to a bundle ID available to your team if needed. Automatic signing is configured.
4. Connect your iPhone, select it as the destination, and follow Xcode’s device pairing and Developer Mode instructions.
5. Run the app. On its intro screen, review the separate-app limitation, then continue and sign in directly on Instagram’s webpage.

A Personal Team can be used for your own device through Xcode, subject to Apple’s provisioning limitations. A public TestFlight link requires Apple Developer Program membership, an App Store Connect app record, a signed uploaded build, a tester group, and Apple’s applicable beta review. Source files and unsigned simulator apps are not iPhone installation links.

To compile for the simulator on a Mac with Xcode:

```sh
xcodebuild -project ReelGuard.xcodeproj -scheme ReelGuard \
  -configuration Debug -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath DerivedData CODE_SIGNING_ALLOWED=NO build
```

The repository includes a macOS GitHub Actions workflow for this build. It has been authored, not executed on a remote repository. Its simulator artifact cannot be installed on a physical iPhone.

## Checks

From this directory:

```sh
swift run --package-path Core ReelGuardPolicyChecks
npm ci --prefix FilterTests --ignore-scripts --no-audit --no-fund
npm test --prefix FilterTests
```

The policy runner uses the same Swift URL policy included in the app. It needs no XCTest installation. The JS tests use synthetic DOM fixtures; they do not log in to Instagram. See `VALIDATION.md` for results and unresolved device checks.

## Limits and acceptance checks

Instagram can change markup or deliver video through a normal `/p/` URL. Unknown media and entry points can escape this first filter. Conversely, an article containing a Reel link may be hidden even if it also contains other material. This is not yet a guarantee that every Reel is inaccessible or every non-Reel feature is unaffected.

On a real iPhone, verify login, login challenges, saved-session restart, Posts, carousels, Stories, DMs, directly uploaded DM videos, shared Reel links, Explore, profiles, back/forward, and on/off switching. Verify video playback is stopped for detected hidden cards. Inspect the native blocked screen with VoiceOver and larger text. Test phone sizes before release. A web wrapper may not support every native Instagram feature; Meta may restrict embedded web login. None of these checks have passed yet.

The privacy manifest declares only the app’s own preference storage. App Store privacy disclosures must account for the actual embedded Instagram behavior before any store submission; no store approval is claimed.

References: [WKNavigationDelegate](https://developer.apple.com/documentation/webkit/wknavigationdelegate), [document-start injection](https://developer.apple.com/documentation/webkit/wkuserscriptinjectiontime), [personal device development](https://developer.apple.com/help/account/basics/about-your-developer-account/), [TestFlight](https://developer.apple.com/testflight/).
