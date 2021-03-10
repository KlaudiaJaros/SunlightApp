package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Lets the user record today's walk and edit it.
 */
public class RecordWalksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_walks);

        // get activity elements:
        TextView dailyTarget=(TextView)findViewById(R.id.dailyTargetValue);
        TextView today = (TextView)findViewById(R.id.todayDate);
        TextView walkedFor = (TextView)findViewById(R.id.actualTargetValue);

        // get the current date:
        Calendar calendar = Calendar.getInstance();
        String pattern = "dd-MM-yyy";
        String date = new SimpleDateFormat(pattern).format(calendar.getTime());
        today.setText(R.string.todayStr+date+":");

        // apply existing settings:
        if (UserSettings.getTarget()!=null){
            dailyTarget.setText(UserSettings.getTarget());
        }

    }
}