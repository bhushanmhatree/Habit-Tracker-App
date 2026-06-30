package com.habittracker;

import android.app.Activity;
import android.os.Bundle;

public class HealthConnectOnboardingActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(HealthConnectUi.screen(
                this,
                "Connect health data",
                "Review permissions before sharing steps with Habit Tracker.",
                "Before you continue",
                "Only grant permissions you are comfortable with. The current app stores habits locally and uses step data for progress."
        ));
    }
}
