package com.example.tipidmate;

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

    public List<Goal> getGoalList() {
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
            if (goal.getId().equals(id)) {
                return goal;
            }
        }
        return null;
    }

    public void updateGoal(Goal goal) {
        for (int i = 0; i < goalList.size(); i++) {
            if (goalList.get(i).getId().equals(goal.getId())) {
                goalList.set(i, goal);
                return;
            }
        }
    }
}
