package com.habittracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    void createHabit(String title, String unit, int target, int intervalHours, boolean automatic, int reminderMinutes) {
        Habit habit = createHabit(title, unit, target, intervalHours, automatic);
        habit.reminderMinutes = reminderMinutes;
        updateHabit(habit);
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
        if (habit.progress >= habit.target) {
            markCompleted(habit);
        } else {
            habit.completed = false;
        }
        updateHabit(habit);
    }

    void deleteHabit(String habitId) {
        List<Habit> habits = getHabits();
        for (int i = habits.size() - 1; i >= 0; i--) {
            if (habits.get(i).id.equals(habitId)) {
                habits.remove(i);
            }
        }
        saveHabits(habits);
    }

    void updateHabitDetails(String habitId, String title, String unit, int target, int intervalHours, boolean automatic, int reminderMinutes) {
        Habit habit = getHabit(habitId);
        if (habit == null) {
            return;
        }
        habit.title = title;
        habit.unit = unit;
        habit.target = target;
        habit.intervalHours = intervalHours;
        habit.automatic = automatic;
        habit.reminderMinutes = reminderMinutes;
        if (habit.progress >= habit.target) {
            markCompleted(habit);
        } else {
            habit.completed = false;
        }
        updateHabit(habit);
    }

    void markCompleted(Habit habit) {
        String today = today();
        habit.completed = true;
        if (!habit.completionDates.contains(today)) {
            habit.completionDates.add(today);
        }
        recalculateStreak(habit);
    }

    void recalculateAllStreaks() {
        List<Habit> habits = getHabits();
        for (Habit habit : habits) {
            recalculateStreak(habit);
        }
        saveHabits(habits);
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

    private void recalculateStreak(Habit habit) {
        Set<String> dates = new HashSet<>(habit.completionDates);
        Calendar cursor = Calendar.getInstance();
        String today = format(cursor.getTime());
        if (!dates.contains(today)) {
            cursor.add(Calendar.DATE, -1);
        }
        int current = 0;
        while (dates.contains(format(cursor.getTime()))) {
            current++;
            cursor.add(Calendar.DATE, -1);
        }
        habit.lastCompletedDate = habit.completionDates.isEmpty() ? "" : habit.completionDates.get(habit.completionDates.size() - 1);
        habit.streak = current;
        habit.bestStreak = Math.max(habit.bestStreak, longestStreak(dates));
    }

    private int longestStreak(Set<String> dates) {
        int best = 0;
        for (String date : dates) {
            Calendar cursor = parse(date);
            int count = 0;
            while (dates.contains(format(cursor.getTime()))) {
                count++;
                cursor.add(Calendar.DATE, 1);
            }
            best = Math.max(best, count);
        }
        return best;
    }

    private Calendar parse(String value) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = formatter().parse(value);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (Exception ignored) {
        }
        return calendar;
    }

    private String today() {
        return format(new Date());
    }

    private String format(Date date) {
        return formatter().format(date);
    }

    private SimpleDateFormat formatter() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }
}
