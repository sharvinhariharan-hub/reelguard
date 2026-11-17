package dev.reelguard.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReelDetectorTest {
    private boolean detect(String pkg, String id, boolean visible, int l, int t, int r, int b) {
        return ReelDetector.isBlockingPlayer(pkg, id, visible, l, t, r, b, 1080, 2400);
    }
    @Test public void blocksVisiblePlayer() { assertTrue(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 0, 100, 1080, 2200)); }
    @Test public void ignoresHiddenPreloadedPlayer() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, false, 0, 0, 1080, 2400)); }
    @Test public void ignoresOffscreenPlayerEvenIfReportedVisible() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 1080, 0, 2160, 2400)); }
    @Test public void ignoresMostlyOffscreenPlayer() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 900, 0, 1980, 2400)); }
    @Test public void ignoresThumbnail() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 0, 0, 360, 400)); }
    @Test public void preservesStories() { assertFalse(detect(ReelDetector.INSTAGRAM, "com.instagram.android:id/reel_viewer_container", true, 0, 0, 1080, 2400)); }
    @Test public void preservesDmsAndPhotoFeed() {
        for (String id : new String[]{"direct_thread_view", "row_feed_photo", "clips_tab", "profile_tab"})
            assertFalse(detect(ReelDetector.INSTAGRAM, "com.instagram.android:id/" + id, true, 0, 0, 1080, 2400));
    }
    @Test public void ignoresOtherPackages() { assertFalse(detect("com.example", ReelDetector.PLAYER_ID, true, 0, 0, 1080, 2400)); }
    @Test public void nullMetadataIsAllowed() { assertFalse(detect(null, null, true, 0, 0, 1080, 2400)); }
    @Test public void invalidScreenSizeIsAllowed() { assertFalse(ReelDetector.isBlockingPlayer(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 0, 0, 1080, 2400, 0, 0)); }
    @Test public void largeBoundsMustIntersectScreen() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, -5000, -5000, -1, -1)); }
    @Test public void partialHeightDuringTransitionIsAllowed() { assertFalse(detect(ReelDetector.INSTAGRAM, ReelDetector.PLAYER_ID, true, 0, 2000, 1080, 4400)); }
}
