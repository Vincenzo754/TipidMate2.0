package com.example.tipidmate;

import java.util.ArrayList;
import java.util.List;

public class GroupBudgetRepository {
    private static GroupBudgetRepository instance;
    private final List<GroupBudget> groupBudgets = new ArrayList<>();

    private GroupBudgetRepository() {
        // private constructor
    }

    public static synchronized GroupBudgetRepository getInstance() {
        if (instance == null) {
            instance = new GroupBudgetRepository();
        }
        return instance;
    }

    public List<GroupBudget> getGroupBudgets() {
        return groupBudgets;
    }

    public void addGroupBudget(GroupBudget groupBudget) {
        groupBudgets.add(groupBudget);
    }

    public void removeGroupBudget(GroupBudget groupBudget) {
        groupBudgets.remove(groupBudget);
    }

    public GroupBudget findGroupBudgetById(String id) {
        for (GroupBudget groupBudget : groupBudgets) {
            if (groupBudget.getId().equals(id)) {
                return groupBudget;
            }
        }
        return null;
    }
}
