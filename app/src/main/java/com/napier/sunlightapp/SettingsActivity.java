package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Settings Activity for displaying and editing user settings.
 */
public class SettingsActivity extends AppCompatActivity {
    // to store user settings:
    private String userName ;
    private String target ;
    private String notificationsEnabled="true"; //default
    // filename where all user settings are stored:
    private final String filename = "userSettings.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // using FileInputStream, try to open the app's file:
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fis!=null){ // if successful
            // pull existing user settings:
            UserSettings.loadUserSettings(fis);
            userName=UserSettings.getUserName();
            target=UserSettings.getTarget();
            notificationsEnabled=UserSettings.getNotificationsEnabled();
        }

        // get access to layout fields and switch:
        EditText userNameText= (EditText)findViewById(R.id.userNameValue);
        EditText userTargetText = (EditText)findViewById(R.id.userTargetValue);
        Switch notificationsSwitch = (Switch)findViewById(R.id.notificationsSwitch);

        // set everything according to the existing user settings (if not null):
        if (userName!=null){
            userNameText.setText(userName);
        }
        if(target!=null)
        {
            userTargetText.setText(target);
        }
        notificationsSwitch.setChecked(notificationsEnabled.equals("true"));

        File path = this.getFilesDir(); // path to UserData where user settings are saved

        // save and exit button logic:
        Button saveButton = (Button)findViewById(R.id.saveExitButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the user input from the EditText fields:
                String userNameEdit=userNameText.getText().toString();
                String targetEdit= userTargetText.getText().toString();

                // booleans to check if the settings have changed and if the input is valid:
                boolean newUsername = false, newTarget = false, validUsername=false, validTarget=false;

                // check if username needs saving/updating, if yes, validate input:
                if (userName!=null && userNameEdit!=null){
                    newUsername = !userName.equals(userNameEdit); // compare existing username to the one in EditText
                    validUsername = userNameEdit.length()>=0 && userNameEdit.length()<=11 ;
                }
                else if(userNameEdit!=null){ // no existing username, only validate input
                    newUsername=true;
                    validUsername = userNameEdit.length()>=0 && userNameEdit.length()<=11 ;
                }

                // check if target needs saving/updating, if yes, validate input:
                if (target!=null && targetEdit!=null){
                    newTarget = !target.equals(targetEdit); // compare existing target to the one in EditText
                    validTarget = targetEdit.length()>=0 && targetEdit.length()<=3  && TextUtils.isDigitsOnly(targetEdit);
                }
                else if(targetEdit!=null){ // no existing target, just validate the new one
                    newTarget=true;
                    validTarget = targetEdit.length()>=0 && targetEdit.length()<=3  && TextUtils.isDigitsOnly(targetEdit);
                }

                // check if notification setting needs updating:
                boolean newNotification = false;
                String newNotificationSetting = "false";
                if (notificationsSwitch.isChecked()){
                    newNotificationSetting="true";
                }
                if (!newNotificationSetting.equals(notificationsEnabled)){ // compare existing setting to the actual one
                    notificationsEnabled=newNotificationSetting;
                    newNotification=true;
                }

                // save the new/updated settings:
                if (newUsername || newTarget || newNotification){
                    if (validUsername && validTarget){
                        try {
                            userName=userNameEdit;
                            target=targetEdit;
                            String toSave = userName+","+target+','+notificationsEnabled;

                            // create a file:
                            File file = new File(path, filename);
                            // Open a FileOutputStream to write to a file:
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            fos.write(toSave.getBytes());
                            fos.flush();
                            returnToMain();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{ // no new setting detected
                    Toast.makeText(getBaseContext(), "Nothing to save", Toast.LENGTH_LONG).show();
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
     * Returns to MainActivity
     */
    private void returnToMain(){
        Intent mainActivity = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}