package com.example.tipidmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class GroupBudgetDetailsActivity extends AppCompatActivity {

    private GroupBudget groupBudget;
    private final int[] avatarBackgrounds = {R.drawable.shape_circular_background_blue, R.drawable.shape_circular_background_purple, R.drawable.shape_circular_background_orange, R.drawable.shape_circular_background_red};
    private static final int ADD_CONTRIBUTION_REQUEST = 1;
    private static final int VIEW_HISTORY_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_details);

        String groupBudgetId = getIntent().getStringExtra("group_budget_id");
        groupBudget = GroupBudgetRepository.getInstance().findGroupBudgetById(groupBudgetId);

        if (groupBudget == null) {
            Toast.makeText(this, "Group budget not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(groupBudget.getTitle());
        toolbar.setNavigationOnClickListener(v -> finish());

        FloatingActionButton fabAddContribution = findViewById(R.id.fab_add_contribution);
        fabAddContribution.setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetDetailsActivity.this, GroupBudgetAddActivity.class);
            intent.putExtra("group_budget_id", groupBudget.getId());
            startActivityForResult(intent, ADD_CONTRIBUTION_REQUEST);
        });

        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(GroupBudgetDetailsActivity.this, GroupBudgetHistoryActivity.class);
            intent.putExtra("group_budget_id", groupBudget.getId());
            startActivityForResult(intent, VIEW_HISTORY_REQUEST);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_CONTRIBUTION_REQUEST || requestCode == VIEW_HISTORY_REQUEST) && resultCode == RESULT_OK) {
            // Data has changed from either adding or deleting a contribution, so refresh the UI
            updateUI();
        }
    }

    private void updateUI() {
        // Update summary card
        TextView subtitle = findViewById(R.id.group_budget_subtitle);
        TextView amount = findViewById(R.id.group_budget_amount);
        TextView target = findViewById(R.id.group_budget_target);
        TextView percentage = findViewById(R.id.group_budget_percentage);
        ProgressBar progressBar = findViewById(R.id.group_budget_progress);
        TextView memberCount = findViewById(R.id.group_budget_member_count);
        
        subtitle.setText(groupBudget.getDescription());
        memberCount.setText(String.format(Locale.getDefault(), "%d people", groupBudget.getMembers().size()));

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


        // Update members list
        LinearLayout membersContainer = findViewById(R.id.members_container);
        membersContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String member : groupBudget.getMembers()) {
            View memberView = inflater.inflate(R.layout.member_item, membersContainer, false);

            TextView tvMemberAvatar = memberView.findViewById(R.id.tv_member_avatar);
            TextView tvMemberName = memberView.findViewById(R.id.tv_member_name);
            TextView tvPaymentStatus = memberView.findViewById(R.id.tv_payment_status);
            TextView tvMemberAmount = memberView.findViewById(R.id.tv_member_amount);

            tvMemberName.setText(member);
            tvMemberAvatar.setText(String.valueOf(member.charAt(0)).toUpperCase());
            
            int colorIndex = Math.abs(member.hashCode()) % avatarBackgrounds.length;
            tvMemberAvatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

            double memberContribution = groupBudget.getMemberContribution(member);
            tvMemberAmount.setText(String.format(Locale.getDefault(), "₱%.2f", memberContribution));

            if (memberContribution > 0) {
                tvPaymentStatus.setText("Paid");
                tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.light_green_accent));
            } else {
                tvPaymentStatus.setText("Pending");
                tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
            }
            
            // Hide delete icon on this screen, it's handled in the history
            View deleteIcon = memberView.findViewById(R.id.iv_delete_member);
            if(deleteIcon != null) {
                deleteIcon.setVisibility(View.GONE);
            }

            membersContainer.addView(memberView);
        }
    }
}
