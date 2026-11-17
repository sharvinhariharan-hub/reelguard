package dev.reelguard.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private LinearLayout content;
    private TextView status;
    private Switch toggle;
    private boolean rendering;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(245, 244, 239));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        content.setPadding(pad, pad * 2, pad, pad);
        scroll.addView(content);
        setContentView(scroll);
        text("REELGUARD  /  ANDROID PROTOTYPE", 12);
        text("Your attention.\nYour choice.", 36);
        text("Keep Instagram. Step out of the Reels loop.", 18);
        status = text("", 16);
        toggle = new Switch(this);
        toggle.setText(R.string.block_reels);
        toggle.setTextSize(22);
        toggle.setMinHeight(dp(72));
        content.addView(toggle, new LinearLayout.LayoutParams(-1, -2));
        toggle.setOnCheckedChangeListener((button, checked) -> {
            if (rendering) return;
            if (checked && !GuardPreferences.consented(this)) {
                refresh();
                showDisclosure();
            } else {
                GuardPreferences.setEnabled(this, checked);
                refresh();
            }
        });
        button("1. Review accessibility access", this::showDisclosure);
        button("2. Open accessibility settings", () -> {
            if (!GuardPreferences.consented(this)) showDisclosure();
            else openAccessibility();
        });
        if (Build.VERSION.SDK_INT >= 33) {
            button("Add on/off tile", () -> getSystemService(StatusBarManager.class)
                .requestAddTileService(new ComponentName(this, GuardTileService.class),
                    "Block Reels", android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_guard),
                    getMainExecutor(), result -> {}));
        }
        text("WHAT THIS VERSION DOES", 12);
        text("When a supported full-screen Reels player is detected, ReelGuard covers it and tries to return you to the previous screen. Reels opened from DMs are blocked too.", 16);
        text("Posts, messages, profiles and Stories are outside the blocking rule. Inline Reels and previews in the Home feed are not blocked in this prototype.", 16);
        text("Always in your control", 22);
        text("Turn blocking off here, on the blocking screen, or with the Quick Settings tile. You can disable the accessibility service in Android settings at any time.", 16);
        text("Device testing required", 22);
        text("Instagram changes its interface. This prototype has not been validated against a live Instagram session. A frame or sound may play before detection. If going back fails, audio may continue beneath the cover. It does not disable Instagram’s server-side Reels feature.", 16);
        text("Private by design", 22);
        text("No internet permission. No message text, screenshots, analytics or accounts. Detection uses visible interface identifiers and screen bounds only.", 16);
        button("Revoke consent and stop", () -> {
            GuardPreferences.setEnabled(this, false);
            GuardPreferences.store(this).edit().putBoolean("consent", false).apply();
            refresh();
        });
        text("0.1.0 · Open-source native Android prototype", 12);
    }

    @Override public void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        if (toggle == null) return;
        rendering = true;
        toggle.setChecked(GuardPreferences.enabled(this));
        rendering = false;
        boolean service = serviceEnabled(this);
        status.setText(!GuardPreferences.enabled(this) ? "Blocking is off"
            : service ? "Blocking requested · accessibility enabled"
            : "Setup needed · enable accessibility to start");
    }

    static boolean serviceEnabled(android.content.Context context) {
        AccessibilityManager manager = context.getSystemService(AccessibilityManager.class);
        ComponentName target = new ComponentName(context, ReelBlockService.class);
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (target.equals(ComponentName.unflattenFromString(info.getId()))) return true;
        }
        return false;
    }

    private void showDisclosure() {
        new AlertDialog.Builder(this).setTitle("Allow Reels detection?")
            .setMessage("ReelGuard uses Android Accessibility to inspect Instagram’s interface identifiers, visibility and screen bounds. Android grants broad screen access, but this implementation does not read message text or content descriptions, take screenshots, save screen contents, or transmit data.\n\nWhen it detects the full-screen Reels player, it covers it and performs Back. This includes Reels opened from DMs. You can switch blocking off at any time.\n\nDo you consent to this use?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("I agree", (dialog, which) -> {
                GuardPreferences.store(this).edit().putBoolean("consent", true).apply();
                GuardPreferences.setEnabled(this, true);
                refresh();
                openAccessibility();
            }).show();
    }

    private void openAccessibility() { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.rgb(28, 49, 43));
        view.setPadding(0, dp(12), 0, dp(12));
        content.addView(view);
        return view;
    }
    private void button(String title, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setMinHeight(dp(52));
        content.addView(button, new LinearLayout.LayoutParams(-1, -2));
        button.setOnClickListener(view -> action.run());
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
