package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *  Record Walks Activity class - lets the user record today's walk and edit it.
 */
public class RecordWalksActivity extends AppCompatActivity {
    // filename for storing walks:
    private final String filename="walkHistory.csv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_walks);

        // get activity elements:
        TextView dailyTarget=(TextView)findViewById(R.id.dailyTargetValue);
        TextView today = (TextView)findViewById(R.id.todayDate);
        TextView walkedFor = (TextView)findViewById(R.id.walkedForValue);
        EditText notesEdit = (EditText)findViewById(R.id.notesEdit);

        // get the current date:
        Calendar calendar = Calendar.getInstance();
        String pattern = "dd-MM-yyy";
        String date = new SimpleDateFormat(pattern).format(calendar.getTime());
        String displayDate =this.getResources().getString(R.string.todayStr)+date+":";
        today.setText(displayDate);

        // apply existing settings:
        if (UserSettings.getTarget()!=null){
            dailyTarget.setText(UserSettings.getTarget());
        }

        // using FileInputStream, try to open the app's file:
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // if opening the file was successful:
        if(fis!=null){
            // load saved walks data:
            UserSettings.loadWalkHistory(fis);

            // if the latest walk has today's date, display its details:
            if(UserSettings.isLatestWalkWasToday()){
                Walk walk = UserSettings.getLastWalk();
                if(walk.getWalkTime()!=0){
                    walkedFor.setText(String.valueOf(walk.getWalkTime()));
                }
                if(walk.getNotes()!=null && walk.getNotes().length()>0){
                    notesEdit.setText(walk.getNotes());
                }
            }
        }

        File path = this.getFilesDir(); // path to UserData for saving user's walks

        // Save button onClick:
        Button saveBtn = (Button)findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int walkTime=0;
                String notes="";
                String notesEditText=String.valueOf(notesEdit.getText());

                // to check if user's input is valid:
                boolean validNote = notesEdit.getText()!=null && notesEditText.length()>0 && !notesEditText.contains(",");
                boolean validTime =walkedFor.getText()!=null && walkedFor.getText().length()>0 & TextUtils.isDigitsOnly(walkedFor.getText());

                if(validNote){
                    notes= notesEditText;
                }
                else {
                    Toast.makeText(getBaseContext(), "Your note can't be longer than 50 chars and have commas.", Toast.LENGTH_LONG).show();
                }
                if(validTime){
                    walkTime=Integer.parseInt(String.valueOf(walkedFor.getText()));
                }
                else {
                    Toast.makeText(getBaseContext(), "Walk time must be a number and no greater than 999.", Toast.LENGTH_LONG).show();
                }

                if(validNote && validTime){
                    // create a Walk object to be saved:
                    Walk walkToSave = new Walk();
                    walkToSave.setDate(date);
                    walkToSave.setWalkTime(walkTime);
                    walkToSave.setNotes(notes);

                    // open a file in the UserData internal storage:
                    File file = new File(path, filename);
                    try {
                        // pass it to UserSettings class to save it:
                        UserSettings.saveWalk(file, walkToSave);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // return to Main Activity:
                    Intent mainActivity = new Intent(RecordWalksActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }
            }
        });
    }
}