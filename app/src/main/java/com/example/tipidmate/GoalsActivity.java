package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalsActivity extends AppCompatActivity {

    private LinearLayout goalsContainer;
    private GoalRepository goalRepository;
    private LinearLayout emptyState;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_screen);

        goalsContainer = findViewById(R.id.goalsContainer);
        emptyState = findViewById(R.id.emptyState);
        goalRepository = GoalRepository.getInstance();
        etSearch = findViewById(R.id.etSearch);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGoalsList();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
    }

    private void updateGoalsList() {
        filterGoals(etSearch.getText().toString());
    }

    private void filterGoals(String query) {
        goalsContainer.removeAllViews();
        List<Goal> allGoals = goalRepository.getGoalList();
        List<Goal> filteredGoals = new ArrayList<>();

        for (Goal goal : allGoals) {
            if (goal.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredGoals.add(goal);
            }
        }

        if (filteredGoals.isEmpty()) {
            if (allGoals.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                goalsContainer.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                goalsContainer.setVisibility(View.VISIBLE); // Keep container visible to show no search results message if needed
            }
        } else {
            emptyState.setVisibility(View.GONE);
            goalsContainer.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Goal goal : filteredGoals) {
                View goalView = inflater.inflate(R.layout.goal_item, goalsContainer, false);

                ImageView ivGoalIcon = goalView.findViewById(R.id.ivGoalIcon);
                TextView tvGoalTitle = goalView.findViewById(R.id.tvGoalTitle);
                TextView tvGoalDescription = goalView.findViewById(R.id.tvGoalDescription);
                ProgressBar pbGoalProgress = goalView.findViewById(R.id.pbGoalProgress);
                TextView tvGoalProgress = goalView.findViewById(R.id.tvGoalProgress);
                TextView tvGoalDueDate = goalView.findViewById(R.id.tvGoalDueDate);
                ImageView ivDeleteGoal = goalView.findViewById(R.id.ivDeleteGoal);

                ivGoalIcon.setImageResource(goal.getIconResId());
                tvGoalTitle.setText(goal.getTitle());
                tvGoalDescription.setText(goal.getDescription());

                int progress = 0;
                if (goal.getTargetAmount() > 0) {
                    progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
                }
                pbGoalProgress.setProgress(progress);

                String progressText = String.format(Locale.getDefault(), "₱%.2f / ₱%.2f", goal.getCurrentAmount(), goal.getTargetAmount());
                tvGoalProgress.setText(progressText);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvGoalDueDate.setText("Due by " + sdf.format(new Date(goal.getTargetDate())));

                ivDeleteGoal.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Goal")
                            .setMessage("Are you sure you want to delete this goal?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                goalRepository.removeGoal(goal);
                                updateGoalsList();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                goalView.setOnClickListener(v -> {
                    Intent intent = new Intent(GoalsActivity.this, GoalsDetailsActivity.class);
                    intent.putExtra("goal_id", goal.getId());
                    startActivity(intent);
                });
                goalsContainer.addView(goalView);
            }
        }
    }
}
