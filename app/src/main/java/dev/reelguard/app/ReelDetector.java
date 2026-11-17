package dev.reelguard.app;

/** Pure metadata rule; no text, descriptions, screenshots, or network access. */
public final class ReelDetector {
    public static final String INSTAGRAM = "com.instagram.android";
    public static final String PLAYER_ID = INSTAGRAM + ":id/clips_viewer_view_pager";

    private ReelDetector() {}

    public static boolean isBlockingPlayer(String packageName, String resourceId,
            boolean visible, int left, int top, int right, int bottom,
            int screenWidth, int screenHeight) {
        if (!INSTAGRAM.equals(packageName) || !PLAYER_ID.equals(resourceId) || !visible
                || screenWidth <= 0 || screenHeight <= 0) return false;
        long width = Math.max(0, Math.min(right, screenWidth) - Math.max(left, 0));
        long height = Math.max(0, Math.min(bottom, screenHeight) - Math.max(top, 0));
        // Intersection, not declared bounds: preloaded offscreen players must never block Stories.
        return width >= screenWidth * 0.8 && height >= screenHeight * 0.55;
    }
}
