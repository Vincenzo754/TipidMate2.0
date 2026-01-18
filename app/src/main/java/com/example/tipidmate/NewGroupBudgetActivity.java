package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NewGroupBudgetActivity extends AppCompatActivity {

    private List<String> members = new ArrayList<>();
    private LinearLayout membersContainer;
    private EditText budgetNameInput, totalBudgetInput, descriptionInput;
    private int[] avatarBackgrounds = {R.drawable.shape_circular_background_blue, R.drawable.shape_circular_background_purple, R.drawable.shape_circular_background_orange, R.drawable.shape_circular_background_red};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_budget);

        membersContainer = findViewById(R.id.members_container);
        budgetNameInput = findViewById(R.id.budget_name_input);
        totalBudgetInput = findViewById(R.id.total_budget_input);
        descriptionInput = findViewById(R.id.description_input);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView addMemberButton = findViewById(R.id.add_member_button);
        addMemberButton.setOnClickListener(v -> showAddMemberDialog());

        Button createBudgetButton = findViewById(R.id.create_budget_button);
        createBudgetButton.setOnClickListener(v -> createGroupBudget());

        // Add the admin user by default
        addMember("You", true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.group_budget_add_member, null);
        builder.setView(dialogView);

        final EditText etMemberName = dialogView.findViewById(R.id.etMemberName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);

        final AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String memberName = etMemberName.getText().toString().trim();
            if (!memberName.isEmpty()) {
                addMember(memberName, false);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addMember(String memberName, boolean isAdmin) {
        if (members.contains(memberName)) return; // Prevent duplicate members
        members.add(memberName);

        LayoutInflater inflater = LayoutInflater.from(this);
        View memberView = inflater.inflate(R.layout.member_item, membersContainer, false);

        TextView tvMemberAvatar = memberView.findViewById(R.id.tv_member_avatar);
        TextView tvMemberName = memberView.findViewById(R.id.tv_member_name);
        TextView tvPaymentStatus = memberView.findViewById(R.id.tv_payment_status);

        tvMemberName.setText(memberName);
        tvMemberAvatar.setText(String.valueOf(memberName.charAt(0)).toUpperCase());
        
        int colorIndex = Math.abs(memberName.hashCode()) % avatarBackgrounds.length;
        tvMemberAvatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

        if (isAdmin) {
            tvPaymentStatus.setText("Admin");
        } else {
            tvPaymentStatus.setText("Member");
        }

        // Hide amount view on this screen
        TextView tvMemberAmount = memberView.findViewById(R.id.tv_member_amount);
        if (tvMemberAmount != null) {
            tvMemberAmount.setVisibility(View.GONE);
        }

        membersContainer.addView(memberView);
    }

    private void createGroupBudget() {
        String title = budgetNameInput.getText().toString().trim();
        String totalAmountStr = totalBudgetInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(totalAmountStr)) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountStr);

        GroupBudget newGroupBudget = new GroupBudget(title, description, totalAmount, members);
        GroupBudgetRepository.getInstance().addGroupBudget(newGroupBudget);

        finish(); // Go back to the GroupBudgetActivity screen
    }
}
