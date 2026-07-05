package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Single screen for both login and registration, toggled with the
 * Sign in / Register tabs. In debug builds a quick-login section
 * appears with one-tap test accounts; BuildConfig.DEBUG guarantees
 * it never ships in a release build.
 */
public class AuthActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";

    // Dev quick-login accounts (debug builds only).
    private static final String DEV_PASSWORD = "test123456";
    private static final String DEV_ADMIN_EMAIL = "admin@sportisrael.test";
    private static final String DEV_USER_EMAIL = "user@sportisrael.test";
    private static final String DEV_ORGANIZER_EMAIL = "organizer@sportisrael.test";

    TextView auth_subtitle;
    Button tab_login;
    Button tab_register;
    TextView label_full_name;
    EditText m_full_name;
    EditText m_email;
    EditText m_password;
    TextView label_phone;
    EditText m_phone;
    Button action_button;
    ProgressBar progress_bar;
    LinearLayout dev_section;
    Button dev_admin;
    Button dev_user;
    Button dev_organizer;

    FirebaseAuth f_auth;
    boolean is_login_mode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth_subtitle = findViewById(R.id.auth_subtitle);
        tab_login = findViewById(R.id.auth_tab_login);
        tab_register = findViewById(R.id.auth_tab_register);
        label_full_name = findViewById(R.id.label_full_name);
        m_full_name = findViewById(R.id.full_name);
        m_email = findViewById(R.id.email);
        m_password = findViewById(R.id.password);
        label_phone = findViewById(R.id.label_phone);
        m_phone = findViewById(R.id.phone_number);
        action_button = findViewById(R.id.auth_action_button);
        progress_bar = findViewById(R.id.progressBar);
        dev_section = findViewById(R.id.dev_quick_login);
        dev_admin = findViewById(R.id.dev_login_admin);
        dev_user = findViewById(R.id.dev_login_user);
        dev_organizer = findViewById(R.id.dev_login_organizer);

        f_auth = FirebaseAuth.getInstance();

        tab_login.setOnClickListener(this);
        tab_register.setOnClickListener(this);
        action_button.setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            dev_section.setVisibility(View.VISIBLE);
            dev_admin.setOnClickListener(this);
            dev_user.setOnClickListener(this);
            dev_organizer.setOnClickListener(this);
        }

        applyMode();
    }

    @Override
    public void onClick(View view) {
        if (view == tab_login && !is_login_mode) {
            is_login_mode = true;
            applyMode();
        } else if (view == tab_register && is_login_mode) {
            is_login_mode = false;
            applyMode();
        } else if (view == action_button) {
            if (is_login_mode) {
                onLoginClick();
            } else {
                onRegisterClick();
            }
        } else if (view == dev_admin) {
            devLogin(DEV_ADMIN_EMAIL, "Dev Admin");
        } else if (view == dev_user) {
            devLogin(DEV_USER_EMAIL, "Dev User");
        } else if (view == dev_organizer) {
            devLogin(DEV_ORGANIZER_EMAIL, "Dev Organizer");
        }
    }

    /** Updates tab highlight, field visibility and button text for the mode. */
    private void applyMode() {
        int registerFieldVisibility = is_login_mode ? View.GONE : View.VISIBLE;
        label_full_name.setVisibility(registerFieldVisibility);
        m_full_name.setVisibility(registerFieldVisibility);
        label_phone.setVisibility(registerFieldVisibility);
        m_phone.setVisibility(registerFieldVisibility);

        auth_subtitle.setText(is_login_mode ? "Welcome back" : "Create your account");
        action_button.setText(is_login_mode ? "Sign in" : "Create account");

        styleTab(tab_login, is_login_mode);
        styleTab(tab_register, !is_login_mode);
    }

    private void styleTab(Button tab, boolean active) {
        if (active) {
            tab.setBackgroundResource(R.drawable.btn_primary);
            tab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tab.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            tab.setTextColor(ContextCompat.getColor(this, R.color.on_surface_secondary));
        }
    }

    private void onLoginClick() {
        if (!ValidationUtils.validateEmail(m_email)) {
            return;
        }
        if (!ValidationUtils.validatePassword(m_password)) {
            return;
        }
        signIn(ValidationUtils.getTrimmedText(m_email), ValidationUtils.getTrimmedText(m_password));
    }

    private void devLogin(String email, String label) {
        Log.d(TAG, "Dev quick login as " + label);
        signIn(email, DEV_PASSWORD);
    }

    private void signIn(String email, String password) {
        progress_bar.setVisibility(View.VISIBLE);
        f_auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progress_bar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            finish();
                        } else {
                            Toast.makeText(AuthActivity.this,
                                    getString(R.string.error_prefix) + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void onRegisterClick() {
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
        final String user_name = ValidationUtils.getTrimmedText(m_full_name);

        Log.d(TAG, "Register attempt for email: " + user_email);
        progress_bar.setVisibility(View.VISIBLE);

        f_auth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progress_bar.setVisibility(View.INVISIBLE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(AuthActivity.this,
                                    getString(R.string.error_prefix) + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Toast.makeText(AuthActivity.this, R.string.user_created, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            finish();
                            return;
                        }
                        UserDirectory.writeUserDocument(user.getUid(), user_name, user.getEmail());
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user_name).build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AuthActivity.this, R.string.user_created, Toast.LENGTH_LONG).show();
                                }
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                finish();
                            }
                        });
                    }
                });
    }
}
