package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * LoginActivity handles user authentication and registration
 * Integrates with SQLite database for user management
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "InventoryAppPrefs";
    private static final String PREF_LOGGED_IN = "logged_in";
    private static final String PREF_USER_EMAIL = "user_email";

    // UI Components
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialButton biometricButton;

    // Database helper
    private InventoryDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        databaseHelper = new InventoryDatabaseHelper(this);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupClickListeners();

        Log.d(TAG, "LoginActivity created successfully");
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        biometricButton = findViewById(R.id.biometric_login_button);

        // Set default demo credentials for testing
        emailInput.setText("demo@inventory.com");
        passwordInput.setText("demo123");
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> handleRegistration());
        biometricButton.setOnClickListener(v -> handleBiometricLogin());
    }

    /**
     * Handle user login attempt
     */
    private void handleLogin() {
        String email = getTextFromInput(emailInput);
        String password = getTextFromInput(passwordInput);

        if (!validateLoginInput(email, password)) {
            return;
        }

        // Disable button to prevent multiple clicks
        loginButton.setEnabled(false);

        try {
            // Verify credentials against database
            if (databaseHelper.verifyUser(email, password)) {
                // Login successful
                saveLoginState(email);
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                // Login failed
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
                passwordInput.setText(""); // Clear password field
                passwordInput.requestFocus();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage(), e);
            Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_LONG).show();
        } finally {
            loginButton.setEnabled(true);
        }
    }

    /**
     * Handle user registration
     */
    private void handleRegistration() {
        String email = getTextFromInput(emailInput);
        String password = getTextFromInput(passwordInput);

        if (!validateRegistrationInput(email, password)) {
            return;
        }

        // Disable button to prevent multiple clicks
        registerButton.setEnabled(false);

        try {
            // Check if user already exists
            if (databaseHelper.userExists(email)) {
                Toast.makeText(this, "Account already exists. Please log in instead.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Create new user account
            long userId = databaseHelper.createUser(email, password);

            if (userId != -1) {
                // Registration successful
                saveLoginState(email);
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                Toast.makeText(this, "Failed to create account. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during registration: " + e.getMessage(), e);
            Toast.makeText(this, "Registration error. Please try again.", Toast.LENGTH_LONG).show();
        } finally {
            registerButton.setEnabled(true);
        }
    }

    /**
     * Handle biometric authentication (placeholder for future implementation)
     */
    private void handleBiometricLogin() {
        Toast.makeText(this, "Biometric authentication will be available in a future update",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Validate login input fields
     * @param email User email
     * @param password User password
     * @return true if valid, false otherwise
     */
    private boolean validateLoginInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            emailInput.setError("Please enter a valid email address");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate registration input fields
     * @param email User email
     * @param password User password
     * @return true if valid, false otherwise
     */
    private boolean validateRegistrationInput(String email, String password) {
        if (!validateLoginInput(email, password)) {
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters long");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Check if email format is valid
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Safely get text from TextInputEditText
     * @param input Input field to get text from
     * @return Trimmed text or empty string if null
     */
    private String getTextFromInput(TextInputEditText input) {
        if (input.getText() != null) {
            return input.getText().toString().trim();
        }
        return "";
    }

    /**
     * Save login state to SharedPreferences
     * @param email User's email
     */
    private void saveLoginState(String email) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_LOGGED_IN, true);
        editor.putString(PREF_USER_EMAIL, email);
        editor.apply();

        Log.d(TAG, "Login state saved for user: " + email);
    }

    /**
     * Check if user is already logged in
     * @return true if logged in, false otherwise
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_LOGGED_IN, false);
    }

    /**
     * Navigate to MainActivity and finish this activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}