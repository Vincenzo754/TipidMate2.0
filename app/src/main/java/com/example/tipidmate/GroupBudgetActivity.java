package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.GroupBudget;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupBudgetActivity extends AppCompatActivity {

    private LinearLayout groupBudgetsContainer;
    private ScrollView scrollView;
    private LinearLayout emptyState;
    private EditText etSearch;
    private ApiService apiService;
    private int userId;
    private List<GroupBudget> allGroupBudgets = new ArrayList<>();
    private int[] avatarBackgrounds = {
            R.drawable.shape_circular_background_blue,
            R.drawable.shape_circular_background_purple,
            R.drawable.shape_circular_background_orange,
            R.drawable.shape_circular_background_red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_screen);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GroupBudgetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        groupBudgetsContainer = findViewById(R.id.groupBudgetsContainer);
        scrollView = findViewById(R.id.scrollGroupBudgets);
        emptyState = findViewById(R.id.emptyState);
        etSearch = findViewById(R.id.etSearch);

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        FloatingActionButton btnAddGroupBudget = findViewById(R.id.btnAddGroupBudget);
        btnAddGroupBudget.setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetActivity.this, NewGroupBudgetActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroupBudgets(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetActivity.this, HomeScreenActivity.class);
            startActivity(intent);
        });

        // Load group budgets from database
        loadGroupBudgets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupBudgets();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
    }

    private void loadGroupBudgets() {
        Call<ApiResponse<List<GroupBudget>>> call = apiService.getGroupBudgets(userId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudget>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudget>>> call, Response<ApiResponse<List<GroupBudget>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudget>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        allGroupBudgets = apiResponse.getData();
                        if (allGroupBudgets == null) {
                            allGroupBudgets = new ArrayList<>();
                        }
                        filterGroupBudgets(etSearch.getText().toString());
                    } else {
                        Toast.makeText(GroupBudgetActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudget>>> call, Throwable t) {
                Toast.makeText(GroupBudgetActivity.this,
                        "Failed to load group budgets: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void filterGroupBudgets(String query) {
        groupBudgetsContainer.removeAllViews();
        List<GroupBudget> filteredGroupBudgets = new ArrayList<>();

        for (GroupBudget groupBudget : allGroupBudgets) {
            if (groupBudget.getBudgetName().toLowerCase().contains(query.toLowerCase())) {
                filteredGroupBudgets.add(groupBudget);
            }
        }

        if (filteredGroupBudgets.isEmpty()) {
            if (allGroupBudgets.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            }
        } else {
            emptyState.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            displayGroupBudgets(filteredGroupBudgets);
        }
    }

    private void displayGroupBudgets(List<GroupBudget> groupBudgets) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (GroupBudget groupBudget : groupBudgets) {
            View groupBudgetView = inflater.inflate(R.layout.group_budget_items, groupBudgetsContainer, false);

            TextView title = groupBudgetView.findViewById(R.id.group_budget_title);
            TextView subtitle = groupBudgetView.findViewById(R.id.group_budget_subtitle);
            TextView amount = groupBudgetView.findViewById(R.id.group_budget_amount);
            TextView percentage = groupBudgetView.findViewById(R.id.group_budget_percentage);
            ProgressBar progressBar = groupBudgetView.findViewById(R.id.group_budget_progress);
            LinearLayout avatarsContainer = groupBudgetView.findViewById(R.id.group_budget_avatars);
            ImageView deleteButton = groupBudgetView.findViewById(R.id.ivDeleteGroupBudget);

            title.setText(groupBudget.getBudgetName());
            subtitle.setText(groupBudget.getDescription());

            double currentAmount = groupBudget.getCurrentAmount();
            double targetAmount = groupBudget.getTargetAmount();
            int progress = 0;
            if (targetAmount > 0) {
                progress = (int) ((currentAmount / targetAmount) * 100);
            }

            amount.setText(String.format(Locale.getDefault(), "₱%.2f / ₱%.2f", currentAmount, targetAmount));
            percentage.setText(String.format(Locale.getDefault(), "%d%%", progress));
            progressBar.setProgress(progress);

            // Show member count instead of avatars for now
            // (You'll need to load members separately if you want to show avatars)
            TextView memberCountView = new TextView(this);
            memberCountView.setText(groupBudget.getMemberCount() + " members");
            memberCountView.setTextColor(getResources().getColor(R.color.light_gray_text));
            avatarsContainer.addView(memberCountView);

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Group Budget")
                        .setMessage("Are you sure you want to delete this group budget?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteGroupBudget(groupBudget);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            groupBudgetView.setOnClickListener(v -> {
                Intent intent = new Intent(GroupBudgetActivity.this, GroupBudgetDetailsActivity.class);
                intent.putExtra("group_budget_id", groupBudget.getGroupBudgetId());
                startActivity(intent);
            });

            groupBudgetsContainer.addView(groupBudgetView);
        }
    }

    private void deleteGroupBudget(GroupBudget groupBudget) {
        Call<ApiResponse<Void>> call = apiService.deleteGroupBudget(groupBudget);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(GroupBudgetActivity.this,
                                "Group budget deleted successfully",
                                Toast.LENGTH_SHORT).show();
                        loadGroupBudgets();
                    } else {
                        Toast.makeText(GroupBudgetActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(GroupBudgetActivity.this,
                        "Failed to delete group budget: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}