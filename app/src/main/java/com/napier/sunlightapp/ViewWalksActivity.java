package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Lets the user view past walks and today's walk and stats.
 */
public class ViewWalksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_walks);

        // get activity elements:
        ListView walksHistory=(ListView)findViewById(R.id.walksHistoryList);
        TextView dailyTarget=(TextView)findViewById(R.id.dailyTargetValue);
        TextView today = (TextView)findViewById(R.id.todayDate);
        TextView walkedFor = (TextView)findViewById(R.id.actualTargetValue);
        TextView targetAchieved = (TextView)findViewById(R.id.targetAchieved);

        // get the current date:
        Calendar calendar = Calendar.getInstance();
        String pattern = "dd-MM-yyy";
        String date = new SimpleDateFormat(pattern).format(calendar.getTime());
        today.setText(R.string.todayStr+date+":");

        // apply existing settings:
        if (UserSettings.getTarget()!=null){
            dailyTarget.setText(UserSettings.getTarget());
        }
        if(UserSettings.getRemainingTarget()!=null ){
            int remaining = Integer.parseInt(UserSettings.getRemainingTarget());
            if(remaining<=0){
                targetAchieved.setText(R.string.yesStr);
            }
            else{
                targetAchieved.setText(R.string.notYetStr);
            }
        }

        // display past walks:
        final String[] pastWalks = new String[]
                { "Walk 1", "Walk 2", "Walk 3", "Walk 4",
                        "Walk 5", "Walk 6", "Walk 7"};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                pastWalks);

        walksHistory.setAdapter(adapter);
        walksHistory.setContentDescription("Past walks");
    }

    public void editWalkButtonOnClick(View view) {
        Intent recordWalks = new Intent(ViewWalksActivity.this, RecordWalksActivity.class);
        startActivity(recordWalks);
    }
}