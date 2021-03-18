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

/**
 * Activity to display all available walks from Firebase based on user's location.
 */
public class WalksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks);

        // Get the TextViews and ListView references:
        ListView walksList = (ListView) findViewById(R.id.walksList);
        TextView noCity = (TextView)findViewById(R.id.noCityText);
        TextView cityTV = (TextView) findViewById(R.id.cityStr);

        // Retrieve passed information from the previous activity:
        Intent intent = getIntent();
        String city = Objects.requireNonNull(intent.getExtras()).getString("city");
        cityTV.setText(city); // set the current city

        // Access a Cloud Firestore instance:
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String cityLower =city.toLowerCase(); // my collection names are saved all lowercase

        // open the Firebase collection and get the city specific document:
        DocumentReference docRef = db.collection("walks").document(cityLower);

        // to store downloaded walks:
        ArrayList<String> listOfWalks= new ArrayList<>();

        // adapter for ListView:
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listOfWalks);

        // get walks from the city document:
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
                                    Object o = walksList.getItemAtPosition(position); // get the object the user picked
                                    String element=(String)o; // cast to String because I'm using a String Adapter
                                    Toast.makeText(getApplicationContext(),element,Toast.LENGTH_SHORT).show(); // display which walk the user picked

                                    // Open the WalkDetailsActivity and pass user's choice along to display the right walk details:
                                    Intent walkDetails = new Intent(WalksActivity.this, WalkDetailsActivity.class);
                                    walkDetails.putExtra("walk", element);
                                    walkDetails.putExtra("city", city);
                                    setResult(1, walkDetails);
                                    startActivity(walkDetails);
                                }
                            });
                        }
                        else { // list of walks is empty:
                            noCity.setText(R.string.walksAvailableStr);
                        }
                    }
                    else{ // no such document exists in Firebase:
                        Log.d("TAG", "No such document");
                        noCity.setText(R.string.walksAvailableStr);
                    }
                }
                else { // task unsuccessful:
                    Log.d("TAG", "Error getting documents: ", task.getException());
                    noCity.setText(R.string.walksAvailableStr);
                }
            }
        });
    }
}