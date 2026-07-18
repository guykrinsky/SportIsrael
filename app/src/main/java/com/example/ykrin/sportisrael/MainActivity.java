package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";
    Button m_register_button;
    BottomNavigationView menu_bar_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_register_button = (Button)findViewById(R.id.main_register_button);
        m_register_button.setOnClickListener(this);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        NavigationBar.attach(this, menu_bar_view);
    }

    @Override
    public void onClick(View button_pressed)
    {
        if (button_pressed == m_register_button)
        {
            Intent intent_auth = new Intent(this, AuthActivity.class);
            startActivity(intent_auth);
        }
    }
}
