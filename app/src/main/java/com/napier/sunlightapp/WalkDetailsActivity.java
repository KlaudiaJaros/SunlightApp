package com.napier.sunlightapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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



public class WalkDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        // to change the activity view:
        TextView walkDescription = (TextView)findViewById(R.id.fullDescText);
        TextView walkTitle = (TextView)findViewById(R.id.walkTitleTextView);
        ImageView walkImage = (ImageView)findViewById(R.id.walkImageView);

        // get the walk name from the previous activity:
        Intent intent = getIntent();
        String walk = intent.getExtras().getString("walk");
        walkTitle.setText(walk);
        String city= intent.getExtras().getString("city");

        /* Firebase Storage: */
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create a child reference
        // imagesRef now points to the city

        StorageReference cityRef = storageRef.child(city);
        StorageReference photoRef = storageRef.child(city+"/"+walk+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024; // CAUTION: photos in Firebase storage cannot exceed 1MB
        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                walkImage.setImageBitmap(bm);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        // Access a Cloud Firestore instance:
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // open the Firebase collection:
        DocumentReference docRef = db.collection("walks").document("edinburgh");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            private static final String TAG = "Firebase" ;

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String,Object> walkDetails = document.getData();
                        walkDescription.setText(String.valueOf(walkDetails.get(walk)));
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}