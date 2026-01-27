package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.GroupBudget;
import com.example.tipidmate.models.GroupBudgetMember;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewGroupBudgetActivity extends AppCompatActivity {

    private EditText budgetNameInput, totalBudgetInput, descriptionInput;
    private LinearLayout membersContainer;
    private TextView addMemberButton;
    private Button createBudgetButton;
    private ApiService apiService;
    private int userId;
    private List<String> memberNames = new ArrayList<>();
    private int[] avatarBackgrounds = {
            R.drawable.shape_circular_background_blue,
            R.drawable.shape_circular_background_purple,
            R.drawable.shape_circular_background_orange,
            R.drawable.shape_circular_background_red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_budget);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(NewGroupBudgetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup listeners
        setupListeners();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void initializeViews() {
        budgetNameInput = findViewById(R.id.budget_name_input);
        totalBudgetInput = findViewById(R.id.total_budget_input);
        descriptionInput = findViewById(R.id.description_input);
        membersContainer = findViewById(R.id.members_container);
        addMemberButton = findViewById(R.id.add_member_button);
        createBudgetButton = findViewById(R.id.create_budget_button);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        addMemberButton.setOnClickListener(v -> showAddMemberDialog());
        createBudgetButton.setOnClickListener(v -> createGroupBudget());
    }

    private void showAddMemberDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null);
        EditText etMemberName = dialogView.findViewById(R.id.etMemberName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String memberName = etMemberName.getText().toString().trim();

            if (TextUtils.isEmpty(memberName)) {
                etMemberName.setError("Name is required");
                return;
            }

            if (memberNames.contains(memberName)) {
                Toast.makeText(this, "Member already added", Toast.LENGTH_SHORT).show();
                return;
            }

            memberNames.add(memberName);
            addMemberToUI(memberName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addMemberToUI(String memberName) {
        View memberView = LayoutInflater.from(this).inflate(R.layout.member_item, membersContainer, false);

        TextView tvMemberAvatar = memberView.findViewById(R.id.tv_member_avatar);
        TextView tvMemberName = memberView.findViewById(R.id.tv_member_name);
        TextView tvPaymentStatus = memberView.findViewById(R.id.tv_payment_status);
        TextView tvMemberAmount = memberView.findViewById(R.id.tv_member_amount);
        ImageView ivDeleteMember = memberView.findViewById(R.id.iv_delete_member);

        // Generate initials
        String[] parts = memberName.split(" ");
        String initials = "";
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                initials += part.charAt(0);
                if (initials.length() >= 2) break;
            }
        }
        if (initials.isEmpty()) {
            initials = memberName.substring(0, Math.min(2, memberName.length()));
        }

        tvMemberAvatar.setText(initials.toUpperCase());
        tvMemberName.setText(memberName);
        tvPaymentStatus.setText("Not Paid");
        tvMemberAmount.setText("₱0.00");

        int colorIndex = Math.abs(memberName.hashCode()) % avatarBackgrounds.length;
        tvMemberAvatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

        // Show delete button and set click listener
        ivDeleteMember.setVisibility(View.VISIBLE);
        ivDeleteMember.setOnClickListener(v -> {
            memberNames.remove(memberName);
            membersContainer.removeView(memberView);
        });

        membersContainer.addView(memberView);
    }

    private void createGroupBudget() {
        // Get input values
        String budgetName = budgetNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String amountStr = totalBudgetInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(budgetName)) {
            budgetNameInput.setError("Budget name is required");
            budgetNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            totalBudgetInput.setError("Target amount is required");
            totalBudgetInput.requestFocus();
            return;
        }

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(amountStr);
            if (targetAmount <= 0) {
                totalBudgetInput.setError("Amount must be greater than 0");
                totalBudgetInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            totalBudgetInput.setError("Invalid amount");
            totalBudgetInput.requestFocus();
            return;
        }

        if (memberNames.isEmpty()) {
            Toast.makeText(this, "Please add at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        createBudgetButton.setEnabled(false);
        createBudgetButton.setText("Creating...");

        // Create GroupBudget object (using Monthly as default budget type)
        GroupBudget groupBudget = new GroupBudget(userId, budgetName, description, targetAmount, "Monthly");

        // Make API call to create group budget
        Call<ApiResponse<GroupBudget>> call = apiService.addGroupBudget(groupBudget);
        call.enqueue(new Callback<ApiResponse<GroupBudget>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupBudget>> call, Response<ApiResponse<GroupBudget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<GroupBudget> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Try to get group_budget_id from response
                        int groupBudgetId = -1;

                        if (apiResponse.getData() != null) {
                            groupBudgetId = apiResponse.getData().getGroupBudgetId();
                        }

                        // If we couldn't get from data, try parsing from message
                        if (groupBudgetId == -1) {
                            // The response might have group_budget_id in a different field
                            // Check the actual response structure
                            Toast.makeText(NewGroupBudgetActivity.this,
                                    "Group budget created! Adding members...",
                                    Toast.LENGTH_SHORT).show();

                            // For now, we'll need to get the ID from the success response
                            // You might need to update your PHP to return the full object
                        }

                        // Add members
                        addMembersToGroupBudget(groupBudgetId);
                    } else {
                        createBudgetButton.setEnabled(true);
                        createBudgetButton.setText("Create Budget");

                        Toast.makeText(NewGroupBudgetActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    createBudgetButton.setEnabled(true);
                    createBudgetButton.setText("Create Budget");

                    Toast.makeText(NewGroupBudgetActivity.this,
                            "Failed to create group budget. Code: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupBudget>> call, Throwable t) {
                createBudgetButton.setEnabled(true);
                createBudgetButton.setText("Create Budget");

                Toast.makeText(NewGroupBudgetActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void addMembersToGroupBudget(int groupBudgetId) {
        if (groupBudgetId == -1) {
            // If we don't have the ID, just finish and refresh the list
            Toast.makeText(this, "Group budget created! Please add members from the details screen.", Toast.LENGTH_LONG).show();
            createBudgetButton.setEnabled(true);
            createBudgetButton.setText("Create Budget");
            finish();
            return;
        }

        final int[] membersAdded = {0};
        final int totalMembers = memberNames.size();

        for (String memberName : memberNames) {
            GroupBudgetMember member = new GroupBudgetMember(groupBudgetId, memberName);

            Call<ApiResponse<GroupBudgetMember>> call = apiService.addMember(member);
            call.enqueue(new Callback<ApiResponse<GroupBudgetMember>>() {
                @Override
                public void onResponse(Call<ApiResponse<GroupBudgetMember>> call, Response<ApiResponse<GroupBudgetMember>> response) {
                    membersAdded[0]++;

                    if (membersAdded[0] == totalMembers) {
                        createBudgetButton.setEnabled(true);
                        createBudgetButton.setText("Create Budget");

                        Toast.makeText(NewGroupBudgetActivity.this,
                                "Group budget created successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate back
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<GroupBudgetMember>> call, Throwable t) {
                    membersAdded[0]++;

                    if (membersAdded[0] == totalMembers) {
                        createBudgetButton.setEnabled(true);
                        createBudgetButton.setText("Create Budget");

                        Toast.makeText(NewGroupBudgetActivity.this,
                                "Group budget created but some members failed to add",
                                Toast.LENGTH_LONG).show();

                        finish();
                    }
                }
            });
        }
    }
}
