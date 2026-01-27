package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    // UI Components - matching your XML IDs
    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLogIn;

    // API Service
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);

        // Initialize views
        initializeViews();

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Sign up button click
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login link click
        tvLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogIn = findViewById(R.id.tvLogIn);
    }

    private void registerUser() {
        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show loading
        showLoading(true);

        // Create User object for registration (using username as email)
        User user = new User(username, username, password);

        // Make API call
        Call<ApiResponse<User>> call = apiService.registerUser(user);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Registration successful
                        Toast.makeText(SignupActivity.this,
                                "Account created successfully! Please login.",
                                Toast.LENGTH_LONG).show();

                        // Navigate to LoginActivity
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // Registration failed - show error from server
                        Toast.makeText(SignupActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    String errorMessage = "Server error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("SignupActivity", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);

                // Network error
                String errorMessage = "Connection failed: " + t.getMessage();
                Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("SignupActivity", errorMessage, t);

                // Print for debugging
                t.printStackTrace();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSignUp.setEnabled(false);
            btnSignUp.setText("Creating Account...");
        } else {
            btnSignUp.setEnabled(true);
            btnSignUp.setText("Sign Up");
        }
    }
}