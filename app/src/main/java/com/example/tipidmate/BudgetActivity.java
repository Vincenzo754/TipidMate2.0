package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Budget;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetActivity extends AppCompatActivity {

    private EditText etBudgetAmount;
    private RadioGroup rgBudgetFrequency;
    private RadioButton rbMonthly, rbDaily;
    private MaterialButton btnSaveBudget;
    private ImageView ivBack;

    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_details_screen);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BudgetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Load existing budget if any
        loadActiveBudget();
    }

    private void initializeViews() {
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        rgBudgetFrequency = findViewById(R.id.rgBudgetFrequency);
        rbMonthly = findViewById(R.id.rbMonthly);
        rbDaily = findViewById(R.id.rbDaily);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void loadActiveBudget() {
        Call<ApiResponse<Budget>> call = apiService.getActiveBudget(userId);
        call.enqueue(new Callback<ApiResponse<Budget>>() {
            @Override
            public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Budget> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Budget budget = apiResponse.getData();

                        // Pre-fill with existing budget
                        etBudgetAmount.setText(String.valueOf((int)budget.getAmount()));

                        if (budget.getFrequency().equals("monthly")) {
                            rbMonthly.setChecked(true);
                        } else {
                            rbDaily.setChecked(true);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                // Fail silently - user might not have a budget yet
                t.printStackTrace();
            }
        });
    }

    private void saveBudget() {
        // Get amount
        String amountText = etBudgetAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountText)) {
            etBudgetAmount.setError("Amount is required");
            etBudgetAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                etBudgetAmount.setError("Amount must be greater than 0");
                etBudgetAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Invalid amount");
            etBudgetAmount.requestFocus();
            return;
        }

        // Get frequency
        String frequency = rbMonthly.isChecked() ? "monthly" : "daily";

        // Create budget object
        Budget budget = new Budget(userId, amount, frequency);

        // Show loading
        btnSaveBudget.setEnabled(false);
        btnSaveBudget.setText("Saving...");

        // Make API call
        Call<ApiResponse<Budget>> call = apiService.setBudget(budget);
        call.enqueue(new Callback<ApiResponse<Budget>>() {
            @Override
            public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                btnSaveBudget.setEnabled(true);
                btnSaveBudget.setText("Save");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Budget> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(BudgetActivity.this,
                                "Budget saved successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish(); // Go back to home screen
                    } else {
                        Toast.makeText(BudgetActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BudgetActivity.this,
                            "Failed to save budget",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                btnSaveBudget.setEnabled(true);
                btnSaveBudget.setText("Save");

                Toast.makeText(BudgetActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}