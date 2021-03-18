package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * View Walks Activity - lets the user view past walks and today's walk and stats.
 */
public class ViewWalksActivity extends AppCompatActivity {
    // filename for storing walk history:
    private final String filename="walkHistory.csv";

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
        String displayDate=this.getResources().getString(R.string.todayStr)+date+":";
        today.setText(displayDate);

        // apply existing settings:
        if (UserSettings.getTarget()!=null){
            dailyTarget.setText(UserSettings.getTarget());
        }

        // to store past walks:
        ArrayList<Walk> walks = new ArrayList<>();

        // using FileInputStream, try to open the app's file:
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // if successful, load the walk history and put it in the ArrayList:
        if(fis!=null) {
            UserSettings.loadWalkHistory(fis);
            walks=UserSettings.getWalks();

            // if the latest walk data is today, display walked for:
            if(UserSettings.isLatestWalkWasToday()){
                walkedFor.setText(Integer.toString(UserSettings.getLastWalk().getWalkTime()));
            }
        }

        // display if the target was achieved:
        if(UserSettings.isTargetAchieved()){
            targetAchieved.setText(R.string.yesStr);
        }
        else{
            targetAchieved.setText(R.string.notYetStr);
        }

        // adapter to display past walks:
        ArrayAdapter<Walk> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                walks);

        walksHistory.setAdapter(adapter);
        walksHistory.setContentDescription("Past walks");

    }

    /**
     * Opens Record Walks Activity to edit today's walk.
     * @param view view
     */
    public void editWalkButtonOnClick(View view) {
        Intent recordWalks = new Intent(ViewWalksActivity.this, RecordWalksActivity.class);
        startActivity(recordWalks);
    }
}