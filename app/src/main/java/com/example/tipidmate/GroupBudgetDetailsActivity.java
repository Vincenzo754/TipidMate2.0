package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.GroupBudget;
import com.example.tipidmate.models.GroupBudgetMember;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupBudgetDetailsActivity extends AppCompatActivity {

    private ApiService apiService;
    private int groupBudgetId;
    private int userId;
    private GroupBudget currentGroupBudget;
    private final int[] avatarBackgrounds = {
            R.drawable.shape_circular_background_blue,
            R.drawable.shape_circular_background_purple,
            R.drawable.shape_circular_background_orange,
            R.drawable.shape_circular_background_red
    };
    private static final int ADD_CONTRIBUTION_REQUEST = 1;
    private static final int VIEW_HISTORY_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_details);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GroupBudgetDetailsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get group budget ID
        groupBudgetId = getIntent().getIntExtra("group_budget_id", -1);

        if (groupBudgetId == -1) {
            Toast.makeText(this, "Group budget not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        FloatingActionButton fabAddContribution = findViewById(R.id.fab_add_contribution);
        fabAddContribution.setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetDetailsActivity.this, GroupBudgetAddActivity.class);
            intent.putExtra("group_budget_id", groupBudgetId);
            startActivityForResult(intent, ADD_CONTRIBUTION_REQUEST);
        });

        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetDetailsActivity.this, GroupBudgetHistoryActivity.class);
            intent.putExtra("group_budget_id", groupBudgetId);
            startActivityForResult(intent, VIEW_HISTORY_REQUEST);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        // Load data
        loadGroupBudgetDetails();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_CONTRIBUTION_REQUEST || requestCode == VIEW_HISTORY_REQUEST) && resultCode == RESULT_OK) {
            loadGroupBudgetDetails();
        }
    }

    private void loadGroupBudgetDetails() {
        // Load group budget info
        Call<ApiResponse<List<GroupBudget>>> call = apiService.getGroupBudgets(userId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudget>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudget>>> call, Response<ApiResponse<List<GroupBudget>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudget>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        for (GroupBudget gb : apiResponse.getData()) {
                            if (gb.getGroupBudgetId() == groupBudgetId) {
                                currentGroupBudget = gb;
                                displayGroupBudgetInfo(gb);
                                loadMembers();
                                return;
                            }
                        }
                        Toast.makeText(GroupBudgetDetailsActivity.this,
                                "Group budget not found",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudget>>> call, Throwable t) {
                Toast.makeText(GroupBudgetDetailsActivity.this,
                        "Failed to load group budget: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void displayGroupBudgetInfo(GroupBudget groupBudget) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(groupBudget.getBudgetName());

        TextView subtitle = findViewById(R.id.group_budget_subtitle);
        TextView amount = findViewById(R.id.group_budget_amount);
        TextView target = findViewById(R.id.group_budget_target);
        TextView percentage = findViewById(R.id.group_budget_percentage);
        TextView status = findViewById(R.id.group_budget_status);
        ProgressBar progressBar = findViewById(R.id.group_budget_progress);
        TextView memberCount = findViewById(R.id.group_budget_member_count);

        subtitle.setText(groupBudget.getDescription());
        memberCount.setText(String.format(Locale.getDefault(), "%d people", groupBudget.getMemberCount()));

        double currentAmount = groupBudget.getCurrentAmount();
        double targetAmount = groupBudget.getTargetAmount();
        int progress = 0;
        if (targetAmount > 0) {
            progress = (int) ((currentAmount / targetAmount) * 100);
        }

        amount.setText(String.format(Locale.getDefault(), "₱%.2f", currentAmount));
        target.setText(String.format(Locale.getDefault(), "/ ₱%.2f", targetAmount));
        percentage.setText(String.format(Locale.getDefault(), "%d%% Spent", progress));
        progressBar.setProgress(progress);

        if (progress >= 100) {
            status.setText("Goal Reached!");
            status.setTextColor(ContextCompat.getColor(this, R.color.light_green_accent));
        } else if (progress >= 75) {
            status.setText("Almost There!");
            status.setTextColor(ContextCompat.getColor(this, R.color.light_green_accent));
        } else {
            status.setText("On Track");
            status.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
        }
    }

    private void loadMembers() {
        Call<ApiResponse<List<GroupBudgetMember>>> call = apiService.getMembers(groupBudgetId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudgetMember>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudgetMember>>> call, Response<ApiResponse<List<GroupBudgetMember>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudgetMember>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        displayMembers(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudgetMember>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayMembers(List<GroupBudgetMember> members) {
        LinearLayout membersContainer = findViewById(R.id.members_container);
        membersContainer.removeAllViews();

        if (members.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No members yet. Add members to start tracking contributions.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
            tvEmpty.setPadding(0, 16, 0, 16);
            membersContainer.addView(tvEmpty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (GroupBudgetMember member : members) {
            View memberView = inflater.inflate(R.layout.member_item, membersContainer, false);

            TextView tvMemberAvatar = memberView.findViewById(R.id.tv_member_avatar);
            TextView tvMemberName = memberView.findViewById(R.id.tv_member_name);
            TextView tvPaymentStatus = memberView.findViewById(R.id.tv_payment_status);
            TextView tvMemberAmount = memberView.findViewById(R.id.tv_member_amount);

            tvMemberName.setText(member.getMemberName());
            tvMemberAvatar.setText(member.getInitials());

            int colorIndex = Math.abs(member.getMemberName().hashCode()) % avatarBackgrounds.length;
            tvMemberAvatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

            double memberContribution = member.getTotalContributed();
            tvMemberAmount.setText(String.format(Locale.getDefault(), "₱%.2f", memberContribution));

            if (memberContribution > 0) {
                tvPaymentStatus.setText("Paid");
                tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.light_green_accent));
            } else {
                tvPaymentStatus.setText("Pending");
                tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
            }

            // Hide delete icon on this screen
            View deleteIcon = memberView.findViewById(R.id.iv_delete_member);
            if (deleteIcon != null) {
                deleteIcon.setVisibility(View.GONE);
            }

            membersContainer.addView(memberView);
        }
    }
}