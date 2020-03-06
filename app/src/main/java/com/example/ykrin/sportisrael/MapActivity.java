package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener ,View.OnClickListener, GoogleMap.OnMarkerDragListener {
    // private static final String TAG = MapActivity.class.getSimpleName();
    private static final String TAG = "sportIsrael";
    BottomNavigationView menu_bar_view;
    private static final LatLng DORON = new LatLng(32.124699,34.817350);

    EditText court_title;
    EditText court_description;
    Button add_court;
    Button create_court;
    LinearLayout new_court_information;
    Button save_new_court;

    Marker current_marker;
    private Marker new_court_marker;

    private GoogleMap mMap;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Doron court) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = DORON;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    FirebaseFirestore mDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);

        add_court = findViewById(R.id.add_court);
        court_title = findViewById(R.id.court_title);
        court_description = findViewById(R.id.court_discription);
        create_court = findViewById(R.id.create_button);
        new_court_information = findViewById(R.id.new_court_layout);

        save_new_court = findViewById(R.id.save_new_court);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

        // Access a Cloud Firestore instance from your Activity
        mDB = FirebaseFirestore.getInstance();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        add_court.setOnClickListener(this);
        create_court.setOnClickListener(this);
        save_new_court.setOnClickListener(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        current_marker = null;
        new_court_marker = null;
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        // updateLocationUI();
    }
    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SportIsrael", "Entered onMapReady");
        // Add a marker for every court in the DB.
        mDB.collection("courts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Court court = document.toObject(Court.class);
                                Log.d(TAG, court.toString());
                                if (court.getTitle() == null || court.getLocation() == null)
                                {
                                    continue;
                                }
                                BitmapDescriptor marker_color;
                                switch(court.getState())
                                {
                                    case "empty":
                                        marker_color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                                        break;
                                    case "full":
                                        marker_color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                                        break;
                                    case "searching":
                                        marker_color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                        break;
                                    default:
                                        marker_color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                                        break;
                                }
                                mMap.addMarker(new MarkerOptions()
                                    .position(court.getLatLng())
                                    .title(court.getTitle())
                                    .icon(marker_color)
                                    );
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful())
                        {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null)
                            {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        Log.d(TAG, "clicked on marker: " + marker.getTitle());
        Log.d(TAG, "sending intent to show court information");
        Intent show_court_information = new Intent(this, CourtInformationActivity.class);
        show_court_information.putExtra("court_title", marker.getTitle());
        startActivity(show_court_information);
        return false;

    }

    @Override
    public void onClick(View button)
    {
        if (button == create_court)
            on_create_court_button_click();
        else if (button == add_court)
            on_add_court_button_click();
        else if (button == save_new_court)
            set_final_court_location();

    }

    public  void set_final_court_location()
    {
        if (new_court_marker == null) {
            Toast.makeText(this, "new court empty", Toast.LENGTH_SHORT).show();
            return;
        }
        new_court_marker.setPosition(new_court_marker.getPosition());
        new_court_marker.setDraggable(false);
        GeoPoint final_marker_position = new GeoPoint(new_court_marker.getPosition().latitude, new_court_marker.getPosition().longitude);
        mDB.collection("courts").document(new_court_marker.getTitle())
                .update("location", final_marker_position)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "court location successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating location", e);
                    }
                });

        add_court.setVisibility(View.VISIBLE);
        save_new_court.setVisibility(View.GONE);

    }

    public void on_add_court_button_click()
    {
        // hides layout
        if(new_court_information.getVisibility() == View.VISIBLE)
            new_court_information.setVisibility(View.GONE);
        // shows layout
        else
            new_court_information.setVisibility(View.VISIBLE);
    }

    public void on_create_court_button_click()
    {
        String new_court_title;
        String new_court_description;
        if (court_description.getText() != null && court_title.getText() != null) {
            new_court_description = court_description.getText().toString();
            new_court_title = court_title.getText().toString();
        }
        else
        {
            Toast.makeText(this,"Please enter court description and title", Toast.LENGTH_LONG).show();
            return;
        }
        GeoPoint current_place = new GeoPoint(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
        Court new_court = new Court(new_court_title, new_court_description, current_place,"empty");
        // Adding new court to DB.
        mDB.collection("courts").document(new_court.getTitle()).set(new_court);

        new_court_marker = mMap.addMarker(new MarkerOptions()
            .title(new_court.getTitle())
            .position(new_court.getLatLng())
            .snippet(new_court.getDescription())
            .draggable(true)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Empty new court fields.
        court_title.setText("");
        court_description.setText("");

        new_court_information.setVisibility(View.GONE);
        add_court.setVisibility(View.GONE);
        save_new_court.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
       Toast.makeText(this,"Drag the marker to the court's place", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {

    }
}
