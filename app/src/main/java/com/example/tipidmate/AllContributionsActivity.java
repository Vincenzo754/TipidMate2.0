package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Contribution;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllContributionsActivity extends AppCompatActivity {

    private LinearLayout llContributions;
    private ApiService apiService;
    private int goalId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_contributions_screen);

        // Get user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AllContributionsActivity.this, LoginActivity.class);
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
        llContributions = findViewById(R.id.llContributions);

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Load contributions from database
        loadContributions();
    }

    private void loadContributions() {
        // Show loading state
        showLoadingState();

        Call<ApiResponse<List<Contribution>>> call = apiService.getContributions(goalId);
        call.enqueue(new Callback<ApiResponse<List<Contribution>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contribution>>> call, Response<ApiResponse<List<Contribution>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Contribution>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        List<Contribution> contributions = apiResponse.getData();

                        if (contributions != null && !contributions.isEmpty()) {
                            displayContributions(contributions);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Toast.makeText(AllContributionsActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(AllContributionsActivity.this,
                            "Failed to load contributions",
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Contribution>>> call, Throwable t) {
                Toast.makeText(AllContributionsActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
                showEmptyState();
            }
        });
    }

    private void showLoadingState() {
        llContributions.removeAllViews();

        // Create loading view
        TextView tvLoading = new TextView(this);
        tvLoading.setText("Loading contributions...");
        tvLoading.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
        tvLoading.setTextSize(16);
        tvLoading.setPadding(0, 100, 0, 0);
        tvLoading.setGravity(android.view.Gravity.CENTER);

        llContributions.addView(tvLoading);
    }

    private void displayContributions(List<Contribution> contributions) {
        llContributions.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Contribution contribution : contributions) {
            View contributionView = inflater.inflate(R.layout.goals_contribution_item, llContributions, false);

            TextView tvContributionNote = contributionView.findViewById(R.id.tvContributionNote);
            TextView tvContributionTimestamp = contributionView.findViewById(R.id.tvContributionTimestamp);
            TextView tvContributionAmount = contributionView.findViewById(R.id.tvContributionAmount);

            // Set note
            String note = contribution.getNotes();
            if (note == null || note.isEmpty()) {
                tvContributionNote.setText("Savings contribution");
            } else {
                tvContributionNote.setText(note);
            }

            // Format and set timestamp
            String displayDate = formatDate(contribution.getContributionDate());
            tvContributionTimestamp.setText(displayDate);

            // Set amount
            tvContributionAmount.setText(String.format(Locale.getDefault(), "+₱%.2f", contribution.getAmount()));

            llContributions.addView(contributionView);
        }
    }

    private void showEmptyState() {
        llContributions.removeAllViews();

        // Create empty state view
        LinearLayout emptyLayout = new LinearLayout(this);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(android.view.Gravity.CENTER);
        emptyLayout.setPadding(32, 100, 32, 32);

        TextView tvEmptyIcon = new TextView(this);
        tvEmptyIcon.setText("💰");
        tvEmptyIcon.setTextSize(48);
        tvEmptyIcon.setGravity(android.view.Gravity.CENTER);

        TextView tvEmptyTitle = new TextView(this);
        tvEmptyTitle.setText("No Contributions Yet");
        tvEmptyTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
        tvEmptyTitle.setTextSize(18);
        tvEmptyTitle.setGravity(android.view.Gravity.CENTER);
        tvEmptyTitle.setPadding(0, 16, 0, 8);

        TextView tvEmptyMessage = new TextView(this);
        tvEmptyMessage.setText("Start saving towards your goal!");
        tvEmptyMessage.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
        tvEmptyMessage.setTextSize(14);
        tvEmptyMessage.setGravity(android.view.Gravity.CENTER);

        emptyLayout.addView(tvEmptyIcon);
        emptyLayout.addView(tvEmptyTitle);
        emptyLayout.addView(tvEmptyMessage);

        llContributions.addView(emptyLayout);
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Unknown date";
        }

        try {
            // Try parsing as timestamp from database (yyyy-MM-dd HH:mm:ss)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            try {
                // Try parsing as just date (yyyy-MM-dd)
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                // If all parsing fails, return the original string
                return dateString;
            }
        }
    }
}
