package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // UI Components - matching your XML IDs
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignUp;

    // API Service
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize UI components
        initializeViews();

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Set click listeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeViews() {
        // Initialize using the IDs from your XML
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void loginUser() {
        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        showLoading(true);

        // Create User object for login (email can be empty for login)
        User user = new User(username, "", password);

        // Make API call
        Call<ApiResponse<User>> call = apiService.loginUser(user);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Login successful
                        User loggedInUser = apiResponse.getUser();

                        // Save user session
                        saveUserSession(loggedInUser);

                        // Show success message
                        Toast.makeText(LoginActivity.this,
                                "Welcome back, " + loggedInUser.getUsername() + "!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to HomeScreenActivity
                        Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                        finishAffinity(); // Finishes this activity and all parent activities.

                    } else {
                        // Login failed - show error message from server
                        Toast.makeText(LoginActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (404, 500, etc.)
                    Toast.makeText(LoginActivity.this,
                            "Server error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);

                // Network error or connection failed
                Toast.makeText(LoginActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();

                // Print stack trace for debugging
                t.printStackTrace();
            }
        });
    }

    private void saveUserSession(User user) {
        // Save user data in SharedPreferences for session management
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("user_id", user.getUserId());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("full_name", user.getFullName());
        editor.putBoolean("is_logged_in", true);

        editor.apply();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Log In");
        }
    }
}