package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Lets the user record today's walk and edit it.
 */
public class RecordWalksActivity extends AppCompatActivity {
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
        // TODO: saved walked for value

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


        File path = this.getFilesDir();

        Button saveBtn = (Button)findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int walkTime=0;
                String notes="";

                boolean validNote = notesEdit.getText()!=null && notesEdit.getText().length()>0;
                boolean validTime =walkedFor.getText()!=null && walkedFor.getText().length()>0;

                if(validNote){
                    notes= String.valueOf(notesEdit.getText());
                }
                if(validTime){
                    walkTime=Integer.parseInt(String.valueOf(walkedFor.getText()));
                }

                if(validNote && validTime){
                    Walk walkToSave = new Walk();
                    walkToSave.setDate(date);
                    walkToSave.setWalkTime(walkTime);
                    walkToSave.setNotes(notes);

                    //FileOutputStream fos = openFileOutput(filename, MODE_APPEND);
                    File file = new File(path, filename);
                    try {
                        UserSettings.saveWalk(file, walkToSave);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                Intent mainActivity = new Intent(RecordWalksActivity.this, MainActivity.class);
                startActivity(mainActivity);
            }

            private FileOutputStream openFileOutput(String filename, int modePrivate) {
                File file = new File(getFilesDir(), filename);
                boolean deleted = file.delete();

                // Open a FileOutputStream to prepare for writing to a file:
                FileOutputStream fos ;
                fos=this.openFileOutput(filename, Context.MODE_APPEND);
                return fos;
            }
        });
    }
}