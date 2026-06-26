package com.habittracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SyncSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(Color.WHITE);

        TextView title = text("Sync", 30, true);
        root.addView(title);
        root.addView(text("Connectors are prepared for release setup. Add production credentials and consent screens before publishing sync.", 16, false));

        Button health = button("Open Health Connect");
        health.setOnClickListener(v -> openHealthConnect());
        root.addView(health);

        Button strava = button("Open Strava Setup");
        strava.setOnClickListener(v -> openUrl("https://www.strava.com/settings/api"));
        root.addView(strava);

        setContentView(root);
    }

    private void openHealthConnect() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.healthdata");
        if (intent == null) {
            intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
        }
        startActivity(intent);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private TextView text(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.BLACK);
        view.setTextSize(size);
        view.setPadding(0, 0, 0, 24);
        if (bold) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 0);
        button.setLayoutParams(params);
        return button;
    }
}
