package com.habittracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SyncSettingsActivity extends Activity {
    private static final int BG = Color.rgb(246, 247, 250);
    private static final int TEXT = Color.rgb(23, 25, 31);
    private static final int MUTED = Color.rgb(106, 113, 125);
    private static final int BLUE = Color.rgb(22, 141, 255);
    private static final int SOFT_BLUE = Color.rgb(232, 244, 255);

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(34), dp(22), dp(22));
        root.setBackgroundColor(BG);

        root.addView(text("Sync", 30, true, TEXT));
        TextView subtitle = text("Connect health services after privacy policy, consent screens, and production credentials are ready.", 14, false, MUTED);
        subtitle.setPadding(0, dp(6), 0, dp(18));
        root.addView(subtitle);

        root.addView(syncCard("Google Health Connect", "Prepare health permission review and open Health Connect settings.", "Open Health Connect", v -> openHealthConnect()));
        root.addView(syncCard("Strava", "Create a Strava API app, then connect OAuth and activity sync.", "Open Strava API", v -> openUrl("https://www.strava.com/settings/api")));

        setContentView(root);
    }

    private LinearLayout syncCard(String title, String body, String action, android.view.View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(Color.WHITE, dp(26)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        card.addView(text(title, 20, true, TEXT));
        TextView copy = text(body, 14, false, MUTED);
        copy.setPadding(0, dp(8), 0, dp(14));
        card.addView(copy);

        Button button = new Button(this);
        button.setText(action);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(BLUE);
        button.setBackground(rounded(SOFT_BLUE, dp(22)));
        setFont(button, true);
        button.setOnClickListener(listener);
        card.addView(button, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        return card;
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

    private TextView text(String text, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        setFont(view, bold);
        return view;
    }

    private void setFont(TextView view, boolean bold) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setTypeface(getResources().getFont(bold ? R.font.poppins_semibold : R.font.poppins_regular));
        } else if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
