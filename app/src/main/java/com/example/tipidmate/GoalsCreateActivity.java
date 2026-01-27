package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Goal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsCreateActivity extends AppCompatActivity {

    private EditText etGoalTitle, etTargetAmount, etTargetDate, etDescription;
    private TextView tvCategory;
    private RelativeLayout spinnerCategory;
    private AppCompatButton btnSetGoal;
    private ApiService apiService;
    private int userId;
    private String selectedCategory = "";
    private String selectedIconName = "ic_goal";
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_create);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalsCreateActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup listeners
        setupListeners();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void initializeViews() {
        etGoalTitle = findViewById(R.id.etGoalTitle);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etTargetDate = findViewById(R.id.etTargetDate);
        etDescription = findViewById(R.id.etDescription);
        tvCategory = findViewById(R.id.tvCategory);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSetGoal = findViewById(R.id.btnSetGoal);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        selectedDate = Calendar.getInstance();
    }

    private void setupListeners() {
        // Category selector
        spinnerCategory.setOnClickListener(v -> showCategoryDialog());

        // Date picker
        etTargetDate.setOnClickListener(v -> showDatePicker());

        // Set Goal button
        btnSetGoal.setOnClickListener(v -> createGoal());
    }

    private void showCategoryDialog() {
        String[] categories = {"Electronic", "Vehicle", "Appliances", "Travel", "Furniture", "Education", "Others"};
        String[] iconNames = {"ic_laptop", "ic_transport", "ic_appliances", "ic_travel", "ic_furniture", "ic_education", "ic_others"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setItems(categories, (dialog, which) -> {
            selectedCategory = categories[which];
            selectedIconName = iconNames[which];
            tvCategory.setText(selectedCategory);
        });
        builder.show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    etTargetDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void createGoal() {
        // Get input values
        String title = etGoalTitle.getText().toString().trim();
        String amountStr = etTargetAmount.getText().toString().trim();
        String dateStr = etTargetDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            etGoalTitle.setError("Goal title is required");
            etGoalTitle.requestFocus();
            return;
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            etTargetAmount.setError("Target amount is required");
            etTargetAmount.requestFocus();
            return;
        }

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(amountStr);
            if (targetAmount <= 0) {
                etTargetAmount.setError("Amount must be greater than 0");
                etTargetAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etTargetAmount.setError("Invalid amount");
            etTargetAmount.requestFocus();
            return;
        }

        if (dateStr.isEmpty()) {
            Toast.makeText(this, "Please select a target date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert date to database format (yyyy-MM-dd)
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate;
        try {
            formattedDate = outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        btnSetGoal.setEnabled(false);
        btnSetGoal.setText("Creating Goal...");

        // Create Goal object
        Goal goal = new Goal(userId, title, description, targetAmount, 0.0, formattedDate, selectedIconName);

        // Make API call
        Call<ApiResponse<Goal>> call = apiService.addGoal(goal);
        call.enqueue(new Callback<ApiResponse<Goal>>() {
            @Override
            public void onResponse(Call<ApiResponse<Goal>> call, Response<ApiResponse<Goal>> response) {
                btnSetGoal.setEnabled(true);
                btnSetGoal.setText("Set Goal");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Goal> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(GoalsCreateActivity.this,
                                "Goal created successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate back to GoalsActivity
                        Intent intent = new Intent(GoalsCreateActivity.this, GoalsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(GoalsCreateActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GoalsCreateActivity.this,
                            "Failed to create goal",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Goal>> call, Throwable t) {
                btnSetGoal.setEnabled(true);
                btnSetGoal.setText("Set Goal");

                Toast.makeText(GoalsCreateActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
