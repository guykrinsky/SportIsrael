package com.example.ykrin.sportisrael;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    BottomNavigationView menu_bar_view;

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("SportIsrael", "Entered onMapReady");
        // Add a marker in Sydney, Australia,
        LatLng ropes_court = new LatLng(32.124654, 34.812243);
        googleMap.addMarker(new MarkerOptions().position(ropes_court).title("Neve Gan Basketball court"));

        // and move the map's camera to the same location.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ropes_court));
    }
}
