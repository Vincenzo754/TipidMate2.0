package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Contribution;
import com.example.tipidmate.models.Goal;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsSavingsActivity extends AppCompatActivity {

    private static final String TAG = "GoalsSavingsActivity";

    private ImageView ivGoalIcon;
    private TextView tvGoalTitle, tvDate, tvTime;
    private EditText etAmount, etNote;
    private MaterialButton btnAddToGoal;
    private ApiService apiService;
    private int goalId;
    private int userId;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_savings);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        Log.d(TAG, "User ID: " + userId);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalsSavingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get goal ID
        goalId = getIntent().getIntExtra("goal_id", -1);

        Log.d(TAG, "Goal ID: " + goalId);

        if (goalId == -1) {
            Toast.makeText(this, "Goal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize calendar
        calendar = Calendar.getInstance();
        updateDateTimeDisplay();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup listeners
        setupListeners();

        // Load goal info
        loadGoalInfo();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void initializeViews() {
        ivGoalIcon = findViewById(R.id.ivGoalIcon);
        tvGoalTitle = findViewById(R.id.tvGoalTitle);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnAddToGoal = findViewById(R.id.btnAddToGoal);
    }

    private void setupListeners() {
        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        btnAddToGoal.setOnClickListener(v -> addContribution());
    }

    private void loadGoalInfo() {
        Log.d(TAG, "Loading goal info for goal_id: " + goalId);

        Call<ApiResponse<List<Goal>>> call = apiService.getGoals(userId);
        call.enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                Log.d(TAG, "Goals response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Goal>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        for (Goal goal : apiResponse.getData()) {
                            if (goal.getGoalId() == goalId) {
                                Log.d(TAG, "Found goal: " + goal.getGoalName());
                                displayGoalInfo(goal);
                                return;
                            }
                        }
                        Log.e(TAG, "Goal not found in list");
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                Log.e(TAG, "Failed to load goal info: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void displayGoalInfo(Goal goal) {
        tvGoalTitle.setText(goal.getGoalName());

        int iconResId = getIconResource(goal.getIconName());
        ivGoalIcon.setImageResource(iconResId);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        tvDate.setText(dateFormat.format(calendar.getTime()));
        tvTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void addContribution() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        Log.d(TAG, "Add contribution clicked");
        Log.d(TAG, "Amount: " + amountStr);
        Log.d(TAG, "Note: " + note);

        // Validate amount
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        // Show loading
        btnAddToGoal.setEnabled(false);
        btnAddToGoal.setText("Adding...");

        // Create Contribution object
        Contribution contribution = new Contribution(goalId, amount, note);

        Log.d(TAG, "Making API call - Goal ID: " + goalId + ", Amount: " + amount);

        // Make API call
        Call<ApiResponse<Contribution>> call = apiService.addContribution(contribution);
        call.enqueue(new Callback<ApiResponse<Contribution>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contribution>> call, Response<ApiResponse<Contribution>> response) {
                btnAddToGoal.setEnabled(true);
                btnAddToGoal.setText("Add to Goal");

                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Contribution> apiResponse = response.body();

                    Log.d(TAG, "Success: " + apiResponse.isSuccess());
                    Log.d(TAG, "Message: " + apiResponse.getMessage());

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(GoalsSavingsActivity.this,
                                "Contribution added successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Go back to goal details
                        finish();
                    } else {
                        Toast.makeText(GoalsSavingsActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful");
                    Toast.makeText(GoalsSavingsActivity.this,
                            "Failed to add contribution. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contribution>> call, Throwable t) {
                btnAddToGoal.setEnabled(true);
                btnAddToGoal.setText("Add to Goal");

                Log.e(TAG, "API call failed: " + t.getMessage());
                t.printStackTrace();

                Toast.makeText(GoalsSavingsActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_goals;

        switch (iconName) {
            case "ic_home":
                return R.drawable.ic_home;
            case "ic_car":
                return R.drawable.ic_transport;
            case "ic_travel":
                return R.drawable.ic_travel;
            case "ic_education":
                return R.drawable.ic_education;
            case "ic_furniture":
                return R.drawable.ic_furniture;
            case "ic_others":
                return R.drawable.ic_others;
            case "ic_electronic":
                return R.drawable.ic_laptop;
            case "ic_appliances":
                return R.drawable.ic_appliances;
            default:
                return R.drawable.ic_goals;
        }
    }
}
