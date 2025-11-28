package com.example.ykrin.sportisrael;

import android.text.TextUtils;
import android.widget.EditText;

public class ValidationUtils {
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    /**
     * Validates an email field and sets an error if invalid.
     * @param emailField The EditText containing the email
     * @return true if valid, false otherwise
     */
    public static boolean validateEmail(EditText emailField) {
        String email = emailField.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return false;
        }
        return true;
    }
    
    /**
     * Validates a password field and sets an error if invalid.
     * @param passwordField The EditText containing the password
     * @return true if valid, false otherwise
     */
    public static boolean validatePassword(EditText passwordField) {
        String password = passwordField.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordField.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }
        return true;
    }
    
    /**
     * Validates a name field and sets an error if invalid.
     * @param nameField The EditText containing the name
     * @return true if valid, false otherwise
     */
    public static boolean validateName(EditText nameField) {
        String name = nameField.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }
        return true;
    }
    
    /**
     * Gets the trimmed text from an EditText.
     * @param field The EditText field
     * @return The trimmed string value
     */
    public static String getTrimmedText(EditText field) {
        return field.getText().toString().trim();
    }
}

