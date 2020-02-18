package com.example.ykrin.sportisrael;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,View.OnClickListener, GoogleMap.OnMarkerDragListener {
    // private static final String TAG = MapActivity.class.getSimpleName();
    private static final String TAG = "sportIsrael";

    BottomNavigationView menu_bar_view;
    private static final LatLng DORON = new LatLng(32.124699,34.817350);
    private static final LatLng MAUZ_AVIV = new LatLng(32.109579,34.814509);
    private static final LatLng ROPES_COURT = new LatLng(32.124803,34.811851);
    private static final LatLng SUN_CITY_COURT = new LatLng(32.124086,34.827155);

    EditText court_title;
    EditText court_description;
    Button new_court;
    Button create;
    LinearLayout hidden;
    LinearLayout info;
    ImageView info_court_image;
    TextView info_title;
    Button info_full_button;
    Button info_empty_button;
    Button info_players_button;

    private Marker ropes_court;
    private Marker doron;
    private Marker mauz_aviv;
    private Marker sun_city_court;
    Marker current_marker;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);

        new_court = findViewById(R.id.add_court);
        court_title = findViewById(R.id.court_title);
        court_description = findViewById(R.id.court_discription);
        create = findViewById(R.id.create_button);
        hidden = (LinearLayout) findViewById(R.id.hiddenLayout);
        info = (LinearLayout) findViewById(R.id.court_info);
        info_court_image = findViewById(R.id.image);
        info_title = findViewById(R.id.info_court_name);
        info_empty_button = findViewById(R.id.court_is_empty);
        info_full_button = findViewById(R.id.court_is_full);
        info_players_button = findViewById(R.id.court_is_searching_for_players);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new_court.setOnClickListener(this);
        create.setOnClickListener(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        // Add a marker in Sydney, Australia,

        mMap = googleMap;

        doron = mMap.addMarker(new MarkerOptions()
                .position(DORON)
                .title("Doron court")
                .snippet("basketball and soccer court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mauz_aviv = mMap.addMarker(new MarkerOptions()
                .position(MAUZ_AVIV)
                .title("Mauz aviv court")
                .snippet("basketball and soccer court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        ropes_court = mMap.addMarker(new MarkerOptions()
                .position(ROPES_COURT)
                .title("Neve gan court")
                .snippet("basketball and soccer court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        sun_city_court = mMap.addMarker(new MarkerOptions()
                .position(SUN_CITY_COURT)
                .title("Sun City court")
                .snippet("basketball court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

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
    public boolean onMarkerClick(Marker marker)
    {

        info.setVisibility(View.VISIBLE);
        info_title.setText(marker.getTitle());
        set_current_marker(marker);
        info_players_button.setOnClickListener(this);
        info_full_button.setOnClickListener(this);
        info_empty_button.setOnClickListener(this);

        return false;
    }

    @Override
    public void onClick(View button)
    {
        if (button == create)
            on_create_button_click();
        else if (button == new_court)
            on_add_court_button_click();
        else if (button == info_empty_button)
            current_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        else if (button == info_full_button)
            current_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        else if (button == info_players_button)
            current_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        info.setVisibility(View.GONE);
    }
    public void on_add_court_button_click()
    {
        // hides layout
        if(hidden.getVisibility() == View.VISIBLE)
            hidden.setVisibility(View.GONE);
        // shows layout
        else
            hidden.setVisibility(View.VISIBLE);
    }

    public void on_create_button_click()
    {
        String new_court_title = "";
        String new_court_disception = "";
        if (court_description.getText() != null && court_title.getText() != null) {
            new_court_disception = court_description.getText().toString();
            new_court_title = court_title.getText().toString();
        }
        else
        {
            Toast.makeText(this,"Please enter court discreption and title", Toast.LENGTH_LONG).show();
        }
        Marker new_mark = mMap.addMarker(new MarkerOptions()
            .title(new_court_title)
            .position(DORON)
            .snippet(new_court_disception)
            .draggable(true)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        hidden.setVisibility(View.GONE);
        court_title.setText("");
        court_description.setText("");
        // TODO: change layout to gone.
    }

    public void set_current_marker(Marker marker)
    {
        String current_marker_title = marker.getTitle();
        if(current_marker_title.equals(doron.getTitle()))
            current_marker = doron;
        else if (current_marker_title.equals(ropes_court.getTitle()))
            current_marker = ropes_court;
        else if (current_marker_title.equals(mauz_aviv.getTitle()))
            current_marker = mauz_aviv;
        else if (current_marker_title.equals(sun_city_court.getTitle()))
            current_marker = sun_city_court;
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
       // Toast.makeText(this,"Drag the marker to the court's place", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {
        Toast.makeText(this,"when you leave the marker you cant change his place", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        marker.setPosition(marker.getPosition());
        marker.setDraggable(false);
    }
}
