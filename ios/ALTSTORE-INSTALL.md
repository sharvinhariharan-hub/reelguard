# Install the downloadable iPhone IPA with a personal Apple ID

The downloadable `ReelGuard-unsigned.ipa` is a real iPhone app package, not a simulator build. It must be signed for your phone before iOS can run it. **Downloading it in Safari does not by itself install it.** AltStore Classic and AltServer can perform personal Apple ID signing without installing full Xcode on your Mac.

1. Install **AltServer** on your Mac and use it to install **AltStore Classic** on the connected, unlocked iPhone. Follow [AltStore’s official Mac instructions](https://faq.altstore.io/altstore-classic/how-to-install-altstore-macos). Pair/trust the phone and enable Developer Mode when required. Enter your account credentials only in the signing tool’s own sign-in interface, never in this project or chat.
2. Keep AltServer running with the Mac and iPhone on the same Wi-Fi, or keep the phone connected by USB.
3. Download `ReelGuard-unsigned.ipa` from this project’s GitHub release onto the iPhone. Save it to Files.
4. In **AltStore Classic → My Apps**, use the **+** button and select the downloaded IPA. Follow the signing prompts to install it with your personal Apple ID.
5. Open ReelGuard and read its intro. Sign in on Instagram’s webpage inside the app.

Free signing expires after seven days. AltStore can refresh apps while AltServer is reachable. See [AltServer and refreshing](https://faq.altstore.io/altstore-classic/altserver) and [AltStore’s getting-started guide](https://faq.altstore.io/altstore-classic/your-altstore).

This installation path has not yet been verified on the user’s iOS 27 phone. Use current AltStore/AltServer versions and consult their compatibility/troubleshooting guidance if Apple signing rejects the device or account. No sideloading restriction bypasses, jailbreaks, or third-party signing certificates are used by this project.

This app opens Instagram’s website in its own native window. It does not remove or block Reels in the official Instagram app. Live Instagram login and exact current markup require phone testing; this alpha cannot guarantee complete Reel blocking.
