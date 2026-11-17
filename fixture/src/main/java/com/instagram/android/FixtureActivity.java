package com.instagram.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Synthetic emulator fixture. Not Instagram. Never package this with the user-facing APK. */
public final class FixtureActivity extends Activity {
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        recreate();
    }
    @Override public void onBackPressed() {
        if (!getIntent().getBooleanExtra("trap", false)) super.onBackPressed();
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (android.os.Build.VERSION.SDK_INT >= 33 && getIntent().getBooleanExtra("trap", false)) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> {});
        }
        String mode = getIntent().getStringExtra("screen");
        if ("reels".equals(mode)) {
            LinearLayout player = panel("TEST REELS PLAYER");
            player.setId(R.id.clips_viewer_view_pager);
            if (getIntent().getBooleanExtra("offscreen", false)) player.setTranslationX(5000);
            setContentView(player);
        } else if ("story".equals(mode)) {
            FrameLayout frame = new FrameLayout(this);
            LinearLayout story = panel("TEST STORY");
            story.setId(R.id.reel_viewer_container);
            frame.addView(story, new FrameLayout.LayoutParams(-1, -1));
            LinearLayout preload = panel("PRELOADED HIDDEN REELS");
            preload.setId(R.id.clips_viewer_view_pager);
            preload.setTranslationX(5000);
            frame.addView(preload, new FrameLayout.LayoutParams(-1, -1));
            setContentView(frame);
        } else {
            LinearLayout home = panel("dm".equals(mode) ? "TEST DM CONVERSATION" : "TEST PHOTO FEED");
            addButton(home, "Open Reels", "reels");
            addButton(home, "Open DMs", "dm");
            addButton(home, "Open Story", "story");
            setContentView(home);
        }
    }
    private LinearLayout panel(String title) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        TextView label = new TextView(this);
        label.setText(title);
        label.setTextSize(24);
        layout.addView(label);
        return layout;
    }
    private void addButton(LinearLayout layout, String title, String screen) {
        Button button = new Button(this);
        button.setText(title);
        button.setOnClickListener(view -> startActivity(new Intent(this, FixtureActivity.class).putExtra("screen", screen)));
        layout.addView(button);
    }
}
