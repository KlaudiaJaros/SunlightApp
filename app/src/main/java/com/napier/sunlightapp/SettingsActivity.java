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
    private static String notificationsEnabled="true"; //default
    private final String filename = "userSettings.csv";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadSettings();

        // get access to EditText fields and switch:
        EditText userNameText= (EditText)findViewById(R.id.userNameValue);
        EditText userTargetText = (EditText)findViewById(R.id.userTargetValue);
        Switch notificationsSwitch = (Switch)findViewById(R.id.notificationsSwitch);

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
                String userNameEdit=userNameText.getText().toString();
                String targetEdit= userTargetText.getText().toString();

                boolean newUsername = false, newTarget = false, validUsername=false, validTarget=false;
                // check if username and target need saving, if so, validate user input:
                if (userName!=null && userNameEdit!=null){
                    newUsername = !userName.equals(userNameEdit);
                    validUsername = userNameEdit.length()>=0 && userNameEdit.length()<=11 ;
                }
                else if(userNameEdit!=null){
                    newUsername=true;
                    validUsername = userNameEdit.length()>=0 && userNameEdit.length()<=11 ;
                }
                if (target!=null && targetEdit!=null){
                    newTarget = !target.equals(targetEdit);
                    validTarget = targetEdit.length()>=0 && targetEdit.length()<=3  && TextUtils.isDigitsOnly(targetEdit);
                }
                else if(targetEdit!=null){
                    newTarget=true;
                    validTarget = targetEdit.length()>=0 && targetEdit.length()<=3  && TextUtils.isDigitsOnly(targetEdit);
                }
                boolean newNotification = false;

                String newNotificationSetting = "false";
                if (notificationsSwitch.isChecked()){
                    newNotificationSetting="true";
                }
                if (!newNotificationSetting.equals(notificationsEnabled)){
                    notificationsEnabled=newNotificationSetting;
                    newNotification=true;
                }

                if (newUsername && newTarget){
                    // if valid, save username and target then exit:
                    if (validUsername && validTarget){
                        try {
                            userName=userNameEdit;
                            target=targetEdit;
                            String toSave = userName+","+target+','+notificationsEnabled;
                            finalFos.write(toSave.getBytes());
                            returnToMain();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(newUsername){
                    // if valid, save username only and exit:
                    if(validUsername){
                        try {
                            userName=userNameEdit;
                            String toSave = userName+","+target+','+notificationsEnabled;
                            finalFos.write(toSave.getBytes());
                            returnToMain();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(newTarget){
                    // if valid, save the target only and exit:
                    if(validTarget){
                        try {
                            target=targetEdit;
                            String toSave = userName+","+target+','+notificationsEnabled;
                            finalFos.write(toSave.getBytes());
                            returnToMain();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(newNotification){
                    try {
                        String toSave = userName+","+target+','+notificationsEnabled;
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
                if (!TextUtils.isDigitsOnly(target)){
                    String warning="Your target must contain digits only.";
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
                if (!words[0].equals("null")){
                    userName=words[0];
                }
                if (!words[1].equals("null")){
                    target=words[1];
                }
                notificationsEnabled=words[2];
            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
        }

        Switch notifications = (Switch)findViewById(R.id.notificationsSwitch);
        if (notificationsEnabled.equals("true")){
            notifications.setChecked(true);
        }
        else{
            notifications.setChecked(false);
        }
    }

    /**
     * Returns to MainActivity
     */
    private void returnToMain(){
        Intent mainActivity = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }

}