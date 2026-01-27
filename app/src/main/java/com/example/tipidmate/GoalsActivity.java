package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Goal;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsActivity extends AppCompatActivity {

    private LinearLayout goalsContainer;
    private LinearLayout emptyState;
    private EditText etSearch;
    private ApiService apiService;
    private int userId;
    private List<Goal> allGoals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_screen);

        // Get user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        goalsContainer = findViewById(R.id.goalsContainer);
        emptyState = findViewById(R.id.emptyState);
        etSearch = findViewById(R.id.etSearch);

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, HomeScreenActivity.class);
            startActivity(intent);
        });

        FloatingActionButton btnAddGoal = findViewById(R.id.btnAddGoal);
        btnAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, GoalsCreateActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGoals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        // Load goals from database
        loadGoalsFromDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoalsFromDatabase();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
    }

    private void loadGoalsFromDatabase() {
        Call<ApiResponse<List<Goal>>> call = apiService.getGoals(userId);
        call.enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Goal>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        allGoals = apiResponse.getData();
                        if (allGoals == null) {
                            allGoals = new ArrayList<>();
                        }
                        filterGoals(etSearch.getText().toString());
                    } else {
                        Toast.makeText(GoalsActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                Toast.makeText(GoalsActivity.this,
                        "Failed to load goals: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void filterGoals(String query) {
        goalsContainer.removeAllViews();
        List<Goal> filteredGoals = new ArrayList<>();

        for (Goal goal : allGoals) {
            if (goal.getGoalName().toLowerCase().contains(query.toLowerCase())) {
                filteredGoals.add(goal);
            }
        }

        if (filteredGoals.isEmpty()) {
            if (allGoals.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                goalsContainer.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                goalsContainer.setVisibility(View.VISIBLE);
            }
        } else {
            emptyState.setVisibility(View.GONE);
            goalsContainer.setVisibility(View.VISIBLE);
            displayGoals(filteredGoals);
        }
    }

    private void displayGoals(List<Goal> goals) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Goal goal : goals) {
            View goalView = inflater.inflate(R.layout.goal_item, goalsContainer, false);

            ImageView ivGoalIcon = goalView.findViewById(R.id.ivGoalIcon);
            TextView tvGoalTitle = goalView.findViewById(R.id.tvGoalTitle);
            TextView tvGoalDescription = goalView.findViewById(R.id.tvGoalDescription);
            ProgressBar pbGoalProgress = goalView.findViewById(R.id.pbGoalProgress);
            TextView tvGoalProgress = goalView.findViewById(R.id.tvGoalProgress);
            TextView tvGoalDueDate = goalView.findViewById(R.id.tvGoalDueDate);
            ImageView ivDeleteGoal = goalView.findViewById(R.id.ivDeleteGoal);

            // Set icon based on icon_name from database
            int iconResId = getIconResource(goal.getIconName());
            ivGoalIcon.setImageResource(iconResId);

            tvGoalTitle.setText(goal.getGoalName());
            tvGoalDescription.setText(goal.getDescription());

            int progress = 0;
            if (goal.getTargetAmount() > 0) {
                progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
            }
            pbGoalProgress.setProgress(progress);

            String progressText = String.format(Locale.getDefault(),
                    "₱%.2f / ₱%.2f", goal.getCurrentAmount(), goal.getTargetAmount());
            tvGoalProgress.setText(progressText);

            // Parse and format date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(goal.getTargetDate());
                tvGoalDueDate.setText("Due by " + outputFormat.format(date));
            } catch (ParseException e) {
                tvGoalDueDate.setText("Due by " + goal.getTargetDate());
            }

            ivDeleteGoal.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to delete this goal?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteGoal(goal);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            goalView.setOnClickListener(v -> {
                Intent intent = new Intent(GoalsActivity.this, GoalsDetailsActivity.class);
                intent.putExtra("goal_id", goal.getGoalId());
                startActivity(intent);
            });

            goalsContainer.addView(goalView);
        }
    }

    private void deleteGoal(Goal goal) {
        Call<ApiResponse<Void>> call = apiService.deleteGoal(goal);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(GoalsActivity.this,
                                "Goal deleted successfully",
                                Toast.LENGTH_SHORT).show();
                        loadGoalsFromDatabase();
                    } else {
                        Toast.makeText(GoalsActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(GoalsActivity.this,
                        "Failed to delete goal: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getIconResource(String iconName) {
        // Map icon names from database to drawable resources
        if (iconName == null) return R.drawable.ic_goals;

        switch (iconName) {
            case "ic_laptop":
                return R.drawable.ic_laptop;
            case "ic_transport":
                return R.drawable.ic_transport;
            case "ic_appliances":
                return R.drawable.ic_appliances;
            case "ic_travel":
                return R.drawable.ic_travel;
            case "ic_furniture":
                return R.drawable.ic_furniture;
            case "ic_education":
                return R.drawable.ic_education;
            case "ic_others":
                return R.drawable.ic_others;
            default:
                return R.drawable.ic_goals;
        }
    }
}
