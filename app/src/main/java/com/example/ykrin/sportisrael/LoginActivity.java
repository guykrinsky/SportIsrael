package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText m_email;
    EditText m_passowrd;
    Button m_logiin_button;
    TextView m_create_btn;
    ProgressBar progress_bar;
    FirebaseAuth fAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        m_email = findViewById(R.id.email);
        m_passowrd = findViewById(R.id.password);
        progress_bar = findViewById(R.id.progressBar);
        m_logiin_button = findViewById(R.id.login_button);
        m_create_btn = findViewById(R.id.create);

        fAuth = FirebaseAuth.getInstance();

        m_logiin_button.setOnClickListener(this);
        m_create_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View button_pressed)
    {
        if (button_pressed == m_logiin_button)
        {
            String user_email = m_email.getText().toString().trim();
            String user_password = m_passowrd.getText().toString().trim();

            if (TextUtils.isEmpty(user_email)) {
                m_email.setError("email is required");
                return;
            }
            if (TextUtils.isEmpty(user_password)) {
                m_passowrd.setError("password is required");
                return;
            }
            if (user_password.length() < 6) {
                m_passowrd.setError("password must be more then 5 characters");
                return;

            }
            progress_bar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progress_bar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        else if (button_pressed == m_create_btn)
        {
            startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
        }
    }
}
