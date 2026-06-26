package com.habittracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class HabitStore {
    private static final String PREFS = "habit_store";
    private static final String HABITS = "habits";
    private static final String WIDGET_PREFIX = "widget_";
    private static final String WIDGET_MODE_PREFIX = "widget_mode_";

    private final SharedPreferences preferences;

    HabitStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    List<Habit> getHabits() {
        ArrayList<Habit> habits = new ArrayList<>();
        String raw = preferences.getString(HABITS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                habits.add(Habit.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
        }
        return habits;
    }

    Habit getHabit(String id) {
        for (Habit habit : getHabits()) {
            if (habit.id.equals(id)) {
                return habit;
            }
        }
        return null;
    }

    Habit createHabit(String title, String unit, int target, int intervalHours, boolean automatic) {
        Habit habit = new Habit(UUID.randomUUID().toString(), title, unit, target, intervalHours, automatic);
        List<Habit> habits = getHabits();
        habits.add(0, habit);
        saveHabits(habits);
        return habit;
    }

    void updateHabit(Habit updated) {
        List<Habit> habits = getHabits();
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).id.equals(updated.id)) {
                habits.set(i, updated);
                saveHabits(habits);
                return;
            }
        }
    }

    void addProgress(String habitId, int amount) {
        Habit habit = getHabit(habitId);
        if (habit == null) {
            return;
        }
        habit.progress = Math.max(0, habit.progress + amount);
        habit.completed = habit.progress >= habit.target;
        updateHabit(habit);
    }

    void saveWidgetHabit(int widgetId, String habitId) {
        preferences.edit().putString(WIDGET_PREFIX + widgetId, habitId).apply();
    }

    void saveWidgetMode(int widgetId, String mode) {
        preferences.edit().putString(WIDGET_MODE_PREFIX + widgetId, mode).apply();
    }

    String getWidgetHabit(int widgetId) {
        return preferences.getString(WIDGET_PREFIX + widgetId, null);
    }

    String getWidgetMode(int widgetId) {
        return preferences.getString(WIDGET_MODE_PREFIX + widgetId, "target");
    }

    void removeWidgetHabit(int widgetId) {
        preferences.edit()
                .remove(WIDGET_PREFIX + widgetId)
                .remove(WIDGET_MODE_PREFIX + widgetId)
                .apply();
    }

    private void saveHabits(List<Habit> habits) {
        JSONArray array = new JSONArray();
        for (Habit habit : habits) {
            try {
                array.put(habit.toJson());
            } catch (JSONException ignored) {
            }
        }
        preferences.edit().putString(HABITS, array.toString()).apply();
    }
}
