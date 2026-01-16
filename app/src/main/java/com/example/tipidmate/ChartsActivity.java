package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts_screen);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_charts);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_charts) {
                return true;
            } else if (itemId == R.id.navigation_goals) {
                startActivity(new Intent(getApplicationContext(), GoalsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_group_budget) {
                startActivity(new Intent(getApplicationContext(), GroupBudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
