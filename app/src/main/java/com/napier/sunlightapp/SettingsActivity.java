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
    private String notificationsEnabled="true"; //default
    private int notificationTime=11;
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

        // get access to layout fields and switch:
        EditText userNameText= (EditText)findViewById(R.id.userNameValue);
        EditText userTargetText = (EditText)findViewById(R.id.userTargetValue);
        Switch notificationsSwitch = (Switch)findViewById(R.id.notificationsSwitch);
        Spinner notificationTimeSpinner = (Spinner)findViewById(R.id.notificationTime);
        TextView notifyText = (TextView)findViewById(R.id.notifyText);

       // String[] times = {"0:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
                            //"16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.times_array, R.layout.my_spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        notificationTimeSpinner.setAdapter(adapter);
        notificationTimeSpinner.setSelection(notificationTime); // default notification time

        // set everything according to the existing user settings (if not null):
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
            notifyText.setVisibility(TextView.INVISIBLE);
            notificationTimeSpinner.setVisibility(Spinner.INVISIBLE);
        }

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
                    DeviceBootReceiver.notificationEnabled=true;
                }
                else{
                    DeviceBootReceiver.notificationEnabled=false;
                }
                if (!newNotificationSetting.equals(notificationsEnabled)){ // compare existing setting to the actual one
                    notificationsEnabled=newNotificationSetting;
                    newNotification=true;
                }
                if(notificationTime!=notificationTimeSpinner.getSelectedItemPosition()){
                    newNotification=true;
                    // set the new notification time with the Alarm Manager:
                    //changeAlarmManagerTime(notificationTimeSpinner.getSelectedItemPosition());
                }

                // save the new/updated settings:
                if (newUsername || newTarget || newNotification){
                    if (validUsername && validTarget){
                        try {
                            userName=userNameEdit;
                            target=targetEdit;
                            notificationTime= notificationTimeSpinner.getSelectedItemPosition();
                            String toSave = userName+","+target+','+notificationsEnabled+','+notificationTime;
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


    /**
     * Changes the alarm manager time used to display notifications
     * @param newTime
     */
/*
    private void changeAlarmManagerTime(int newTime){

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        // create an Alarm Manager and set the alarm time:
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, newTime);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

 */
}