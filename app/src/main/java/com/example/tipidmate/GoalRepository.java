package com.example.tipidmate;

import com.example.tipidmate.models.Goal;

import java.util.ArrayList;
import java.util.List;

public class GoalRepository {
    private static GoalRepository instance;
    private List<Goal> goalList;

    private GoalRepository() {
        goalList = new ArrayList<>();
    }

    public static synchronized GoalRepository getInstance() {
        if (instance == null) {
            instance = new GoalRepository();
        }
        return instance;
    }

    public List<Goal> getGoals() {
        return goalList;
    }

    public void addGoal(Goal goal) {
        goalList.add(goal);
    }

    public void removeGoal(Goal goal) {
        goalList.remove(goal);
    }

    public Goal findGoalById(String id) {
        for (Goal goal : goalList) {
            if (String.valueOf(goal.getGoalId()).equals(id)) {
                return goal;
            }
        }
        return null;
    }

    public void updateGoal(Goal goal) {
        for (int i = 0; i < goalList.size(); i++) {
            if (goalList.get(i).getGoalId() == goal.getGoalId()) {
                goalList.set(i, goal);
                return;
            }
        }
    }
}
