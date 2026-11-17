# iOS validation — 18 November 2026

## Passed locally

- Compiled the shared native Swift URL policy and policy-check executable with Apple Swift 6.1 against the macOS SDK. All 22 URL policy checks passed: Reels routes, ordinary photo/DM/Stories/login routes, disabled blocking, nil/blank URLs, lookalike hosts, and unsupported app/HTTP schemes.
- All 11 JavaScript DOM tests passed: URL recognition, selective article/card hiding, preserving a DM conversation, off/on restoration, disabled startup, SPA history prevention, click cancellation, dynamic links, directly loaded Reel pages, media replay prevention, and resuming after the page cache.
- Swift parser accepted all app Swift source files and the shared policy. **Parsing is not iOS type-checking or an iOS build.**
- Xcode project and privacy manifest passed `plutil` syntax validation.
- Project object references, referenced source/resource existence, and shared scheme XML structure passed local checks.
- App icon generated as a 1024×1024 opaque RGB PNG.

## Blocked in this environment

- `xcode-select` points to `/Library/Developer/CommandLineTools`. Full Xcode, an iOS SDK, simulator runtimes, and `devicectl` are unavailable.
- The local keychain reports zero valid code-signing identities.
- Therefore the complete app has **not** been compiled, type-checked against iOS frameworks, run in an iOS simulator, signed, archived into an IPA, or installed on an iPhone.
- A GitHub Actions workflow for an unsigned iOS simulator build is included but has not been run remotely. A simulator build would not be installable on a phone.
- No App Store Connect app, uploaded build, beta group, or TestFlight invitation link has been created.
- The user selected installation with a personal Apple ID. `PERSONAL-ID-INSTALL.md` documents Personal Team signing; the setup helper has passed shell syntax checking and correctly stops when full Xcode is absent. Installation still requires Xcode, account sign-in in Apple’s interface, and a paired iPhone.

## Still requires a real device and account

Instagram login and challenge flows, saved sessions, all Reel entry points, exact current page markup, shared Reel cards, ordinary posts and DMs, VoiceOver, Dynamic Type, device geometry, playback behavior and on/off switching. Synthetic tests do not establish live Instagram compatibility.

This is a separate Instagram website client inside a native shell. It does not control the official Instagram app and does not meet the original requirement of selectively disabling that app’s native Reels feature. Unknown video formats may escape the filter. No claim of complete Reel inaccessibility, universal preservation of other features, or App Store approval is made.
