package com.habittracker;

import android.app.Activity;
import android.os.Bundle;

public class HealthConnectRationaleActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(HealthConnectUi.screen(
                this,
                "Health Connect",
                "Habit Tracker uses Health Connect only when you choose to connect it. Step data can help walking habits stay accurate across supported health apps.",
                "Data used",
                "Steps are used for walking progress and streak history. You can disconnect Health Connect any time from Android settings."
        ));
    }
}
