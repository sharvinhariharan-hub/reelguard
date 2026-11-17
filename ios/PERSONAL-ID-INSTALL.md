# Install using a personal Apple ID

The user selected **personal Apple ID signing**. There are two routes: build/sign directly in Xcode as described here, or download the cloud-built iPhone IPA and sign it with [AltStore Classic](ALTSTORE-INSTALL.md). Neither requires a paid membership or TestFlight. A public IPA download URL still requires personal signing before installation.

## Current prerequisite

On 18 November 2026, this Mac runs macOS 15.5 and has only Command Line Tools. Full Xcode, an iOS SDK, and a signing identity are not installed. Before choosing an Xcode download, check the iPhone’s iOS version against [Apple’s compatibility table](https://developer.apple.com/xcode/system-requirements). Current App Store Xcode versions may require a newer macOS than this Mac has; older compatible versions are available from [Apple Developer Downloads](https://developer.apple.com/download/all/?q=Xcode). Do not assume a macOS update is required until the device and Xcode compatibility have been checked.

## Setup

1. Install a compatible full Xcode version and its iOS platform support. Open Xcode and complete first-run setup.
2. Double-click `Open iPhone Setup.command`, or open `ReelGuard.xcodeproj` directly.
3. In **Xcode → Settings → Apple Accounts**, add your personal Apple Account. Enter your password and verification code only in Apple’s sign-in interface.
4. Select the **ReelGuard** target and **Signing & Capabilities**. Keep **Automatically manage signing** enabled and select **your Personal Team**. No shared or developer-owned Apple ID is required.
5. If `dev.reelguard.ios` is unavailable, replace it with a unique reverse-domain identifier for this development build. Keep it stable for subsequent reinstalls.
6. Connect and unlock your iPhone. Accept the iPhone’s **Trust This Computer** prompt when shown. Select the iPhone as Xcode’s run destination.
7. When prompted, enable **Settings → Privacy & Security → Developer Mode** on the iPhone, restart, and confirm. This switch may appear only after pairing starts.
8. Press **Run** in Xcode. The app has compiled in the cloud, but local signing and installation still need to be completed and verified for your phone.

The setup helper checks for full Xcode and the iOS SDK, then opens the project. It does not sign in, install Xcode, select a team, accept agreements, or claim to install the app automatically.

## Free account limits

Apple’s Personal Team provisioning profiles expire seven days after issuance. Rebuild and reinstall through Xcode after expiration. Apple currently limits Personal Teams to three installed development apps per device, three registered test devices, and ten App IDs, with the documented seven-day limits. See [Apple’s account overview](https://developer.apple.com/help/account/basics/about-your-developer-account/).

ReelGuard uses automatic signing and no paid-only app capabilities. The project does not ask for your Apple ID credentials in code or store signing secrets.

## What gets installed

This is the separate Instagram website client described in `README.md`. It cannot disable Reels in the official Instagram app. Its URL policy and synthetic DOM filter checks passed locally, but live Instagram compatibility and a complete iOS device build remain unverified.

References: [Xcode device signing](https://developer.apple.com/documentation/xcode/building-and-running-an-app), [Developer Mode](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device).
