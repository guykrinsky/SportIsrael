package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
        register_button = findViewById(R.id.register_button);
        progress_bar = findViewById(R.id.progressBar);

        f_auth = FirebaseAuth.getInstance();

        register_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == register_button)
        {
            onRegisterClick(view);
        }
    }

    private void onRegisterClick(View view)
    {
        if (!ValidationUtils.validateEmail(m_email)) {
            return;
        }
        if (!ValidationUtils.validatePassword(m_password)) {
            return;
        }
        if (!ValidationUtils.validateName(m_full_name)) {
            return;
        }
        
        String user_email = ValidationUtils.getTrimmedText(m_email);
        String user_password = ValidationUtils.getTrimmedText(m_password);
        user_name = ValidationUtils.getTrimmedText(m_full_name);
        
        Log.d("SportIsrael", "Register attempt for email: " + user_email);
        
        progress_bar.setVisibility(View.VISIBLE);

        f_auth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                progress_bar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful())
                {
                    // Update user profile after successful registration
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null)
                    {
                        Log.d("sportIsrael", "User name in register: " + user_name);
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user_name).build();
                        
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, R.string.user_created, Toast.LENGTH_LONG).show();
                                }
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, R.string.user_created, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    }
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_prefix) + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}

