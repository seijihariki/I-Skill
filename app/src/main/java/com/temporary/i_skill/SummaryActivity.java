package com.temporary.i_skill;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SummaryActivity extends AppCompatActivity {

    private Toolbar action_bar;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_summary_activity);

        action_bar = (Toolbar) findViewById(R.id.summary_tb);
        setSupportActionBar(action_bar);
    }
}
