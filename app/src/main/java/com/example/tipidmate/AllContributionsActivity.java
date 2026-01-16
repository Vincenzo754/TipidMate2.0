package com.example.tipidmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AllContributionsActivity extends AppCompatActivity {

    private Goal goal;
    private GoalRepository goalRepository;
    private LinearLayout llContributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_contributions_screen);

        goalRepository = GoalRepository.getInstance();
        String goalId = getIntent().getStringExtra("goal_id");
        goal = goalRepository.findGoalById(goalId);

        if (goal == null) {
            Toast.makeText(this, "Goal not found. It might have been deleted.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        llContributions = findViewById(R.id.llContributions);

        updateUI();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void updateUI() {
        llContributions.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Contribution contribution : goal.getContributions()) {
            View contributionView = inflater.inflate(R.layout.goals_contribution_item, llContributions, false);

            TextView tvContributionNote = contributionView.findViewById(R.id.tvContributionNote);
            TextView tvContributionTimestamp = contributionView.findViewById(R.id.tvContributionTimestamp);
            TextView tvContributionAmount = contributionView.findViewById(R.id.tvContributionAmount);

            tvContributionNote.setText(contribution.getNote());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            tvContributionTimestamp.setText(sdf.format(new Date(contribution.getTimestamp())));
            tvContributionAmount.setText(String.format(Locale.getDefault(), "+₱%.2f", contribution.getAmount()));

            llContributions.addView(contributionView);
        }
    }
}
