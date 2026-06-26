package com.habittracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int REQUEST_ACTIVITY = 10;
    private static final int REQUEST_NOTIFICATIONS = 11;
    private static final String CHANNEL_ID = "habit_completion";
    private static final int BG = Color.rgb(246, 247, 250);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(23, 25, 31);
    private static final int MUTED = Color.rgb(106, 113, 125);
    private static final int BLUE = Color.rgb(22, 141, 255);
    private static final int GREEN = Color.rgb(13, 177, 116);
    private static final int SOFT_BLUE = Color.rgb(232, 244, 255);
    private static final int SOFT_GREEN = Color.rgb(226, 249, 239);

    private HabitStore store;
    private LinearLayout list;
    private SensorManager sensorManager;
    private Sensor stepCounter;
    private Habit activeAutomaticHabit;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        store = new HabitStore(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounter = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        createNotificationChannel();
        requestNotificationPermission();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeAutomaticHabit != null) {
            startStepSensor();
        }
        refreshHabits();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(34), dp(20), dp(18));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.addView(text("Today", 14, false, MUTED));
        titleBlock.addView(text("Habit Tracker", 30, true, TEXT));
        header.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button sync = actionButton("Sync", false);
        sync.setOnClickListener(v -> startActivity(new Intent(this, SyncSettingsActivity.class)));
        header.addView(sync);
        root.addView(header);

        root.addView(summaryCard());

        Button add = actionButton("Create habit", true);
        add.setOnClickListener(v -> showAddDialog());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        addParams.setMargins(0, dp(16), 0, dp(8));
        root.addView(add, addParams);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
        refreshHabits();
    }

    private View summaryCard() {
        LinearLayout card = card();
        List<Habit> habits = store.getHabits();
        int completed = 0;
        int totalScore = 0;
        for (Habit habit : habits) {
            if (habit.completed) {
                completed++;
            }
            totalScore += Math.min(100, Math.round((habit.progress * 100f) / Math.max(1, habit.target)));
        }

        card.addView(text("Daily score", 15, false, MUTED));
        card.addView(text(habits.isEmpty() ? "0" : String.valueOf(totalScore / Math.max(1, habits.size())), 42, true, TEXT));
        card.addView(text(completed + " completed - " + habits.size() + " active habits", 14, false, MUTED));
        return card;
    }

    private void refreshHabits() {
        if (list == null) {
            return;
        }
        list.removeAllViews();
        List<Habit> habits = store.getHabits();
        if (habits.isEmpty()) {
            LinearLayout empty = card();
            empty.addView(text("No habits yet", 20, true, TEXT));
            TextView copy = text("Create a walk, hydration, checklist, or focus habit and track it manually or with phone sensors.", 14, false, MUTED);
            copy.setPadding(0, dp(8), 0, 0);
            empty.addView(copy);
            list.addView(empty);
            return;
        }
        for (Habit habit : habits) {
            list.addView(habitCard(habit));
        }
        HabitWidgetProvider.updateAll(this);
    }

    private View habitCard(Habit habit) {
        LinearLayout card = card();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(text(habit.title, 21, true, TEXT));
        String automaticLabel = habit.automatic ? "Auto step tracking" : "Manual tracking";
        copy.addView(text(automaticLabel + " - every " + habit.intervalHours + " hours", 13, false, MUTED));
        top.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(statusPill(habit.completed ? "Done" : "Open", habit.completed));
        card.addView(top);

        int percent = Math.min(100, Math.round((habit.progress * 100f) / Math.max(1, habit.target)));
        TextView progress = text(habit.progress + " / " + habit.target + " " + habit.unit, 28, true, TEXT);
        progress.setPadding(0, dp(14), 0, dp(6));
        card.addView(progress);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(percent);
        card.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        metrics.setPadding(0, dp(14), 0, dp(4));
        metrics.addView(metric("Target", habit.target + " " + habit.unit), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        metrics.addView(metric("Streak", habit.completed ? "1 day" : "0 days"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        metrics.addView(metric("Score", percent + " pts"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        card.addView(metrics);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(10), 0, 0);

        Button log = actionButton("Log progress", false);
        log.setOnClickListener(v -> {
            store.addProgress(habit.id, 1);
            Habit updated = store.getHabit(habit.id);
            if (updated != null && updated.completed) {
                notifyCompleted(updated);
            }
            render();
        });
        actions.addView(log, new LinearLayout.LayoutParams(0, dp(46), 1));

        Button reset = actionButton("Reset", false);
        reset.setOnClickListener(v -> {
            habit.progress = 0;
            habit.completed = false;
            habit.baselineSteps = 0;
            store.updateHabit(habit);
            render();
        });
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        resetParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(reset, resetParams);

        if (habit.automatic) {
            Button track = actionButton(activeAutomaticHabit != null && activeAutomaticHabit.id.equals(habit.id) ? "Tracking steps" : "Start step tracking", true);
            track.setOnClickListener(v -> startAutomaticHabit(habit));
            LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(0, dp(46), 1);
            trackParams.setMargins(dp(10), 0, 0, 0);
            actions.addView(track, trackParams);
        }

        card.addView(actions);
        return card;
    }

    private LinearLayout metric(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text(label, 12, false, MUTED));
        box.addView(text(value, 14, true, TEXT));
        return box;
    }

    private TextView statusPill(String value, boolean completed) {
        TextView pill = text(value, 12, true, completed ? GREEN : BLUE);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(12), dp(7), dp(12), dp(7));
        pill.setBackground(rounded(completed ? SOFT_GREEN : SOFT_BLUE, dp(18)));
        return pill;
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(8), dp(16), 0);

        EditText title = field("Habit name");
        EditText target = field("Target amount, e.g. 1000");
        target.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText unit = field("Unit, e.g. steps");
        EditText interval = field("Frequency in hours, e.g. 2");
        interval.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        CheckBox automatic = new CheckBox(this);
        automatic.setText("Track walking automatically with step sensor");
        automatic.setTextColor(TEXT);
        setFont(automatic, false);

        form.addView(title);
        form.addView(target);
        form.addView(unit);
        form.addView(interval);
        form.addView(automatic);

        new AlertDialog.Builder(this)
                .setTitle("Create habit")
                .setView(form)
                .setPositiveButton("Create", (dialog, which) -> {
                    String habitTitle = title.getText().toString().trim();
                    String habitUnit = unit.getText().toString().trim();
                    int habitTarget = parsePositive(target.getText().toString(), 1);
                    int habitInterval = parsePositive(interval.getText().toString(), 24);
                    if (habitTitle.isEmpty()) {
                        habitTitle = "Untitled habit";
                    }
                    if (habitUnit.isEmpty()) {
                        habitUnit = automatic.isChecked() ? "steps" : "count";
                    }
                    store.createHabit(habitTitle, habitUnit, habitTarget, habitInterval, automatic.isChecked());
                    render();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startAutomaticHabit(Habit habit) {
        if (stepCounter == null) {
            Toast.makeText(this, "This phone does not expose a step counter sensor.", Toast.LENGTH_LONG).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_ACTIVITY);
            activeAutomaticHabit = habit;
            return;
        }
        activeAutomaticHabit = habit;
        startStepSensor();
        Toast.makeText(this, "Step tracking started. Keep the phone with you.", Toast.LENGTH_SHORT).show();
        render();
    }

    private void startStepSensor() {
        if (sensorManager != null && stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activeAutomaticHabit == null || event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }
        float totalSteps = event.values[0];
        Habit habit = store.getHabit(activeAutomaticHabit.id);
        if (habit == null) {
            return;
        }
        if (habit.baselineSteps <= 0) {
            habit.baselineSteps = totalSteps;
        }
        habit.progress = Math.max(habit.progress, Math.round(totalSteps - habit.baselineSteps));
        boolean wasCompleted = habit.completed;
        habit.completed = habit.progress >= habit.target;
        store.updateHabit(habit);
        activeAutomaticHabit = habit;
        if (!wasCompleted && habit.completed) {
            notifyCompleted(habit);
            sensorManager.unregisterListener(this);
        }
        render();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepSensor();
        }
    }

    private void notifyCompleted(Habit habit) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        android.app.Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(this, CHANNEL_ID)
                : new android.app.Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Habit complete")
                .setContentText(habit.title + " reached " + habit.target + " " + habit.unit)
                .setAutoCancel(true);
        manager.notify(habit.id.hashCode(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Habit completion",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(CARD, dp(26)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(14), 0, 0);
        card.setLayoutParams(params);
        return card;
    }

    private Button actionButton(String value, boolean filled) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(filled ? Color.WHITE : BLUE);
        button.setBackground(rounded(filled ? BLUE : SOFT_BLUE, dp(22)));
        setFont(button, true);
        return button;
    }

    private EditText field(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setSingleLine(true);
        field.setTextColor(TEXT);
        field.setHintTextColor(MUTED);
        setFont(field, false);
        return field;
    }

    private TextView text(String value, int sp, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setLetterSpacing(0);
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

    private int parsePositive(String raw, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(raw.trim()));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
