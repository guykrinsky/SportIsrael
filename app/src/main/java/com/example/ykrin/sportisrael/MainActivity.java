package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";
    Button m_register_button;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_register_button = (Button)findViewById(R.id.main_register_button);
        m_register_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View button_pressed)
    {
        if (button_pressed == m_register_button)
        {
            Intent intent_register = new Intent(this, RegisterActivity.class);
            startActivity(intent_register);
        }
    }
}
