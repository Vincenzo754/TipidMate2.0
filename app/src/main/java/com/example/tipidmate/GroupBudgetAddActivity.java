package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.GroupBudgetContribution;
import com.example.tipidmate.models.GroupBudgetMember;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupBudgetAddActivity extends AppCompatActivity {

    private ApiService apiService;
    private int groupBudgetId;
    private int userId;
    private TextView tvContributorName, tvContributorInitials;
    private EditText etAmount;
    private Button btnSaveContribution;
    private GroupBudgetMember selectedMember;
    private List<GroupBudgetMember> members;
    private int[] avatarBackgrounds = {
            R.drawable.shape_circular_background_blue,
            R.drawable.shape_circular_background_purple,
            R.drawable.shape_circular_background_orange,
            R.drawable.shape_circular_background_red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_add);

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
            Toast.makeText(this, "Group budget not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvContributorName = findViewById(R.id.tv_contributor_name);
        tvContributorInitials = findViewById(R.id.tv_contributor_initials);
        etAmount = findViewById(R.id.et_amount);
        btnSaveContribution = findViewById(R.id.btn_save_contribution);

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RelativeLayout contributorSelector = findViewById(R.id.contributor_selector);
        contributorSelector.setOnClickListener(v -> showMemberSelectionDialog());

        btnSaveContribution.setOnClickListener(v -> saveContribution());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        // Load members
        loadMembers();
    }

    private void loadMembers() {
        Call<ApiResponse<List<GroupBudgetMember>>> call = apiService.getMembers(groupBudgetId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudgetMember>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudgetMember>>> call, Response<ApiResponse<List<GroupBudgetMember>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudgetMember>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        members = apiResponse.getData();

                        if (!members.isEmpty()) {
                            selectedMember = members.get(0);
                            updateContributorView(selectedMember);
                        } else {
                            Toast.makeText(GroupBudgetAddActivity.this,
                                    "No members found. Please add members first.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudgetMember>>> call, Throwable t) {
                Toast.makeText(GroupBudgetAddActivity.this,
                        "Failed to load members: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void showMemberSelectionDialog() {
        if (members == null || members.isEmpty()) {
            Toast.makeText(this, "No members available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] memberNames = new String[members.size()];
        for (int i = 0; i < members.size(); i++) {
            memberNames[i] = members.get(i).getMemberName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Contributor");
        builder.setItems(memberNames, (dialog, which) -> {
            selectedMember = members.get(which);
            updateContributorView(selectedMember);
        });
        builder.show();
    }

    private void updateContributorView(GroupBudgetMember member) {
        tvContributorName.setText(member.getMemberName());
        tvContributorInitials.setText(member.getInitials());

        int colorIndex = Math.abs(member.getMemberName().hashCode()) % avatarBackgrounds.length;
        tvContributorInitials.setBackgroundResource(avatarBackgrounds[colorIndex]);
    }

    private void saveContribution() {
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        if (selectedMember == null) {
            Toast.makeText(this, "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        // Show loading
        btnSaveContribution.setEnabled(false);
        btnSaveContribution.setText("Saving...");

        // Create contribution
        GroupBudgetContribution contribution = new GroupBudgetContribution(
                groupBudgetId,
                selectedMember.getMemberId(),
                amount
        );

        Call<ApiResponse<GroupBudgetContribution>> call = apiService.addGroupContribution(contribution);
        call.enqueue(new Callback<ApiResponse<GroupBudgetContribution>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupBudgetContribution>> call, Response<ApiResponse<GroupBudgetContribution>> response) {
                btnSaveContribution.setEnabled(true);
                btnSaveContribution.setText("Save Contribution");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GroupBudgetContribution> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(GroupBudgetAddActivity.this,
                                "Contribution added successfully!",
                                Toast.LENGTH_SHORT).show();

                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(GroupBudgetAddActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupBudgetAddActivity.this,
                            "Failed to add contribution",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupBudgetContribution>> call, Throwable t) {
                btnSaveContribution.setEnabled(true);
                btnSaveContribution.setText("Save Contribution");

                Toast.makeText(GroupBudgetAddActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}