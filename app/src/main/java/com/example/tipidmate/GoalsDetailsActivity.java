package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GoalsDetailsActivity extends AppCompatActivity {

    private Goal goal;
    private GoalRepository goalRepository;
    private LinearLayout llContributions;
    private TextView tvTotalSaved, tvTargetAmount, tvProgressPercentage, tvRemainingAmount, tvSavingsSuggestion;
    private ProgressBar pbGoalProgress;
    private static final int ADD_SAVINGS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_details);

        goalRepository = GoalRepository.getInstance();
        String goalId = getIntent().getStringExtra("goal_id");
        goal = goalRepository.findGoalById(goalId);

        if (goal == null) {
            Toast.makeText(this, "Goal not found. It might have been deleted.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivGoalIcon = findViewById(R.id.ivGoalIcon);
        TextView tvGoalTitle = findViewById(R.id.tvGoalTitle);
        TextView tvDueDate = findViewById(R.id.tvDueDate);
        tvTotalSaved = findViewById(R.id.tvTotalSaved);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
        pbGoalProgress = findViewById(R.id.pbGoalProgress);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        llContributions = findViewById(R.id.llContributions);
        tvSavingsSuggestion = findViewById(R.id.tvSavingsSuggestion);

        ivGoalIcon.setImageResource(goal.getIconResId());
        tvGoalTitle.setText(goal.getTitle());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDueDate.setText("Due by " + sdf.format(new Date(goal.getTargetDate())));

        updateUI();

        FloatingActionButton fabAddSavings = findViewById(R.id.fabAddSavings);
        fabAddSavings.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsDetailsActivity.this, GoalsSavingsActivity.class);
            intent.putExtra("goal_id", goal.getId());
            startActivityForResult(intent, ADD_SAVINGS_REQUEST);
        });

        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsDetailsActivity.this, AllContributionsActivity.class);
            intent.putExtra("goal_id", goal.getId());
            startActivity(intent);
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_SAVINGS_REQUEST && resultCode == RESULT_OK && data != null) {
            Contribution contribution = (Contribution) data.getSerializableExtra("contribution");
            goal.addSavings(contribution);
            goalRepository.updateGoal(goal);
            updateUI();
        }
    }

    private void updateUI() {
        tvTotalSaved.setText(String.format(Locale.getDefault(), "₱%.2f", goal.getCurrentAmount()));
        tvTargetAmount.setText(String.format(Locale.getDefault(), "/ ₱%.2f", goal.getTargetAmount()));

        int progress = 0;
        if (goal.getTargetAmount() > 0) {
            progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
        }
        pbGoalProgress.setProgress(progress);
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", progress));

        double remainingAmount = goal.getTargetAmount() - goal.getCurrentAmount();
        tvRemainingAmount.setText(String.format(Locale.getDefault(), "₱%.2f remaining", remainingAmount));

        updateSavingsSuggestion();

        llContributions.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        List<Contribution> contributions = goal.getContributions();
        for (int i = 0; i < contributions.size(); i++) {
            // Only show the 3 most recent contributions
            if (i >= 3) {
                break;
            }
            Contribution contribution = contributions.get(contributions.size() - 1 - i);

            View contributionView = inflater.inflate(R.layout.goals_contribution_item, llContributions, false);

            TextView tvContributionNote = contributionView.findViewById(R.id.tvContributionNote);
            TextView tvContributionTimestamp = contributionView.findViewById(R.id.tvContributionTimestamp);
            TextView tvContributionAmount = contributionView.findViewById(R.id.tvContributionAmount);
            ImageView ivDeleteContribution = contributionView.findViewById(R.id.ivDeleteContribution);

            tvContributionNote.setText(contribution.getNote());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            tvContributionTimestamp.setText(sdf.format(new Date(contribution.getTimestamp())));
            tvContributionAmount.setText(String.format(Locale.getDefault(), "+₱%.2f", contribution.getAmount()));

            // Hide the delete button on this screen
            ivDeleteContribution.setVisibility(View.GONE);

            llContributions.addView(contributionView);
        }
    }

    private void updateSavingsSuggestion() {
        long diffInMillis = goal.getTargetDate() - System.currentTimeMillis();
        if (diffInMillis < 0) {
            tvSavingsSuggestion.setText("This goal is past its due date!");
            return;
        }

        long diffInWeeks = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 7;
        double remainingAmount = goal.getTargetAmount() - goal.getCurrentAmount();

        if (remainingAmount <= 0) {
            tvSavingsSuggestion.setText("Congratulations! You have reached your goal.");
            return;
        }

        if (diffInWeeks > 0) {
            double weeklySavings = remainingAmount / diffInWeeks;
            tvSavingsSuggestion.setText(String.format(Locale.getDefault(), "You're on track! Save ₱%.2f weekly to reach your goal on time.", weeklySavings));
        } else {
            tvSavingsSuggestion.setText(String.format(Locale.getDefault(), "You're very close! Just ₱%.2f left to save.", remainingAmount));
        }
    }
}
