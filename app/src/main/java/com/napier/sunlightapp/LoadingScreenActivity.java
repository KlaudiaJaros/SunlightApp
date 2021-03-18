package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Activity that shows the app's loading screen with the app logo.
 */
public class LoadingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        // Progress bar:
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);

        // Progress bar progress:
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    progressBar.setProgress(100);
                    // open the MainActivity after the logo screen:
                    Intent mainActivity = new Intent(LoadingScreenActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }
            }, 2500); // delay for 2.5 seconds
    }
}