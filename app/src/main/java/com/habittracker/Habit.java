package com.habittracker;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class Habit {
    final String id;
    String title;
    String unit;
    int target;
    int intervalHours;
    int progress;
    boolean automatic;
    float baselineSteps;
    boolean completed;
    int reminderMinutes;
    String lastCompletedDate;
    int streak;
    int bestStreak;
    final ArrayList<String> completionDates = new ArrayList<>();

    Habit(String id, String title, String unit, int target, int intervalHours, boolean automatic) {
        this.id = id;
        this.title = title;
        this.unit = unit;
        this.target = target;
        this.intervalHours = intervalHours;
        this.automatic = automatic;
    }

    static Habit fromJson(JSONObject object) throws JSONException {
        Habit habit = new Habit(
                object.getString("id"),
                object.getString("title"),
                object.optString("unit", "count"),
                object.optInt("target", 1),
                object.optInt("intervalHours", 24),
                object.optBoolean("automatic", false)
        );
        habit.progress = object.optInt("progress", 0);
        habit.baselineSteps = (float) object.optDouble("baselineSteps", 0);
        habit.completed = object.optBoolean("completed", false);
        habit.reminderMinutes = object.optInt("reminderMinutes", 0);
        habit.lastCompletedDate = object.optString("lastCompletedDate", "");
        habit.streak = object.optInt("streak", 0);
        habit.bestStreak = object.optInt("bestStreak", 0);
        JSONArray dates = object.optJSONArray("completionDates");
        if (dates != null) {
            for (int i = 0; i < dates.length(); i++) {
                habit.completionDates.add(dates.optString(i));
            }
        }
        return habit;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("title", title);
        object.put("unit", unit);
        object.put("target", target);
        object.put("intervalHours", intervalHours);
        object.put("progress", progress);
        object.put("automatic", automatic);
        object.put("baselineSteps", baselineSteps);
        object.put("completed", completed);
        object.put("reminderMinutes", reminderMinutes);
        object.put("lastCompletedDate", lastCompletedDate == null ? "" : lastCompletedDate);
        object.put("streak", streak);
        object.put("bestStreak", bestStreak);
        JSONArray dates = new JSONArray();
        for (String date : completionDates) {
            dates.put(date);
        }
        object.put("completionDates", dates);
        return object;
    }

    List<String> history() {
        return completionDates;
    }
}
