package com.example.tipidmate;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupBudgetActivity extends AppCompatActivity {

    private LinearLayout groupBudgetsContainer;
    private ScrollView scrollView;
    private LinearLayout emptyState;
    private GroupBudgetRepository groupBudgetRepository;
    private EditText etSearch;
    private int[] avatarBackgrounds = {R.drawable.shape_circular_background_blue, R.drawable.shape_circular_background_purple, R.drawable.shape_circular_background_orange, R.drawable.shape_circular_background_red};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_budget_screen);

        groupBudgetsContainer = findViewById(R.id.groupBudgetsContainer);
        scrollView = findViewById(R.id.scrollGroupBudgets);
        emptyState = findViewById(R.id.emptyState);
        groupBudgetRepository = GroupBudgetRepository.getInstance();
        etSearch = findViewById(R.id.etSearch);

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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGroupBudgetList();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_group_budget);
    }

    private void updateGroupBudgetList() {
        filterGroupBudgets(etSearch.getText().toString());
    }

    private void filterGroupBudgets(String query) {
        groupBudgetsContainer.removeAllViews();
        List<GroupBudget> allGroupBudgets = groupBudgetRepository.getGroupBudgets();
        List<GroupBudget> filteredGroupBudgets = new ArrayList<>();

        for (GroupBudget groupBudget : allGroupBudgets) {
            if (groupBudget.getTitle().toLowerCase().contains(query.toLowerCase())) {
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
            LayoutInflater inflater = LayoutInflater.from(this);
            for (GroupBudget groupBudget : filteredGroupBudgets) {
                View groupBudgetView = inflater.inflate(R.layout.group_budget_items, groupBudgetsContainer, false);

                TextView title = groupBudgetView.findViewById(R.id.group_budget_title);
                TextView subtitle = groupBudgetView.findViewById(R.id.group_budget_subtitle);
                TextView amount = groupBudgetView.findViewById(R.id.group_budget_amount);
                TextView percentage = groupBudgetView.findViewById(R.id.group_budget_percentage);
                ProgressBar progressBar = groupBudgetView.findViewById(R.id.group_budget_progress);
                LinearLayout avatarsContainer = groupBudgetView.findViewById(R.id.group_budget_avatars);
                ImageView deleteButton = groupBudgetView.findViewById(R.id.ivDeleteGroupBudget);

                title.setText(groupBudget.getTitle());
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

                avatarsContainer.removeAllViews();
                int maxAvatars = 3;
                for (int i = 0; i < groupBudget.getMembers().size(); i++) {
                    if (i < maxAvatars) {
                        String member = groupBudget.getMembers().get(i);
                        TextView avatar = new TextView(this);
                        avatar.setText(String.valueOf(member.charAt(0)).toUpperCase());
                        int sizeInDp = 32;
                        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getResources().getDisplayMetrics());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                        if (i > 0) {
                            params.setMarginStart(-20);
                        }
                        avatar.setLayoutParams(params);
                        
                        int colorIndex = Math.abs(member.hashCode()) % avatarBackgrounds.length;
                        avatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

                        avatar.setGravity(android.view.Gravity.CENTER);
                        avatar.setTextColor(getResources().getColor(R.color.white));
                        avatarsContainer.addView(avatar);
                    } else {
                        TextView more = new TextView(this);
                        more.setText("+" + (groupBudget.getMembers().size() - maxAvatars));
                        int sizeInDp = 32;
                        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getResources().getDisplayMetrics());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                        params.setMarginStart(-20);
                        more.setLayoutParams(params);
                        more.setBackgroundResource(R.drawable.shape_circular_background);
                        more.setGravity(android.view.Gravity.CENTER);
                        more.setTextColor(getResources().getColor(R.color.white));
                        avatarsContainer.addView(more);
                        break;
                    }
                }

                deleteButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Group Budget")
                            .setMessage("Are you sure you want to delete this group budget?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                groupBudgetRepository.removeGroupBudget(groupBudget);
                                updateGroupBudgetList();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                groupBudgetView.setOnClickListener(v -> {
                    Intent intent = new Intent(GroupBudgetActivity.this, GroupBudgetDetailsActivity.class);
                    intent.putExtra("group_budget_id", groupBudget.getId());
                    startActivity(intent);
                });

                groupBudgetsContainer.addView(groupBudgetView);
            }
        }
    }
}
