package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Activity that shows the app's loading screen with the app logo.
 */
public class LoadingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);


        ImageView logo = (ImageView) findViewById(R.id.logoView);
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    progressBar.setProgress(100);
                    // open the MainActivity after the logo screen:
                    Intent mainActivity = new Intent(LoadingScreenActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }
            }, 3000); // delay for 3 seconds
    }
}