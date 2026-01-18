package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

public class GroupBudgetHistoryActivity extends AppCompatActivity implements GroupBudgetHistoryAdapter.OnContributionDeletedListener {

    private GroupBudget groupBudget;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;
    private GroupBudgetHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_budget_history);

        String groupBudgetId = getIntent().getStringExtra("group_budget_id");
        groupBudget = GroupBudgetRepository.getInstance().findGroupBudgetById(groupBudgetId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, new Intent()); // Signal that data may have changed
            finish();
        });

        rvHistory = findViewById(R.id.rv_history);
        tvNoHistory = findViewById(R.id.tv_no_history);

        if (groupBudget != null) {
            toolbar.setTitle(groupBudget.getTitle() + " History");
            updateHistoryList();
        } else {
            toolbar.setTitle("Transaction History");
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        }
    }

    private void updateHistoryList() {
        if (groupBudget.getContributions().isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            tvNoHistory.setVisibility(View.GONE);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new GroupBudgetHistoryAdapter(this, groupBudget.getContributions(), this);
            rvHistory.setAdapter(adapter);
        }
    }

    @Override
    public void onContributionDeleted(GroupContribution contribution) {
        groupBudget.removeContribution(contribution);
        updateHistoryList();
        // Also, signal to the details activity that it needs to refresh
        setResult(RESULT_OK, new Intent());
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent()); // Signal that data may have changed
        super.onBackPressed();
    }
}
