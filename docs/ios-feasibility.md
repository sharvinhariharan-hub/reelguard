# iPhone: current feasibility boundary

The requested behavior is selective blocking inside the official native Instagram app, while preserving posts and DMs. There is no supported public iOS API for a third-party app to inspect Instagram’s view hierarchy and cover just its Reels player.

Apple sandboxes apps. Managed Settings and Family Controls provide app/category/website shielding, not selectors for arbitrary features inside another app. Safari extensions affect web pages, not Instagram’s native app. A VPN or DNS filter cannot reliably distinguish Reels from posts and DM media using shared encrypted delivery infrastructure. This is an architectural limitation, not a missing permission in this project.

The repository now includes a separate SwiftUI/WebKit app prototype under `ios/`. It opens Instagram’s website inside its own window and filters identifiable Reels. It does not control the official Instagram app, and does not satisfy the original requirement of selectively blocking that app’s native player. See `ios/README.md` for implementation and build status.

Possible changes of scope, requiring a product decision:

1. An iPhone focus companion could block the entire Instagram app with Screen Time authorization. It would also block posts and DMs, failing the current preservation requirement.
2. A Safari extension could support Instagram’s website. It would fail the native-app requirement.
3. An official Meta feature or future sanctioned integration could expose selective controls; no such integration is assumed here.

A modified client or jailbreak approach is outside this prototype. Neither gives a practical equivalent to a normal, maintained App Store companion.

References: [iOS runtime security](https://support.apple.com/en-ie/guide/security-pdf/sec15bfe098e/1/web/1), [ShieldSettings](https://developer.apple.com/documentation/managedsettings/shieldsettings), [Safari extensions](https://developer.apple.com/documentation/safariservices/safari-web-extensions).
