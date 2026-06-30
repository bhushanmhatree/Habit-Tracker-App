package com.habittracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class HabitWidgetProvider extends AppWidgetProvider {
    static final String ACTION_ADD_PROGRESS = "com.habittracker.widget.ADD_PROGRESS";
    static final String EXTRA_HABIT_ID = "habit_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_ADD_PROGRESS.equals(intent.getAction())) {
            String habitId = intent.getStringExtra(EXTRA_HABIT_ID);
            if (habitId != null) {
                new HabitStore(context).addProgress(habitId, 1);
                updateAll(context);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            updateWidget(context, manager, id, layoutRes());
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        HabitStore store = new HabitStore(context);
        for (int id : appWidgetIds) {
            store.removeWidgetHabit(id);
        }
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        updateComponent(context, manager, HabitWidgetProvider.class, R.layout.habit_widget);
        updateComponent(context, manager, HabitCompactWidgetProvider.class, R.layout.habit_widget_compact);
        updateComponent(context, manager, HabitWideWidgetProvider.class, R.layout.habit_widget_wide);
    }

    private static void updateComponent(Context context, AppWidgetManager manager, Class<?> provider, int layoutRes) {
        ComponentName component = new ComponentName(context, provider);
        for (int id : manager.getAppWidgetIds(component)) {
            updateWidget(context, manager, id, layoutRes);
        }
    }

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        updateWidget(context, manager, widgetId, R.layout.habit_widget);
    }

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId, int layoutRes) {
        HabitStore store = new HabitStore(context);
        String habitId = store.getWidgetHabit(widgetId);
        String mode = store.getWidgetMode(widgetId);
        Habit habit = habitId == null ? null : store.getHabit(habitId);

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutRes);
        if (habit == null) {
            views.setTextViewText(R.id.widget_title, "Choose habit");
            views.setTextViewText(R.id.widget_metric, "Widget setup");
            views.setTextViewText(R.id.widget_progress, "Tap to open");
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context));
        } else {
            views.setTextViewText(R.id.widget_title, habit.title);
            views.setTextViewText(R.id.widget_metric, widgetMetric(habit, mode));
            views.setTextViewText(R.id.widget_progress, widgetProgress(habit, mode));
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context));
            Intent addIntent = new Intent(context, HabitWidgetProvider.class);
            addIntent.setAction(ACTION_ADD_PROGRESS);
            addIntent.putExtra(EXTRA_HABIT_ID, habit.id);
            PendingIntent addPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_add, addPendingIntent);
        }
        manager.updateAppWidget(widgetId, views);
    }

    protected int layoutRes() {
        return R.layout.habit_widget;
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static String widgetMetric(Habit habit, String mode) {
        if ("checklist".equals(mode)) {
            return habit.completed ? "Checklist done" : "Checklist item";
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

    private static String widgetProgress(Habit habit, String mode) {
        int percent = Math.min(100, Math.round((habit.progress * 100f) / Math.max(1, habit.target)));
        if ("checklist".equals(mode)) {
            return habit.completed ? "Completed" : "Open";
        }
        if ("streak".equals(mode)) {
            return habit.streak + " days";
        }
        if ("score".equals(mode)) {
            return percent + " pts";
        }
        if ("frequency".equals(mode)) {
            return "Every " + habit.intervalHours + "h";
        }
        return habit.progress + " / " + habit.target + " " + habit.unit;
    }
}
