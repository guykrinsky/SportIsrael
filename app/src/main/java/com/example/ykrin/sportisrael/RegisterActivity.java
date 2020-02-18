package com.example.ykrin.sportisrael;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText m_full_name;
    EditText m_phone;
    EditText m_password;
    EditText m_email;
    TextView m_login_button;
    Button register_button;

    String user_name;


    ProgressBar progress_bar;
    FirebaseAuth f_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        m_full_name = findViewById(R.id.full_name);
        m_email = findViewById(R.id.email);
        m_password = findViewById(R.id.password);
        m_phone = findViewById(R.id.phone_number);
        m_login_button = findViewById(R.id.have_user);
        register_button = findViewById(R.id.register_button);
        progress_bar = findViewById(R.id.progressBar);

        f_auth = FirebaseAuth.getInstance();
        /*
        if (f_auth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        */

        register_button.setOnClickListener(this);
        m_login_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == register_button)
        {
            onRegisterClick(view);
        }
        else if(view == m_login_button)
        {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }

    private void onRegisterClick(View view)
    {
        Log.d("SportIsrael", "Register with email:" + m_email.getText().toString());
        String user_email = m_email.getText().toString().trim();
        Log.d("SportIsrael", "Register with password:" + m_password.getText().toString());
        String user_password = m_password.getText().toString().trim();
        user_name = m_full_name.getText().toString();

        if(TextUtils.isEmpty(user_email))
        {
            m_email.setError("Email is required");
            return;
        }
        if(TextUtils.isEmpty(user_password))
        {
            m_password.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(user_name))
        {
            m_full_name.setError("Name is required");
            return;
        }
        if (user_password.length() < 6)
        {
            m_password.setError("Password must be more then 5 characters");
            return;
        }
        progress_bar.setVisibility(View.VISIBLE);

        f_auth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                progress_bar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful())
                {
                    Toast.makeText(RegisterActivity.this,"User created!",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Error: " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("sportIsrael", "User name in register: " + user_name);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user_name).build();

        if (user != null)
        {
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Name updated", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

}

