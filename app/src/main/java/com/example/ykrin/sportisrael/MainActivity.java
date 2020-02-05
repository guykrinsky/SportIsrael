package com.example.ykrin.sportisrael;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView menu_bar_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);

        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

    }
}
