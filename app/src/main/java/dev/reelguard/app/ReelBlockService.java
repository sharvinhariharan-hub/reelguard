package dev.reelguard.app;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public final class ReelBlockService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlay;
    private boolean connected;
    private long lastScan;
    private long lastBack;
    private int visited;
    private WindowManager windows;
    private SharedPreferences preferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (store, key) -> handler.post(this::check);
    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            if (!connected) return;
            check();
            // Also detect enabling the switch while an unchanged Reel viewer is already open.
            handler.postDelayed(this, overlay == null ? 700 : 200);
        }
    };
    private final Runnable delayedCheck = this::check;

    @Override protected void onServiceConnected() {
        connected = true;
        windows = getSystemService(WindowManager.class);
        preferences = GuardPreferences.store(this);
        preferences.registerOnSharedPreferenceChangeListener(listener);
        GuardTileService.refresh(this);
        handler.removeCallbacks(watchdog);
        handler.post(watchdog);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!connected) return;
        long elapsed = SystemClock.uptimeMillis() - lastScan;
        handler.removeCallbacks(delayedCheck);
        if (elapsed >= 100) check();
        else handler.postDelayed(delayedCheck, 100 - elapsed);
    }

    private void check() {
        if (!connected) return;
        lastScan = SystemClock.uptimeMillis();
        if (!GuardPreferences.enabled(this)) { hide(); return; }
        boolean blocked = currentPlayerBlocked();
        if (!blocked) { hide(); return; }
        if (overlay == null) {
            show();
            goBack();
        }
    }

    private boolean currentPlayerBlocked() {
        AccessibilityNodeInfo root = foregroundApplication();
        if (root == null) return false;
        try {
            if (!ReelDetector.INSTAGRAM.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) return false;
            DisplayMetrics metrics = new DisplayMetrics();
            windows.getDefaultDisplay().getRealMetrics(metrics);
            visited = 0;
            return scan(root, metrics.widthPixels, metrics.heightPixels, 0);
        } finally { root.recycle(); }
    }

    private AccessibilityNodeInfo foregroundApplication() {
        // The focused application below our accessibility overlay remains inspectable.
        List<AccessibilityWindowInfo> list = getWindows();
        AccessibilityNodeInfo result = null;
        for (AccessibilityWindowInfo window : list) {
            if (result == null && window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION
                    && (window.isFocused() || window.isActive())) result = window.getRoot();
            window.recycle();
        }
        return result;
    }

    private boolean scan(AccessibilityNodeInfo node, int width, int height, int depth) {
        if (++visited > 1800 || depth > 64 || !node.isVisibleToUser()) return false;
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        CharSequence pkg = node.getPackageName();
        if (ReelDetector.isBlockingPlayer(pkg == null ? "" : pkg.toString(), node.getViewIdResourceName(),
                true, rect.left, rect.top, rect.right, rect.bottom, width, height)) return true;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            try { if (scan(child, width, height, depth + 1)) return true; }
            finally { child.recycle(); }
            if (visited > 1800) break;
        }
        return false;
    }

    private void goBack() {
        long now = SystemClock.uptimeMillis();
        // Revalidate just before a global action; never send Back to another app.
        if (now - lastBack < 800 || !GuardPreferences.enabled(this) || !currentPlayerBlocked()) return;
        lastBack = now;
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    private void show() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(48, 48, 48, 48);
        panel.setBackgroundColor(Color.rgb(245, 244, 239));
        TextView title = new TextView(this);
        title.setText(R.string.reels_blocked);
        title.setTextSize(30);
        title.setTextColor(Color.rgb(24, 97, 79));
        panel.addView(title);
        TextView detail = new TextView(this);
        detail.setText("Return to your posts and conversations.\nYou can turn blocking off whenever you choose.");
        detail.setTextSize(17);
        detail.setTextColor(Color.DKGRAY);
        detail.setPadding(0, 32, 0, 32);
        panel.addView(detail);
        Button back = new Button(this);
        back.setText(R.string.go_back);
        back.setOnClickListener(view -> goBack());
        panel.addView(back);
        Button off = new Button(this);
        off.setText(R.string.turn_off);
        off.setOnClickListener(view -> { GuardPreferences.setEnabled(this, false); hide(); });
        panel.addView(off);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE);
        params.setTitle("ReelGuard Reels blocker");
        try { windows.addView(panel, params); overlay = panel; }
        catch (WindowManager.BadTokenException | IllegalStateException exception) { overlay = null; }
    }

    private void hide() {
        if (overlay == null) return;
        try { windows.removeView(overlay); }
        catch (IllegalArgumentException ignored) { /* Window already removed by the system. */ }
        overlay = null;
    }
    @Override public void onInterrupt() { hide(); }
    @Override public void onDestroy() {
        connected = false;
        handler.removeCallbacksAndMessages(null);
        if (preferences != null) preferences.unregisterOnSharedPreferenceChangeListener(listener);
        hide();
        super.onDestroy();
    }
}
