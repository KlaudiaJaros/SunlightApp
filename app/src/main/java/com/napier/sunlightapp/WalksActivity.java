package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WalksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks);

        ListView walksList = (ListView) findViewById(R.id.walksList);

        Intent intent = getIntent();
        String city = intent.getExtras().getString("city");
        TextView cityTV = (TextView) findViewById(R.id.cityStr);
        cityTV.setText(city);

        if (city.equals("Edinburgh"))
        {
            final String[] edinburghWalks = new String[]
                    { "Arthur Seat",
                    "Water of Leith",
                    "Princes Gardens",
                    "Newhaven to Granton Harbour",
                    "Portobello Beach",
                    "Union Canal"};

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    edinburghWalks);

            walksList.setAdapter(adapter);
            walksList.setContentDescription("Arthur Seat Walk");
        }

    }

}