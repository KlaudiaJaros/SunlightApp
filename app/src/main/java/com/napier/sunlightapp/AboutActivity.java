package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * About Activity - displays information about Sunlight.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // web link in TextView:
        TextView link = (TextView) findViewById(R.id.link);
        if (link != null) {
            link.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}