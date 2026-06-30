package com.habittracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class HabitReminderReceiver extends BroadcastReceiver {
    static final String ACTION_REMIND = "com.habittracker.REMIND";
    static final String EXTRA_HABIT_ID = "habit_id";
    private static final String CHANNEL_ID = "habit_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_REMIND.equals(intent.getAction())) {
            return;
        }
        String habitId = intent.getStringExtra(EXTRA_HABIT_ID);
        Habit habit = new HabitStore(context).getHabit(habitId);
        if (habit == null) {
            return;
        }
        showReminder(context, habit);
        HabitReminderScheduler.schedule(context, habit);
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Habit reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showReminder(Context context, Habit habit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ensureChannel(context);
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                habit.id.hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Habit reminder")
                .setContentText("Time to work on " + habit.title)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(("reminder_" + habit.id).hashCode(), builder.build());
        }
    }
}
