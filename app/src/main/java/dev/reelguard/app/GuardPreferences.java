package dev.reelguard.app;

import android.content.Context;
import android.content.SharedPreferences;

final class GuardPreferences {
    static SharedPreferences store(Context context) {
        return context.getSharedPreferences("guard", Context.MODE_PRIVATE);
    }
    static boolean consented(Context c) { return store(c).getBoolean("consent", false); }
    static boolean enabled(Context c) { return consented(c) && store(c).getBoolean("enabled", false); }
    static void setEnabled(Context c, boolean value) {
        store(c).edit().putBoolean("enabled", value && consented(c)).apply();
        GuardTileService.refresh(c);
    }
}
