package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class CourtInformationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";
    ImageView info_court_image;
    TextView info_title;
    TextView court_description;
    Button info_full_button;
    Button info_empty_button;
    Button info_players_button;
    Button back_to_the_map;
    Court court;
    FirebaseFirestore mDB;
    String court_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_information);
        info_court_image = findViewById(R.id.image);
        info_title = findViewById(R.id.info_court_name);
        court_description = findViewById(R.id.info_court_description);
        info_empty_button = findViewById(R.id.court_is_empty);
        info_full_button = findViewById(R.id.court_is_full);
        info_players_button = findViewById(R.id.court_is_searching_for_players);
        back_to_the_map = findViewById(R.id.back_to_map_button);

        mDB = FirebaseFirestore.getInstance();
//        String court_title = "Doron";
//        DocumentReference cd = mDB.collection("courts").document(court_title);
//        Log.d(TAG, "court ref: " + cd.toString());
//        cd.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                Log.d(TAG, "Get doc is completed.");
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    Log.d(TAG, "document data: " + document.getData());
//                } else {
//                    Log.d(TAG, "doc get failed: ", task.getException());
//                }
//                Log.d(TAG, "Finished isSuccessfull.");
//            }
//        });
        // Extract marker name (== court name), from intent.
        Bundle extras = getIntent().getExtras();
        final String court_title = extras.getString("court_title");
        //TODO: Add better handling to when court_title is null.
        if (court_title == null)
            Log.e(TAG, "CourtInformationActivity didn't received court_title");

        DocumentReference selected_document = mDB.collection("courts").document(court_title);
        selected_document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    court = task.getResult().toObject(Court.class);
                    Log.d(TAG, "show information of court:" + court.toString());
                    show_court_information(court);
                }
                else {
                    Log.e(TAG, "There isn't a court with clicked marker title: " + court_title);
                    // TODO: check how to add toast in innner class.
                    // Toast.makeText(this, "Selected court has no information", Toast.LENGTH_SHORT).show();
                }
            }
        });

        info_empty_button.setOnClickListener(this);
        info_full_button.setOnClickListener(this);
        info_players_button.setOnClickListener(this);
        back_to_the_map.setOnClickListener(this);
    }

    public void show_court_information(Court court)
    {
        info_title.setText(court.getTitle());
        court_description.setText(court.getDescription());
    }


    @Override
    public void onClick(View button) {
        String new_state = "";
        if (button == info_empty_button)
        {
            new_state = "empty";
        }
        else if (button == info_full_button)
        {
            new_state = "full";
        }
        else if (button == info_players_button)
        {
            new_state = "searching";
        }
        else if (button == back_to_the_map)
        {
            Intent back_to_map_activity = new Intent(this, MapActivity.class);
            startActivity(back_to_map_activity);
        }
        if (!new_state.equals(""))
        {
            court.setState(new_state);
            mDB.collection("courts").document(court.getTitle()).update("state", court.getState());
            Toast.makeText(this, "Changed court state to " + new_state, Toast.LENGTH_SHORT).show();
        }
    }


}
