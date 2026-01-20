package com.example.tipidmate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    public static void setupBottomNavigationView(BottomNavigationView bottomNavigationView, final Context context) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                if (!(context instanceof HomeScreenActivity)) {
                    Intent intent = new Intent(context, HomeScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                }
                return true;
            } else if (itemId == R.id.navigation_charts) {
                if (!(context instanceof ChartsActivity)) {
                    Intent intent = new Intent(context, ChartsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                }
                return true;
            } else if (itemId == R.id.navigation_goals) {
                if (!(context instanceof GoalsActivity)) {
                    Intent intent = new Intent(context, GoalsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                }
                return true;
            } else if (itemId == R.id.navigation_group_budget) {
                if (!(context instanceof GroupBudgetActivity)) {
                    Intent intent = new Intent(context, GroupBudgetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                }
                return true;
            }
            return false;
        });

        bottomNavigationView.setOnItemReselectedListener(item -> {
            // Do nothing. This prevents re-selecting the same item from reloading the activity.
        });
    }
}
