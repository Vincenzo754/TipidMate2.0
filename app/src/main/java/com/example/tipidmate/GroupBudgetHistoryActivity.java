package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.GroupBudgetContribution;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupBudgetHistoryActivity extends AppCompatActivity implements GroupBudgetHistoryAdapter.OnContributionDeletedListener {

    private ApiService apiService;
    private int groupBudgetId;
    private int userId;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;
    private GroupBudgetHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_members_transaction_history);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get group budget ID
        groupBudgetId = getIntent().getIntExtra("group_budget_id", -1);

        if (groupBudgetId == -1) {
            Toast.makeText(this, "Group budget not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        rvHistory = findViewById(R.id.rv_transaction_history);
        tvNoHistory = findViewById(R.id.tv_no_history);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // Load contributions
        loadContributions();
    }

    private void loadContributions() {
        Call<ApiResponse<List<GroupBudgetContribution>>> call = apiService.getGroupContributions(groupBudgetId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudgetContribution>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudgetContribution>>> call, Response<ApiResponse<List<GroupBudgetContribution>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudgetContribution>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<GroupBudgetContribution> contributions = apiResponse.getData();

                        if (contributions.isEmpty()) {
                            rvHistory.setVisibility(View.GONE);
                            tvNoHistory.setVisibility(View.VISIBLE);
                        } else {
                            rvHistory.setVisibility(View.VISIBLE);
                            tvNoHistory.setVisibility(View.GONE);

                            adapter = new GroupBudgetHistoryAdapter(
                                    GroupBudgetHistoryActivity.this,
                                    contributions,
                                    GroupBudgetHistoryActivity.this
                            );
                            rvHistory.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudgetContribution>>> call, Throwable t) {
                Toast.makeText(GroupBudgetHistoryActivity.this,
                        "Failed to load contributions: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onContributionDeleted(GroupBudgetContribution contribution) {
        // Note: You'll need to create a delete_group_contribution.php endpoint
        // For now, just reload the list
        Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show();
        loadContributions();
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}