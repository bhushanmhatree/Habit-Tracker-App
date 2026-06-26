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
            updateWidget(context, manager, id);
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
        ComponentName component = new ComponentName(context, HabitWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        HabitStore store = new HabitStore(context);
        String habitId = store.getWidgetHabit(widgetId);
        Habit habit = habitId == null ? null : store.getHabit(habitId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.habit_widget);
        if (habit == null) {
            views.setTextViewText(R.id.widget_title, "Choose habit");
            views.setTextViewText(R.id.widget_progress, "Tap to open");
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context));
        } else {
            views.setTextViewText(R.id.widget_title, habit.title);
            views.setTextViewText(R.id.widget_progress, habit.progress + " / " + habit.target + " " + habit.unit);
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

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
