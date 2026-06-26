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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int REQUEST_ACTIVITY = 10;
    private static final int REQUEST_NOTIFICATIONS = 11;
    private static final String CHANNEL_ID = "habit_completion";

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
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 44, 28, 24);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = text("Habit Tracker", 30, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        header.addView(title, titleParams);

        Button sync = pill("Sync", false);
        sync.setOnClickListener(v -> startActivity(new Intent(this, SyncSettingsActivity.class)));
        header.addView(sync);
        root.addView(header);

        TextView subtitle = text("Manual habits, step goals, and quiet completion nudges.", 15, false);
        subtitle.setPadding(0, 0, 0, 20);
        root.addView(subtitle);

        Button add = pill("New habit", true);
        add.setOnClickListener(v -> showAddDialog());
        root.addView(add);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);
        refreshHabits();
    }

    private void refreshHabits() {
        if (list == null) {
            return;
        }
        list.removeAllViews();
        List<Habit> habits = store.getHabits();
        if (habits.isEmpty()) {
            TextView empty = text("No habits yet. Start with a walk, water, reading, or anything you want to repeat.", 17, false);
            empty.setPadding(0, 36, 0, 0);
            list.addView(empty);
            return;
        }
        for (Habit habit : habits) {
            list.addView(habitRow(habit));
        }
        HabitWidgetProvider.updateAll(this);
    }

    private View habitRow(Habit habit) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 24, 0, 24);

        TextView name = text(habit.title, 22, true);
        row.addView(name);

        String automaticLabel = habit.automatic ? "auto step tracking" : "manual";
        TextView meta = text(habit.progress + " / " + habit.target + " " + habit.unit + " every " + habit.intervalHours + "h · " + automaticLabel, 15, false);
        row.addView(meta);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, 14, 0, 0);

        Button plus = pill("+1", false);
        plus.setOnClickListener(v -> {
            store.addProgress(habit.id, 1);
            Habit updated = store.getHabit(habit.id);
            if (updated != null && updated.completed) {
                notifyCompleted(updated);
            }
            refreshHabits();
        });
        actions.addView(plus);

        Button reset = pill("Reset", false);
        reset.setOnClickListener(v -> {
            habit.progress = 0;
            habit.completed = false;
            habit.baselineSteps = 0;
            store.updateHabit(habit);
            refreshHabits();
        });
        actions.addView(reset);

        if (habit.automatic) {
            Button track = pill(activeAutomaticHabit != null && activeAutomaticHabit.id.equals(habit.id) ? "Tracking" : "Track steps", true);
            track.setOnClickListener(v -> startAutomaticHabit(habit));
            actions.addView(track);
        }

        row.addView(actions);
        View divider = new View(this);
        divider.setBackgroundColor(Color.BLACK);
        row.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return row;
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(16, 8, 16, 0);

        EditText title = field("Habit name");
        EditText target = field("Target amount, e.g. 1000");
        target.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText unit = field("Unit, e.g. steps");
        EditText interval = field("Interval hours, e.g. 2");
        interval.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        CheckBox automatic = new CheckBox(this);
        automatic.setText("Track automatically with step sensor");
        automatic.setTextColor(Color.BLACK);

        form.addView(title);
        form.addView(target);
        form.addView(unit);
        form.addView(interval);
        form.addView(automatic);

        new AlertDialog.Builder(this)
                .setTitle("New habit")
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
                    refreshHabits();
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
        refreshHabits();
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
        refreshHabits();
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

    private int parsePositive(String raw, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(raw.trim()));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private EditText field(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setSingleLine(true);
        field.setTextColor(Color.BLACK);
        field.setHintTextColor(Color.DKGRAY);
        return field;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.BLACK);
        view.setTextSize(sp);
        view.setLetterSpacing(0);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private Button pill(String value, boolean filled) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextColor(filled ? Color.WHITE : Color.BLACK);
        button.setBackgroundResource(filled ? R.drawable.button_black : R.drawable.button_white);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                44
        );
        params.setMargins(0, 0, 12, 0);
        button.setLayoutParams(params);
        return button;
    }
}
