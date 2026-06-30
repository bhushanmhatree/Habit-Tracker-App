package com.habittracker;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class HealthConnectUi {
    private HealthConnectUi() {
    }

    static LinearLayout screen(Activity activity, String title, String subtitle, String section, String body) {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(activity, 22), dp(activity, 34), dp(activity, 22), dp(activity, 22));
        root.setBackgroundColor(Color.rgb(246, 247, 250));

        root.addView(text(activity, title, 30, true, Color.rgb(23, 25, 31)));
        TextView caption = text(activity, subtitle, 14, false, Color.rgb(106, 113, 125));
        caption.setPadding(0, dp(activity, 6), 0, dp(activity, 18));
        root.addView(caption);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 18), dp(activity, 18), dp(activity, 18), dp(activity, 18));
        card.setBackground(rounded(Color.WHITE, dp(activity, 26)));
        card.addView(text(activity, section, 20, true, Color.rgb(23, 25, 31)));
        TextView copy = text(activity, body, 14, false, Color.rgb(106, 113, 125));
        copy.setPadding(0, dp(activity, 8), 0, dp(activity, 16));
        card.addView(copy);
        Button done = new Button(activity);
        done.setText("Done");
        done.setAllCaps(false);
        done.setTextColor(Color.WHITE);
        done.setBackground(rounded(Color.rgb(22, 141, 255), dp(activity, 22)));
        setFont(activity, done, true);
        done.setOnClickListener(v -> activity.finish());
        card.addView(done, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48)));
        root.addView(card);
        return root;
    }

    private static TextView text(Activity activity, String value, int size, boolean bold, int color) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        setFont(activity, view, bold);
        return view;
    }

    private static void setFont(Activity activity, TextView view, boolean bold) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setTypeface(activity.getResources().getFont(bold ? R.font.poppins_semibold : R.font.poppins_regular));
        } else if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    private static GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
