package com.habittracker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class HabitWidgetConfigureActivity extends Activity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

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
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(Color.WHITE);

        TextView title = label("Select habit", 26, true);
        root.addView(title);

        List<Habit> habits = new HabitStore(this).getHabits();
        if (habits.isEmpty()) {
            TextView empty = label("Create a habit in the app first.", 16, false);
            empty.setPadding(0, 24, 0, 0);
            root.addView(empty);
        }
        for (Habit habit : habits) {
            TextView row = label(habit.title + "\n" + habit.progress + " / " + habit.target + " " + habit.unit, 18, false);
            row.setPadding(0, 24, 0, 24);
            row.setOnClickListener(v -> finishWithHabit(habit.id));
            root.addView(row);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(root);
        setContentView(scrollView);
    }

    private void finishWithHabit(String habitId) {
        HabitStore store = new HabitStore(this);
        store.saveWidgetHabit(appWidgetId, habitId);
        HabitWidgetProvider.updateWidget(this, AppWidgetManager.getInstance(this), appWidgetId);

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private TextView label(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.BLACK);
        view.setTextSize(size);
        if (bold) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }
}
