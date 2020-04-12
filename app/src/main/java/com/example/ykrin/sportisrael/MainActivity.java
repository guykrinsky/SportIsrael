package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";
    BottomNavigationView menu_bar_view;
    Button m_regiser_button;
    Button m_login_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        m_login_button = (Button)findViewById(R.id.main_login_button);
        m_regiser_button = (Button)findViewById(R.id.main_register_button);

        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        // menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

        m_regiser_button.setOnClickListener(this);
        m_login_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View button_pressed)
    {
        if (button_pressed == m_regiser_button)
        {
            Intent intent_register = new Intent(this, RegisterActivity.class);
            startActivity(intent_register);
        }
       else if (button_pressed == m_login_button)
       {
           Intent intent_login = new Intent(this, LoginActivity.class);
           startActivity(intent_login);
       }
    }
}
