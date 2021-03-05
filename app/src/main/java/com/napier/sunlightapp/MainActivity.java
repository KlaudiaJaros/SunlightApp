package com.napier.sunlightapp;

import androidx.annotation.RequiresApi;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    private static String userName ;
    private static String target ;
    private static boolean notificationsEnabled = true; // default
    private final String filename = "userSettings.csv";
    private final String filename2 = "notificationsSettings.txt";


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyUserSettings();

        /*  LOCATION: */
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
                    System.out.println("on create: lon and lat: "+lon + " " + lat);

                    Toast.makeText(getBaseContext(), locationString, Toast.LENGTH_LONG).show();
                    setCity();
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
        if (notificationsEnabled){
            createNotificationChannel();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SunlightChannel")
                    .setSmallIcon(R.drawable.logo_plain)
                    .setContentTitle("title")
                    .setContentText("content")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());
        }


        /*try {
            connectWeatherAPI();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Reads from the userSettings file and applies user settings to the Activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void applyUserSettings(){
        /* Read from a file located in the internal storage directory provided by the system for this and only this app: */

        // using FileInputStream, try to open the app's file:
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // read from the file using InputStreamReader:
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            // if the file is not empty, get the username and the target:
            if (line != null) {
                String words[] = line.split(",");
                userName=words[0];
                target=words[1];
            }
        } catch (IOException e) {
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

        // load notifications settings:
        fis = null;
        try {
            fis = this.openFileInput(filename2);
            // read from the file using InputStreamReader:
            inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            if (line != null && line.equals("true")) {
                notificationsEnabled=true;
            }
            else if (line!=null && line.equals("false")){
                notificationsEnabled=false;
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
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
        Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivity);
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


    /*private void connectWeatherAPI() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request APIrequest = new Request.Builder()
                .url("https://community-open-weather-map.p.rapidapi.com/weather?q=London%2Cuk&lat=0&lon=0&callback=test&id=2172797&lang=null&units=%22metric%22%20or%20%22imperial%22&mode=xml%2C%20html")
                .get()
                .addHeader("x-rapidapi-key", "5471ed4925msh3fd6adab960ceddp1fa57fjsnfd08b578b125")
                .addHeader("x-rapidapi-host", "community-open-weather-map.p.rapidapi.com")
                .build();

        Response APIresponse = client.newCall(APIrequest).execute();

        TextView weatherData = findViewById(R.id.weatherDataView);
        //weatherData.setText(APIresponse.toString());

    }*/

}