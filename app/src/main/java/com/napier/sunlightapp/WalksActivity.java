package com.napier.sunlightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
                    "Calton Hill",
                    "Princes Gardens",
                    "Royal Botanic Garden",
                    "Portobello Beach",
                    "Dean Village"};

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    edinburghWalks);

            walksList.setAdapter(adapter);
            walksList.setContentDescription("Walks selection");
            walksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object o = walksList.getItemAtPosition(position);
                    String element=(String)o;//As you are using Default String Adapter
                    Toast.makeText(getApplicationContext(),element,Toast.LENGTH_SHORT).show();

                    Intent walkDetails = new Intent(WalksActivity.this, WalkDetailsActivity.class);
                    walkDetails.putExtra("walk", element);
                    walkDetails.putExtra("city", city);
                    setResult(1, walkDetails);
                    startActivity(walkDetails);

                }
            });
        }

    }

}