package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity for Sunlight.
 */
public class MainActivity extends AppCompatActivity {

    // Create a global variable for the system Location Manager and Listener:
    private LocationManager locationManager;
    private LocationListener locationListener;
    // The identifier for which permission request has been answered:
    private static final int ACCESS_REQUEST_LOCATION = 0;

    // To store location:
    private double lat;
    private double lon;
    private String currentCity;

    // User settings filenames:
    private final String settingsFilename = "userSettings.csv";
    private final String walksFilename ="walkHistory.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // open FileInputStream to apply user settings:
        FileInputStream fis = null;
        try{
            fis = this.openFileInput(settingsFilename);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if(fis!=null){ // only if opened successfully
            UserSettings.loadUserSettings(fis);
        }

        // open FileInputStream to load walk history:
        fis = null;
        try{
            fis = this.openFileInput(walksFilename);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if(fis!=null){ // only if opened successfully
            UserSettings.loadWalkHistory(fis);
        }

        // get access to TextViews to display user settings:
        TextView welcomeString = (TextView)findViewById(R.id.welcomeTextView);
        TextView targetValue = (TextView)findViewById(R.id.dailyTargetValue);
        TextView remainingTarget = (TextView)findViewById(R.id.remainingValue);
        TextView targetAchieved = (TextView)findViewById(R.id.targetAchieved);

        // if username and targets are not empty, display them:
        if (UserSettings.getUserName()!=null){
            String displayWelcome=this.getResources().getString((R.string.welcomeStr))+" "+ UserSettings.getUserName();
            welcomeString.setText(displayWelcome);
        }
        if(UserSettings.getTarget()!=null)
        {
            targetValue.setText(UserSettings.getTarget());
        }

        // display if the target was achieved:
        if(UserSettings.isTargetAchieved()){
            targetAchieved.setText(R.string.yesStr);
            // display remaining target:
            remainingTarget.setText("0");
        }
        else{
            targetAchieved.setText(R.string.notYetStr);
            // display remaining target:
            remainingTarget.setText(Integer.toString(UserSettings.getRemainingTarget()));

        }


        /*  LOCATION: */
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Handle the case we don't have the necessary permission
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(getResources().getString(R.string.app_name),
                        "No Location Permission. Asking for Permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_REQUEST_LOCATION);
            } else {
                Log.i(getResources().getString(R.string.app_name),
                        "Location is allowed.");
                setLocationUpdateFunction();
            }
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            // save latitude and longitude:
            lat = location.getLatitude();
            lon = location.getLongitude();
            // print to the console:
            System.out.println("get current location: lon and lat: "+lon + " " + lat);

            // set city:
            setCity();

            // pull weather data:
            try {
                connectWeatherAPI();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location != null) {
                    String locationString =
                            "Location changed: Lat: " + location.getLatitude() +
                                    " Lng: " + location.getLongitude();

                    // save latitude and longitude:
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    // print to the console:
                    System.out.println("location change: lon and lat: "+lon + " " + lat);

                    setCity();
                    try {
                        connectWeatherAPI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Handle the case we don't have the necessary permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(getResources().getString(R.string.app_name),
                    "No Location Permission. Asking for Permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_REQUEST_LOCATION);
        } else {
            Log.i(getResources().getString(R.string.app_name),
                    "Location is allowed.");
            setLocationUpdateFunction();
        }

        /* Notifications: */
        // create an Alarm Manager and set the alarm time:
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (UserSettings.getNotificationsEnabled().equals("true")){

            createNotificationChannel();
            DeviceBootReceiver.notificationEnabled=true;

            // run this only the first time you open the app:
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean("firstTime", false)) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, UserSettings.getNotificationTime());
                calendar.set(Calendar.MINUTE, 1);
                calendar.set(Calendar.SECOND, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                // set first time to true:
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstTime", true);
                editor.apply();
            }
        }
        // if notifications are off, cancel the alarm manager:
        else{
            manager.cancel(pendingIntent);
            DeviceBootReceiver.notificationEnabled=false;
        }
    }

