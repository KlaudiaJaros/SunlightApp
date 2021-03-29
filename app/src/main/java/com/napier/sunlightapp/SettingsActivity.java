package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Settings Activity for displaying and editing user settings.
 */
public class SettingsActivity extends AppCompatActivity {
    // to store user settings:
    private String userName ;
    private String target ;
    private String notificationsEnabled="true";
    private int notificationTime=11; // default
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
            notificationTime=UserSettings.getNotificationTime();
        }
        else{
            userName="";
            target="0";
        }

        // get access to layout fields and switch:
        EditText userNameText= (EditText)findViewById(R.id.userNameValue);
        EditText userTargetText = (EditText)findViewById(R.id.userTargetValue);
        Switch notificationsSwitch = (Switch)findViewById(R.id.notificationsSwitch);
        Spinner notificationTimeSpinner = (Spinner)findViewById(R.id.notificationTime);
        TextView notifyText = (TextView)findViewById(R.id.notifyText);

        // populate spinner with the notification times from the string resource:
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.times_array, R.layout.my_spinner);
        // Specify the layout to use when the list of choices appears:
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner:
        notificationTimeSpinner.setAdapter(adapter);

        // set everything according to the existing user settings (if not null):
        notificationTimeSpinner.setSelection(notificationTime); // if null, sets to 11 - default time
        if (userName!=null){
            userNameText.setText(userName);
        }
        if(target!=null)
        {
            userTargetText.setText(target);
        }
        if (notificationsEnabled.equals("true")){
            notificationsSwitch.setChecked(true);
        }
        else{
            notificationsSwitch.setChecked(false);
            // if notifications are off, do not display notification time setting:
            notifyText.setVisibility(TextView.INVISIBLE);
            notificationTimeSpinner.setVisibility(Spinner.INVISIBLE);
        }

        // specify what happens if the user toggles the notification switch:
        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    notifyText.setVisibility(TextView.INVISIBLE);
                    notificationTimeSpinner.setVisibility(Spinner.INVISIBLE);
                }
                else{
                    notifyText.setVisibility(TextView.VISIBLE);
                    notificationTimeSpinner.setVisibility(Spinner.VISIBLE);
                }
            }
        });

        File path = this.getFilesDir(); // path to UserData where user settings are saved

        // save and exit button logic:
        Button saveButton = (Button)findViewById(R.id.saveExitButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the user input from the EditText fields:
                String userNameEdit=userNameText.getText().toString();
                String targetEdit= userTargetText.getText().toString();

                // booleans to check if the input is valid:
                boolean validUsername=false, validTarget=false;

                validUsername = userNameEdit.length()>=0 && userNameEdit.length()<=11 ;
                validTarget = targetEdit.length()>=0 && targetEdit.length()<=3  && TextUtils.isDigitsOnly(targetEdit);

                if (notificationsSwitch.isChecked()){
                    notificationsEnabled="true";
                    DeviceBootReceiver.notificationEnabled=true;
                }
                else{
                    DeviceBootReceiver.notificationEnabled=false;
                    notificationsEnabled="false";
                }

                // save settings if user input is valid:
                if (validUsername && (validTarget || targetEdit.length()==0)){
                    try {
                        userName=userNameEdit;
                        target=targetEdit;
                        if(target.equals("000") || target.equals("00")){
                            target="0";
                        }
                        notificationTime= notificationTimeSpinner.getSelectedItemPosition();

                        // string to be save to the userSettings file:
                        String toSave = userName+","+target+','+notificationsEnabled+','+notificationTime;
                        // update the hour in the DeviceBootReceiver:
                        DeviceBootReceiver.hour=notificationTime;

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

                // display information for the user if their input was incorrect:
                if (userName.length()>11){
                    String warning="Your name cannot be longer than 11 characters.";
                    Toast.makeText(getBaseContext(),warning, Toast.LENGTH_LONG).show();
                }
                if (!TextUtils.isDigitsOnly(target) && target.length()>0){
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


    /**
     * Changes the alarm manager time used to display notifications
     * @param newTime
     */
}