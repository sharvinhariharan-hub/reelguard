package dev.reelguard.app;

import android.content.ComponentName;
import android.content.Context;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public final class GuardTileService extends TileService {
    static void refresh(Context context) {
        requestListeningState(context, new ComponentName(context, GuardTileService.class));
    }
    @Override public void onStartListening() { super.onStartListening(); update(); }
    @Override public void onClick() {
        super.onClick();
        if (!GuardPreferences.consented(this) || !MainActivity.serviceEnabled(this)) { update(); return; }
        GuardPreferences.setEnabled(this, !GuardPreferences.enabled(this));
        update();
    }
    private void update() {
        Tile tile = getQsTile();
        if (tile == null) return;
        boolean ready = GuardPreferences.consented(this) && MainActivity.serviceEnabled(this);
        tile.setState(!ready ? Tile.STATE_UNAVAILABLE : GuardPreferences.enabled(this) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel("Block Reels");
        if (android.os.Build.VERSION.SDK_INT >= 29)
            tile.setSubtitle(!ready ? "Set up in ReelGuard" : GuardPreferences.enabled(this) ? "On" : "Off");
        tile.updateTile();
    }
}
