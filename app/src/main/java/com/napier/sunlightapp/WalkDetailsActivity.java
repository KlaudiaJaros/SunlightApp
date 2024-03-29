package com.napier.sunlightapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Map;


/**
 * Walk Details Activity - displays walk details of a walk chosen in the previous activity.
 */
public class WalkDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        // get the activity elements:
        TextView walkDescription = (TextView)findViewById(R.id.fullDescText);
        TextView walkTitle = (TextView)findViewById(R.id.walkTitleTextView);
        ImageView walkImage = (ImageView)findViewById(R.id.walkImageView);
        TextView mapLink = (TextView)findViewById(R.id.link);

        // get the walk name and city from the previous activity:
        Intent intent = getIntent();
        String walk="";
        String city="";
        if (intent.hasExtra("walk")) {
            walk = intent.getExtras().getString("walk");
            walkTitle.setText(walk);
        }
        if(intent.hasExtra("city")){
            city= intent.getExtras().getString("city");
        }

        /* Firebase Storage: */
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference
        StorageReference storageRef = storage.getReference();

        // Create a child reference to get the photo for the walk:
        StorageReference photoRef = storageRef.child(city+"/"+walk+".jpg"); // e.i. go to dir: city/photo.jpg

        // try downloading the photo from Firebase storage:
        final long ONE_MEGABYTE = 1024 * 1024; // CAUTION: photos in Firebase storage cannot exceed 1MB
        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "city/photo.jpg" is returned
                // create a BitMap to store the byte array as a bitmap:
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // display image bitmap:
                walkImage.setImageBitmap(bm);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });

        // Access a Cloud Firestore instance:
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String cityLower =city.toLowerCase(); // my collection names are saved all lowercase

        // open the Firebase collection to pull walk description by city:
        DocumentReference docRef = db.collection("walks").document(cityLower);
        String finalWalk = walk;

        // get the walk description from the city document:
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            private static final String TAG = "Firebase" ;

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String,Object> walkDetails = document.getData();
                        // get the walk description of the chosen walk:
                        walkDescription.setText(String.valueOf(walkDetails.get(finalWalk)));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "Error getting the document ", task.getException());
                }
            }
        });

        // open the Firebase collection to pull walk location links by city:
        docRef = db.collection("walks").document(cityLower+"_links");

        // get the location link form the document:
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            private static final String TAG = "Firebase" ;

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String,Object> walkDetails = document.getData();
                        // Based on the walk name, it gets the right location link:
                        String linkString =String.valueOf(walkDetails.get(finalWalk+"_link"));
                        // construct a link string:
                        String linkedText = "<b>Google Maps:</b>  Click " +
                                String.format("<a href=\"%s\">here</a> ", linkString) +
                                "to see the walk location.";
                        // display the link and make it work:
                        mapLink.setText(Html.fromHtml(linkedText));
                        mapLink.setMovementMethod(LinkMovementMethod.getInstance());

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "Error getting the document", task.getException());
                }
            }
        });
    }
}