package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.type.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Create a global variable for the system Location Manager and Listener:
    private LocationManager locationManager;
    private LocationListener locationListener;
    // The identifier for which permission request has been answered:
    private static final int ACCESS_REQUEST_LOCATION = 0;

    // Location:
    private double lat;
    private double lon;
    private String currentCity;

    // User settings:
    private static String userName;
    private static String target;
    private static String notificationsEnabled = "true"; // default
    private final String filename = "userSettings.csv";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyUserSettings();

        /*  LOCATION: */
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

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
            } catch (IOException e) {
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

                    Toast.makeText(getBaseContext(), locationString, Toast.LENGTH_LONG).show();
                    setCity();
                    try {
                        connectWeatherAPI();
                    } catch (IOException e) {
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
        if (notificationsEnabled.equals("true")){
            createNotificationChannel();

            String contentStr=this.getString(R.string.dailyReminderContentStr);
            String titleStr = this.getString(R.string.dailyReminderStr);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SunlightChannel")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(titleStr)
                    .setContentText(contentStr)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());
        }



    }

    /**
     * Reads from the userSettings file and applies user settings to the Activity
     */
    private void applyUserSettings(){
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

        // get access to TextViews to display user settings:
        TextView welcomeString = (TextView)findViewById(R.id.welcomeTextView);
        TextView targetValue = (TextView)findViewById(R.id.dailyTargetValue);

        // if username and target not empty, display them:
        if (userName!=null){
            welcomeString.setText(welcomeString.getText()+" "+ userName);
        }
        if(target!=null)
        {
            targetValue.setText(target);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loadActivity = new Intent(MainActivity.this, LoadingScreenActivity.class);
        startActivity(loadActivity);
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
        List<Address> addresses = null; // to store sample addresses
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
                locationTextView.setText("No address found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
     * Cahange current activity to AboutActivity
     * @param view
     */
    public void aboutButtonOnClick(View view) {
        Intent aboutActivity = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(aboutActivity);
    }

    /**
     * Changes current activity to WalksActivity
     * @param view
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


    private void connectWeatherAPI() throws IOException {

        // to edit activity elements:
        TextView tempEdit = (TextView)findViewById(R.id.tempValue);
        TextView minTempEdit = (TextView)findViewById(R.id.minTempValue);
        TextView maxTempEdit = (TextView)findViewById(R.id.maxTempValue);
        TextView feelsLikeEdit = (TextView)findViewById(R.id.feelsLikeValue);
        TextView sunriseEdit = (TextView) findViewById(R.id.sunriseValue);
        TextView sunsetEdit = (TextView)findViewById(R.id.sunsetValue);
        TextView sunlightLeftEdit = (TextView)findViewById(R.id.sunlightLeftValue);
        TextView descEdit = (TextView)findViewById(R.id.descText);

        // url to OpenWeatherMap API:
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" +lat+ "&lon=" + lon + "&appid=38ed18e5f6d4211046e1cf89af573e7a&units=metric";
        // open a request for a JSON object:
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject mainObj = response.getJSONObject("main"); // for temperatures
                    JSONObject sysObj = response.getJSONObject("sys"); // for sunset/sunrise

                    JSONArray array=response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);

                    // to format floats:
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(0);

                    // get the weather information from the response:
                    String temp = df.format(mainObj.getDouble("temp"));
                    String minTemp=df.format(mainObj.getDouble("temp_min"));
                    String maxTemp=df.format(mainObj.getDouble("temp_max"));
                    String feelsLike = df.format(mainObj.getDouble("feels_like"));
                    String description = object.getString("description");

                    long sunrise = sysObj.getLong("sunrise");
                    long sunset = sysObj.getLong("sunset");

                    Calendar calendar = Calendar.getInstance();
                    long currentTime = calendar.getTimeInMillis();
                    long difference = sunset*1000 - currentTime;

                    calendar.setTimeInMillis(sunrise*1000); // times 1000 because the time stamp is in milliseconds
                    String sunriseTime = calendar.get(Calendar.HOUR) + "." + calendar.get(Calendar.MINUTE) + " am";

                    calendar.setTimeInMillis(sunset*1000);
                    String sunsetTime = calendar.get(Calendar.HOUR) + "." + calendar.get(Calendar.MINUTE) + " pm";

                    System.out.println("Sunrise: " + sunriseTime + ", sunset: " + sunsetTime + ", current time: " + currentTime + ", difference: " + difference);

                    tempEdit.setText(temp+" ºC");
                    minTempEdit.setText(minTemp+" ºC");
                    maxTempEdit.setText(maxTemp+" ºC");
                    feelsLikeEdit.setText(feelsLike + " ºC");
                    sunriseEdit.setText(sunriseTime);
                    sunsetEdit.setText(sunsetTime);
                    descEdit.setText("Description: " + description);

                    difference=difference/1000;
                    float minuteDifference = difference/60;

                    if (minuteDifference>=60 && currentTime>=(sunrise*1000)) {
                        int hour= (int) (minuteDifference/60);
                        int minutes = (int) (minuteDifference%60);

                        sunlightLeftEdit.setText(hour+ " hours " + minutes + "min");
                    }
                    else if (minuteDifference>0 && currentTime>=(sunrise*1000)){
                        int minutes = (int)(minuteDifference%60);
                        sunlightLeftEdit.setText(minutes + " min");
                    }
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
                error.printStackTrace();
            }
        }
    );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);

    }

}