# Physical-device acceptance checklist

Record phone model, Android version, Instagram version, orientation, language, and date. No user message text or account credentials should be saved.

## Blocking on

- Open Reels from its tab, a Home feed card, Explore, a profile, a saved item, and a direct link. Verify the overlay/back action.
- From a DM conversation, open a shared Reel. Verify it is blocked and returning leaves the conversation accessible.
- Repeat with Reels already open when enabling the switch/tile.
- Verify photo posts, carousels, profile browsing, DM typing, directly uploaded DM video, Stories, Story replies, and search remain usable.
- In particular, open Stories after visiting Reels: Instagram can preload an offscreen player.
- Enter Reels repeatedly and swipe quickly. Note any brief frame/audio leak and failures.

## Recovery and switching

- Turn off from the app, tile, and overlay. Reels should become usable promptly.
- Switch to Home, another application, Recents, lock screen, notification shade, and keyboard. No persistent overlay should cover another app.
- If Back fails, both overlay controls must remain available.
- Disable the accessibility service and revoke in-app consent. Blocking must stop.
- Kill/restart processes and reboot: saved requested state may persist, but accessibility service reconnection must be verified.
- Test Android 8, a recent Android release, landscape, split-screen, picture-in-picture, and a phone with aggressive battery management. Unsupported layouts must be recorded as failures, not counted as passes.

## Release gate

Do not describe the app as “blocks all Reels” until entry points and inline playback are covered and verified. Do not describe iPhone support as implemented. No Play Store publication or public repository creation has been performed by this task.
