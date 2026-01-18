package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class GroupBudgetAddActivity extends AppCompatActivity {

    private GroupBudget groupBudget;
    private TextView tvContributorName, tvContributorInitials;
    private EditText etAmount;
    private String selectedMember;
    private int[] avatarBackgrounds = {R.drawable.shape_circular_background_blue, R.drawable.shape_circular_background_purple, R.drawable.shape_circular_background_orange, R.drawable.shape_circular_background_red};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_add);

        String groupBudgetId = getIntent().getStringExtra("group_budget_id");
        groupBudget = GroupBudgetRepository.getInstance().findGroupBudgetById(groupBudgetId);

        if (groupBudget == null) {
            Toast.makeText(this, "Group budget not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvContributorName = findViewById(R.id.tv_contributor_name);
        tvContributorInitials = findViewById(R.id.tv_contributor_initials);
        etAmount = findViewById(R.id.et_amount);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RelativeLayout contributorSelector = findViewById(R.id.contributor_selector);
        contributorSelector.setOnClickListener(v -> showMemberSelectionDialog());

        Button saveButton = findViewById(R.id.btn_save_contribution);
        saveButton.setOnClickListener(v -> saveContribution());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        // Set initial contributor to the first member (usually "You")
        if (!groupBudget.getMembers().isEmpty()) {
            selectedMember = groupBudget.getMembers().get(0);
            updateContributorView(selectedMember);
        }
    }

    private void showMemberSelectionDialog() {
        List<String> members = groupBudget.getMembers();
        CharSequence[] memberNames = members.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Contributor");
        builder.setItems(memberNames, (dialog, which) -> {
            selectedMember = members.get(which);
            updateContributorView(selectedMember);
        });
        builder.show();
    }

    private void updateContributorView(String memberName) {
        tvContributorName.setText(memberName);
        tvContributorInitials.setText(String.valueOf(memberName.charAt(0)).toUpperCase());

        int colorIndex = Math.abs(memberName.hashCode()) % avatarBackgrounds.length;
        tvContributorInitials.setBackgroundResource(avatarBackgrounds[colorIndex]);
    }

    private void saveContribution() {
        String amountStr = etAmount.getText().toString();
        if (TextUtils.isEmpty(amountStr) || selectedMember == null) {
            Toast.makeText(this, "Please enter an amount and select a member.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        GroupContribution contribution = new GroupContribution(selectedMember, amount);
        groupBudget.addContribution(contribution);

        setResult(RESULT_OK);
        finish();
    }
}
