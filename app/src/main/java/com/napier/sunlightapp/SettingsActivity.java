package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class SettingsActivity extends AppCompatActivity {
    private static String userName ;
    private static String target ;
    private static boolean notificationsEnabled = true; // default
    private final String filename = "userSettings.csv";
    private final String filename2 = "notificationsSettings.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadSettings();

        // get access to EditText fields and switch:
        EditText userNameText= (EditText)findViewById(R.id.userNameValue);
        EditText userTargetText = (EditText)findViewById(R.id.userTargetValue);

        // if username and target not empty, display them:
        if (userName!=null){
            userNameText.setText(userName);
        }
        if(target!=null)
        {
            userTargetText.setText(target);
        }

        /* Write to a file: */

        // write to a file using FileOutputStream:
        FileOutputStream fos = null;
        try {
            fos = this.openFileOutput(filename, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /* EXIT BUTTON: */
        Button saveButton = (Button)findViewById(R.id.saveExitButton);

        // final version of the FileOutputStream to use inside onClick:
        FileOutputStream finalFos = fos;

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the user input from the EditText fields:
                userName=userNameText.getText().toString();
                target= userTargetText.getText().toString();

                // user input validation:
                boolean setUsername = userName.length()>0 && userName.length()<=11 ;
                boolean setTarget = target.length()>0 && target.length()<=3  && TextUtils.isDigitsOnly(target);

                // save username and target then exit:
                if (setUsername && setTarget){
                    try {
                        String toSave = userName+","+target;
                        finalFos.write(toSave.getBytes());
                        returnToMain();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // save username only and exit:
                else if(setUsername && target.length()==0){
                    try {
                        String toSave = userName+",";
                        finalFos.write(toSave.getBytes());
                        returnToMain();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // save the target only and exit:
                else if(setTarget && userName.length()==0){
                    try {
                        String toSave = ","+target;
                        finalFos.write(toSave.getBytes());
                        returnToMain();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // display information for the user if their input was incorrect:
                if (userName.length()>11){
                    String warning="Your name cannot be longer than 11 characters.";
                    Toast.makeText(getBaseContext(),warning, Toast.LENGTH_LONG).show();
                }
                if (target.length()>3 || !TextUtils.isDigitsOnly(target)){
                    String warning="Your target must contain digits only and cannot exceed 999 minutes.";
                    Toast.makeText(getBaseContext(),warning, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Loads saved settings from internal storage files
     */
    private void loadSettings(){
        /* Read from a file located in the internal storage directory provided by the system for this and only this app: */

        // using FileInputStream, try to open the app's file:
        FileInputStream fis = null;
        InputStreamReader inputStreamReader = null;
        try {
            fis = this.openFileInput(filename);
            // read from the file using InputStreamReader:
            inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        try {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            // if the file is not empty, get the username and the target:
            if (line != null) {
                String words[] = line.split(",");
                userName=words[0];
                target=words[1];
            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
        }

        // notifications:
        // using FileInputStream, try to open the app's file:
        fis = null;
        try {
            fis = this.openFileInput(filename);
            // read from the file using InputStreamReader:
            inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            if (line != null && line.equals("true")) {
                notificationsEnabled=true;
            }
            else if (line!=null && line.equals("false")){
                notificationsEnabled=false;
            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
        }
        Switch notifications = (Switch)findViewById(R.id.notificationsSwitch);
        notifications.setChecked(notificationsEnabled);
    }

    /**
     * Returns to MainActivity
     */
    private void returnToMain(){
        Intent mainActivity = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }

    /**
     * Amends notification settings file
     * @param view
     */
    public void notificationChange(View view) {
        // get access to the switch and save its value:
        Switch notificationsSwitch = (Switch)findViewById(R.id.notificationsSwitch);
        notificationsEnabled=notificationsSwitch.isChecked();

        // write to a file using FileOutputStream:
        FileOutputStream fos = null;
        try {
            fos = this.openFileOutput(filename2, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String toSave="";
            if (notificationsEnabled){
                toSave="true";
            }
            else
            {
                toSave="false";
            }
            fos.write(toSave.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}