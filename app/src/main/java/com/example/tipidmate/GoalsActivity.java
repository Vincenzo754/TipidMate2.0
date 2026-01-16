package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalsActivity extends AppCompatActivity {

    private LinearLayout goalsContainer;
    private GoalRepository goalRepository;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_screen);

        goalsContainer = findViewById(R.id.goalsContainer);
        emptyState = findViewById(R.id.emptyState);
        goalRepository = GoalRepository.getInstance();

        FloatingActionButton btnAddGoal = findViewById(R.id.btnAddGoal);
        btnAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, GoalsCreateActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_charts) {
                startActivity(new Intent(getApplicationContext(), ChartsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_goals) {
                return true;
            } else if (itemId == R.id.navigation_group_budget) {
                startActivity(new Intent(getApplicationContext(), GroupBudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGoalsList();
    }

    private void updateGoalsList() {
        goalsContainer.removeAllViews();
        List<Goal> goals = goalRepository.getGoalList();

        if (goals.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            goalsContainer.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            goalsContainer.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Goal goal : goals) {
                View goalView = inflater.inflate(R.layout.goal_item, goalsContainer, false);

                ImageView ivGoalIcon = goalView.findViewById(R.id.ivGoalIcon);
                TextView tvGoalTitle = goalView.findViewById(R.id.tvGoalTitle);
                ProgressBar pbGoalProgress = goalView.findViewById(R.id.pbGoalProgress);
                TextView tvGoalProgress = goalView.findViewById(R.id.tvGoalProgress);
                TextView tvGoalDueDate = goalView.findViewById(R.id.tvGoalDueDate);

                ivGoalIcon.setImageResource(goal.getIconResId());
                tvGoalTitle.setText(goal.getTitle());

                int progress = 0;
                if (goal.getTargetAmount() > 0) {
                    progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
                }
                pbGoalProgress.setProgress(progress);

                String progressText = String.format(Locale.getDefault(), "₱%.2f / ₱%.2f", goal.getCurrentAmount(), goal.getTargetAmount());
                tvGoalProgress.setText(progressText);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvGoalDueDate.setText("Due by " + sdf.format(new Date(goal.getTargetDate())));

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
