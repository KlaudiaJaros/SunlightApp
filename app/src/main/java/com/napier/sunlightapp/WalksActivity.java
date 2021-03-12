package com.napier.sunlightapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WalksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks);

        ListView walksList = (ListView) findViewById(R.id.walksList);
        TextView noCity = (TextView)findViewById(R.id.noCityText);

        Intent intent = getIntent();
        String city = Objects.requireNonNull(intent.getExtras()).getString("city");
        TextView cityTV = (TextView) findViewById(R.id.cityStr);
        cityTV.setText(city);

        // Access a Cloud Firestore instance:
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String cityLower =city.toLowerCase(); // collection names are saved all lowercase

        // open the Firebase collection:
        DocumentReference docRef = db.collection("walks").document(cityLower);

        // to store downloaded walks:
        ArrayList<String> listOfWalks= new ArrayList<>();

        // adapter for ListView:
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listOfWalks);

        // get walks from document:
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){

                        // get document(city) data(walks):
                        Map<String, Object> map = document.getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                listOfWalks.add(entry.getKey()); // add walk titles to the list of walks
                            }
                        }

                        // if there are any walks, display them:
                        if (listOfWalks!=null && listOfWalks.size()>0){
                            // set the array adapter with the correct walks based on the city:
                            walksList.setAdapter(adapter);
                            walksList.setContentDescription("Walks selection");

                            // add a on item click listener to open WalkDetailsActivity:
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
                        else {
                            noCity.setText(R.string.walksAvailableStr);
                        }
                    }
                    else{
                        System.out.println("Error: document doesn't exist");
                        noCity.setText(R.string.walksAvailableStr);
                    }
                }
                else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                    noCity.setText(R.string.walksAvailableStr);
                }
            }
        });

        /*db.collection("walks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                System.out.println("FIREBASE: " + document.getId() + document.getData());
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                            System.out.println("Error getting documents: " +task.getException());
                        }
                    }
                });*/



        /*
        String[] walks = new String[6];
        if (city.equals("Edinburgh") || city.equals("Glasgow")){
            if (city.equals("Edinburgh"))
            {
                walks = new String[]
                        { "Arthur Seat",
                                "Calton Hill",
                                "Princes Gardens",
                                "Royal Botanic Garden",
                                "Portobello Beach",
                                "Dean Village"};
            }
            else if(city.equals("Glasgow")){
                walks = new String[]
                        {"River Clyde Walk",
                                "Kelvingrove Park"};
            }

            // set the array adapter with the correct walks based on the city:
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    walks);
            walksList.setAdapter(adapter);
            walksList.setContentDescription("Walks selection");

            // add a on item click listener to open WalkDetailsActivity:
            String finalCity = city;
            walksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object o = walksList.getItemAtPosition(position);
                    String element=(String)o;//As you are using Default String Adapter
                    Toast.makeText(getApplicationContext(),element,Toast.LENGTH_SHORT).show();

                    Intent walkDetails = new Intent(WalksActivity.this, WalkDetailsActivity.class);
                    walkDetails.putExtra("walk", element);
                    walkDetails.putExtra("city", finalCity);
                    setResult(1, walkDetails);
                    startActivity(walkDetails);

                }
            });
        }
        else {
            TextView noCity = (TextView)findViewById(R.id.noCityText);
            noCity.setText(R.string.walksAvailableStr);
        }

         */
    }
}