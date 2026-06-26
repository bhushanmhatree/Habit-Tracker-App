package com.habittracker;

import org.json.JSONException;
import org.json.JSONObject;

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
        return object;
    }
}