    /**
     * Creates a notification channel to allow Sunlight send notifications:
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("SunlightChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Sets the current city based on the location and changes the locationValue string in the Main Activity
     */
    private void setCity(){

        System.out.println("lon and lat: "+lon + " " + lat); // for debugging purposes

        TextView locationTextView = findViewById(R.id.locationValue); // find the current city string

        Geocoder gcd = new Geocoder(this, Locale.getDefault()); // using Geocoder class to find the current city based on longitude and latitude
        List<Address> addresses; // to store sample addresses
        try {
            addresses = gcd.getFromLocation(lat, lon, 10); // get a sample of addresses
            if (addresses.size() > 0) { // if not empty
                for (Address a: addresses) { // loop through addresses to find a city
                    if (a.getLocality() != null && a.getLocality().length() > 0) { // if not null or empty
                        currentCity=a.getLocality(); // save the location
                        System.out.println(currentCity);
                        locationTextView.setText(currentCity);  // change the city in the main activity
                        break;
                    }
                }
            }
            else {
                locationTextView.setText(R.string.noAddressStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    Log.i(getResources().getString(R.string.app_name),
                            "Location Permission granted by user.");
                    setLocationUpdateFunction();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e(getResources().getString(R.string.app_name),
                            "No Location Permission granted by user.");
                }
                return;
            }
        }

    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    @SuppressLint("MissingPermission")
    private void setLocationUpdateFunction() {
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }

    /**
     * Creates a JSON object request and a queue using Volley to get data from OpenWeatherAPI. If successful, it sets text to all weather related TextViews using the received data.
     * @throws JSONException Thrown to indicate a problem with the JSON API
     */
    private void connectWeatherAPI() throws JSONException {

        // to edit activity elements:
        TextView tempEdit = (TextView)findViewById(R.id.tempValue);
        TextView minTempEdit = (TextView)findViewById(R.id.minTempValue);
        TextView maxTempEdit = (TextView)findViewById(R.id.maxTempValue);
        TextView feelsLikeEdit = (TextView)findViewById(R.id.feelsLikeValue);
        TextView sunriseEdit = (TextView) findViewById(R.id.sunriseValue);
        TextView sunsetEdit = (TextView)findViewById(R.id.sunsetValue);
        TextView sunlightLeftEdit = (TextView)findViewById(R.id.sunlightLeftValue);
        TextView descEdit = (TextView)findViewById(R.id.descText);
        TextView windSpeedEdit = (TextView)findViewById(R.id.windSpeedValue);

        // url to connect to OpenWeatherMap API:
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" +lat+ "&lon=" + lon + "&appid=38ed18e5f6d4211046e1cf89af573e7a&units=metric";
        // Open a Volley request for a JSON object:
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // get JSON objects from the response:
                    JSONObject mainObj = response.getJSONObject("main"); // for temperatures
                    JSONObject sysObj = response.getJSONObject("sys"); // for sunset/sunrise
                    JSONObject windObj = response.getJSONObject("wind"); // for wind
                    JSONArray array=response.getJSONArray("weather"); // for weather description
                    JSONObject object = array.getJSONObject(0);

                    // to format floats:
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(1);

                    // get the weather information from the JSON objects:
                    String temp = df.format(mainObj.getDouble("temp"));
                    String minTemp=df.format(mainObj.getDouble("temp_min"));
                    String maxTemp=df.format(mainObj.getDouble("temp_max"));
                    String feelsLike = df.format(mainObj.getDouble("feels_like"));
                    double windSpeed = windObj.getDouble("speed");
                    String description = object.getString("description");
                    long sunrise = sysObj.getLong("sunrise"); // in seconds UTS
                    long sunset = sysObj.getLong("sunset");

                    // calculate how much daylight is left:
                    Calendar calendar = Calendar.getInstance();
                    long currentTime = calendar.getTimeInMillis(); // get the current time
                    long difference = sunset*1000 - currentTime; // times 1000 because the calendar current time is in milliseconds

                    String sunriseTime; // to hold a string with sunrise time ready to display
                    calendar.setTimeInMillis(sunrise*1000); // give the calendar UTS time in milliseconds (*1000) to then get normal time min and hr
                    if (calendar.get(Calendar.MINUTE)<10){ // because if it's 12:08 it displays it as 12:8
                        sunriseTime = calendar.get(Calendar.HOUR) + ".0" + calendar.get(Calendar.MINUTE) + " am"; // add a zero
                    }
                    else{ // display as normal
                        sunriseTime = calendar.get(Calendar.HOUR) + "." + calendar.get(Calendar.MINUTE) + " am";
                    }

                    String sunsetTime; // to hold a string with sunrise time ready to display
                    calendar.setTimeInMillis(sunset*1000); // give the calendar UTS time in milliseconds (*1000) to then get normal time min and hr
                    if (calendar.get(Calendar.MINUTE)<10){ // because if it's 12:08 it displays it as 12:8
                        sunsetTime = calendar.get(Calendar.HOUR) + ".0" + calendar.get(Calendar.MINUTE) + " pm"; // add a zero
                    }
                    else{ // display as normal
                        sunsetTime = calendar.get(Calendar.HOUR) + "." + calendar.get(Calendar.MINUTE) + " pm";
                    }

                    // debug:
                    System.out.println("Sunrise: " + sunriseTime + ", sunset: " + sunsetTime + ", current time: " + currentTime + ", difference: " + difference);

                    // set all of the pulled data as text in the TextViews:
                    tempEdit.setText(temp+" ºC");
                    minTempEdit.setText(minTemp+" ºC");
                    maxTempEdit.setText(maxTemp+" ºC");
                    feelsLikeEdit.setText(feelsLike + " ºC");
                    sunriseEdit.setText(sunriseTime);
                    sunsetEdit.setText(sunsetTime);
                    String descToDisplay=getString(R.string.descStr) + " " + description;
                    descEdit.setText(descToDisplay);

                    windSpeed=windSpeed*2.237; // change meters per second to mph
                    df.setMaximumFractionDigits(0);
                    windSpeedEdit.setText(df.format(windSpeed));

                    difference=difference/1000; // daylight left
                    float minuteDifference = difference/60; // get minutes

                    // if there is an hour or more left and the current time is past the sunrise time (e.i. not in the middle of the night)
                    if (minuteDifference>=60 && currentTime>=(sunrise*1000)) {
                        int hour= (int) (minuteDifference/60); // get the hours
                        int minutes = (int) (minuteDifference%60); // get the minutes
                        // display in the sunlight left TestView:
                        sunlightLeftEdit.setText(hour+ " hours " + minutes + "min");
                    }
                    // if there is less than an hour left:
                    else if (minuteDifference>0 && currentTime>=(sunrise*1000)){
                        int minutes = (int)(minuteDifference%60); // get the minutes left only
                        sunlightLeftEdit.setText(minutes + " min"); // set as text
                    }
                    // any other case - no sunlight left:
                    else{
                        sunlightLeftEdit.setText("0");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error loading weather data.",Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }
    );
        // create a Volley request and add it to the queue:
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    // Buttons onClick methods below:

    /**
     * Change current activity to AboutActivity
     * @param view view
     */
    public void aboutButtonOnClick(View view) {
        Intent aboutActivity = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(aboutActivity);
    }

    /**
     * Changes current activity to WalksActivity
     * @param view view
     */
    public void walksButtonOnClick(View view) {
        // go to walks activity and pass the current city:
        Intent walksActivity = new Intent(MainActivity.this, WalksActivity.class);
        walksActivity.putExtra("city", currentCity);
        setResult(1, walksActivity);
        startActivity(walksActivity);
    }

    /**
     * Changes current activity to SettingsActivity
     * @param view view
     */
    public void settingsButtonOnClick(View view) {
        Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivity);
    }

    /**
     * Changes the current activity to ViewWalksActivity
     * @param view view
     */
    public void viewWalksButtonOnClick(View view) {
        Intent viewWalksActivity = new Intent(MainActivity.this, ViewWalksActivity.class);
        startActivity(viewWalksActivity);
    }

    /**
     * Changes the current activity to RecordWalksActivity
     * @param view view
     */
    public void recordWalksButtonOnClick(View view) {
        Intent recordWalksActivity = new Intent(MainActivity.this, RecordWalksActivity.class);
        startActivity(recordWalksActivity);
    }

    /**
     * Goes back to Loading Screen Activity and launches the app again if the user uses the back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loadActivity = new Intent(MainActivity.this, LoadingScreenActivity.class);
        startActivity(loadActivity);
    }
}