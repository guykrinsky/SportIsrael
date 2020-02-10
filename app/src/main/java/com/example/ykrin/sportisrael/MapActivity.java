package com.example.ykrin.sportisrael;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    BottomNavigationView menu_bar_view;
    // TODO: search on what is static final??
    private static final LatLng DORON = new LatLng(32.124699,34.817350);
    private static final LatLng MAUZ_AVIV = new LatLng(32.109579,34.814509);
    private static final LatLng ROPES_COURT = new LatLng(32.124803,34.811851);
    private static final LatLng SUN_CITY_COURT = new LatLng(32.124086,34.827155);


    private Marker ropes_court;
    private Marker doron;
    private Marker mauz_aviv;
    private Marker sun_city_court;


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mauz_aviv = mMap.addMarker(new MarkerOptions()
                .position(MAUZ_AVIV)
                .title("Mauz aviv court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        ropes_court = mMap.addMarker(new MarkerOptions()
                .position(ROPES_COURT)
                .title("Neve gan court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        sun_city_court = mMap.addMarker(new MarkerOptions()
                .position(SUN_CITY_COURT)
                .title("Sun City basketball court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // and move the map's camera to the same location.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ROPES_COURT));
    }
}
