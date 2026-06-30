package com.habittracker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class HabitWidgetConfigureActivity extends Activity {
    private static final String[] MODES = {"target", "checklist", "streak", "score", "frequency"};
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String selectedHabitId;
    private String selectedMode = "target";
    private LinearLayout habitsList;
    private LinearLayout modesList;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setResult(RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(30), dp(22), dp(24));
        root.setBackgroundColor(Color.rgb(246, 247, 250));

        root.addView(label("Widget setup", 28, true, Color.rgb(23, 25, 31)));
        TextView caption = label("Choose one habit and how the widget should display it.", 14, false, Color.rgb(106, 113, 125));
        caption.setPadding(0, dp(4), 0, dp(18));
        root.addView(caption);

        habitsList = new LinearLayout(this);
        habitsList.setOrientation(LinearLayout.VERTICAL);
        root.addView(section("Habit", habitsList));

        modesList = new LinearLayout(this);
        modesList.setOrientation(LinearLayout.VERTICAL);
        root.addView(section("Widget option", modesList));

        Button save = button("Save widget", true);
        save.setOnClickListener(v -> finishWithSelection());
        root.addView(save);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(root);
        setContentView(scrollView);
        renderHabits();
        renderModes();
    }

    private void renderHabits() {
        habitsList.removeAllViews();
        List<Habit> habits = new HabitStore(this).getHabits();
        if (habits.isEmpty()) {
            TextView empty = label("Create a habit in the app first.", 15, false, Color.rgb(106, 113, 125));
            empty.setPadding(0, dp(8), 0, dp(8));
            habitsList.addView(empty);
            return;
        }
        if (selectedHabitId == null) {
            selectedHabitId = habits.get(0).id;
        }
        for (Habit habit : habits) {
            Button row = button(habit.title + "  " + habit.progress + "/" + habit.target, habit.id.equals(selectedHabitId));
            row.setOnClickListener(v -> {
                selectedHabitId = habit.id;
                renderHabits();
            });
            habitsList.addView(row);
        }
    }

    private void renderModes() {
        modesList.removeAllViews();
        for (String mode : MODES) {
            Button row = button(modeLabel(mode), mode.equals(selectedMode));
            row.setOnClickListener(v -> {
                selectedMode = mode;
                renderModes();
            });
            modesList.addView(row);
        }
    }

    private void finishWithSelection() {
        if (selectedHabitId == null) {
            return;
        }
        HabitStore store = new HabitStore(this);
        store.saveWidgetHabit(appWidgetId, selectedHabitId);
        store.saveWidgetMode(appWidgetId, selectedMode);
        HabitWidgetProvider.updateAll(this);

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private LinearLayout section(String title, LinearLayout content) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(12));
        card.setBackground(rounded(Color.WHITE, dp(24)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        card.addView(label(title, 16, true, Color.rgb(23, 25, 31)));
        card.addView(content);
        return card;
    }

    private Button button(String label, boolean selected) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(selected ? Color.WHITE : Color.rgb(23, 25, 31));
        button.setBackground(rounded(selected ? Color.rgb(22, 141, 255) : Color.rgb(240, 246, 255), dp(20)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            button.setTypeface(getResources().getFont(selected ? R.font.poppins_semibold : R.font.poppins_regular));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        params.setMargins(0, dp(10), 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private TextView label(String text, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setTypeface(getResources().getFont(bold ? R.font.poppins_semibold : R.font.poppins_regular));
        } else if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private String modeLabel(String mode) {
        if ("checklist".equals(mode)) {
            return "Checklist";
        }
        if ("streak".equals(mode)) {
            return "Streak";
        }
        if ("score".equals(mode)) {
            return "Score";
        }
        if ("frequency".equals(mode)) {
            return "Frequency";
        }
        return "Target";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
