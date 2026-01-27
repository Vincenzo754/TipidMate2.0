package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsDetailsActivity extends AppCompatActivity {

    private ImageView ivGoalIcon;
    private TextView tvGoalTitle, tvDueDate, tvTotalSaved, tvTargetAmount;
    private TextView tvProgressPercentage, tvRemainingAmount, tvSavingsSuggestion;
    private ProgressBar pbGoalProgress;
    private LinearLayout llContributions;
    private TextView tvSeeAll;
    private FloatingActionButton fabAddSavings;
    private ApiService apiService;
    private int goalId;
    private int userId;
    private Goal currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_details);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalsDetailsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get goal ID from intent
        goalId = getIntent().getIntExtra("goal_id", -1);

        if (goalId == -1) {
            Toast.makeText(this, "Goal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup listeners
        setupListeners();

        // Load goal details
        loadGoalDetails();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void initializeViews() {
        ivGoalIcon = findViewById(R.id.ivGoalIcon);
        tvGoalTitle = findViewById(R.id.tvGoalTitle);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvTotalSaved = findViewById(R.id.tvTotalSaved);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        tvSavingsSuggestion = findViewById(R.id.tvSavingsSuggestion);
        pbGoalProgress = findViewById(R.id.pbGoalProgress);
        llContributions = findViewById(R.id.llContributions);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        fabAddSavings = findViewById(R.id.fabAddSavings);
    }

    private void setupListeners() {
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsDetailsActivity.this, AllContributionsActivity.class);
            intent.putExtra("goal_id", goalId);
            startActivity(intent);
        });

        fabAddSavings.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsDetailsActivity.this, GoalsSavingsActivity.class);
            intent.putExtra("goal_id", goalId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoalDetails();
    }

    private void loadGoalDetails() {
        // Get single goal (you need to create this endpoint)
        // For now, we'll get all goals and filter
        Call<ApiResponse<List<Goal>>> call = apiService.getGoals(userId);
        call.enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Goal>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Goal> goals = apiResponse.getData();

                        // Find the goal with matching ID
                        for (Goal goal : goals) {
                            if (goal.getGoalId() == goalId) {
                                currentGoal = goal;
                                displayGoalDetails(goal);
                                loadRecentContributions();
                                return;
                            }
                        }

                        Toast.makeText(GoalsDetailsActivity.this,
                                "Goal not found",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                Toast.makeText(GoalsDetailsActivity.this,
                        "Failed to load goal details: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void displayGoalDetails(Goal goal) {
        // Set icon
        int iconResId = getIconResource(goal.getIconName());
        ivGoalIcon.setImageResource(iconResId);

        // Set title
        tvGoalTitle.setText(goal.getGoalName());

        // Set due date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(goal.getTargetDate());
            tvDueDate.setText("Due by " + outputFormat.format(date));
        } catch (ParseException e) {
            tvDueDate.setText("Due by " + goal.getTargetDate());
        }

        // Set amounts
        tvTotalSaved.setText(String.format(Locale.getDefault(), "₱%.2f", goal.getCurrentAmount()));
        tvTargetAmount.setText(String.format(Locale.getDefault(), "/ ₱%.2f", goal.getTargetAmount()));

        // Calculate progress
        int progress = 0;
        double remaining = goal.getTargetAmount() - goal.getCurrentAmount();

        if (goal.getTargetAmount() > 0) {
            progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
        }

        pbGoalProgress.setProgress(progress);
        tvProgressPercentage.setText(progress + "%");
        tvRemainingAmount.setText(String.format(Locale.getDefault(), "₱%.2f remaining", remaining));

        // Calculate savings suggestion
        calculateSavingsSuggestion(goal, remaining);
    }

    private void calculateSavingsSuggestion(Goal goal, double remaining) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date targetDate = sdf.parse(goal.getTargetDate());
            Date today = new Date();

            long diffInMillis = targetDate.getTime() - today.getTime();
            long daysRemaining = diffInMillis / (1000 * 60 * 60 * 24);

            if (daysRemaining > 0) {
                double dailySavings = remaining / daysRemaining;
                double weeklySavings = dailySavings * 7;

                if (remaining <= 0) {
                    tvSavingsSuggestion.setText("🎉 Congratulations! You've reached your goal!");
                } else {
                    tvSavingsSuggestion.setText(String.format(Locale.getDefault(),
                            "Save ₱%.2f weekly to reach your goal on time.", weeklySavings));
                }
            } else {
                tvSavingsSuggestion.setText("Target date has passed. Update your goal!");
            }
        } catch (ParseException e) {
            tvSavingsSuggestion.setText("");
        }
    }

    private void loadRecentContributions() {
        Call<ApiResponse<List<Contribution>>> call = apiService.getContributions(goalId);
        call.enqueue(new Callback<ApiResponse<List<Contribution>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contribution>>> call, Response<ApiResponse<List<Contribution>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Contribution>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Contribution> contributions = apiResponse.getData();
                        displayRecentContributions(contributions);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Contribution>>> call, Throwable t) {
                // Silently fail for contributions
                t.printStackTrace();
            }
        });
    }

    private void displayRecentContributions(List<Contribution> contributions) {
        llContributions.removeAllViews();

        // Show only the 3 most recent contributions
        int count = Math.min(contributions.size(), 3);

        if (count == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No contributions yet");
            tvEmpty.setTextColor(getResources().getColor(R.color.light_gray_text));
            tvEmpty.setPadding(0, 16, 0, 16);
            llContributions.addView(tvEmpty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < count; i++) {
            Contribution contribution = contributions.get(i);
            View contributionView = inflater.inflate(R.layout.goals_contribution_item, llContributions, false);

            TextView tvContributionNote = contributionView.findViewById(R.id.tvContributionNote);
            TextView tvContributionTimestamp = contributionView.findViewById(R.id.tvContributionTimestamp);
            TextView tvContributionAmount = contributionView.findViewById(R.id.tvContributionAmount);

            String note = contribution.getNotes();
            if (note == null || note.isEmpty()) {
                tvContributionNote.setText("Savings contribution");
            } else {
                tvContributionNote.setText(note);
            }

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(contribution.getContributionDate());
                tvContributionTimestamp.setText(outputFormat.format(date));
            } catch (ParseException e) {
                tvContributionTimestamp.setText(contribution.getContributionDate());
            }

            tvContributionAmount.setText(String.format(Locale.getDefault(), "+₱%.2f", contribution.getAmount()));

            llContributions.addView(contributionView);
        }
    }

    private int getIconResource(String iconName) {
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
