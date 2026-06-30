package com.habittracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

final class HabitReminderScheduler {
    private HabitReminderScheduler() {
    }

    static void scheduleAll(Context context) {
        List<Habit> habits = new HabitStore(context).getHabits();
        for (Habit habit : habits) {
            schedule(context, habit);
        }
    }

    static void schedule(Context context, Habit habit) {
        if (habit == null || habit.reminderMinutes <= 0) {
            cancel(context, habit == null ? null : habit.id);
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        long triggerAt = System.currentTimeMillis() + habit.reminderMinutes * 60_000L;
        PendingIntent pendingIntent = reminderIntent(context, habit.id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    static void cancel(Context context, String habitId) {
        if (habitId == null) {
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(reminderIntent(context, habitId));
        }
    }

    private static PendingIntent reminderIntent(Context context, String habitId) {
        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.setAction(HabitReminderReceiver.ACTION_REMIND);
        intent.putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId);
        return PendingIntent.getBroadcast(
                context,
                habitId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
