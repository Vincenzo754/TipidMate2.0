package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeScreenActivity extends AppCompatActivity {

    private TransactionRepository transactionRepository;
    private LinearLayout llTransactions;
    private TextView tvTotalBalance, tvSpentAmount, tvRemaining, tvBudgetAmount, tvBudgetLabel, tvNoTransactions, tvUserName;
    private ProgressBar pbBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        transactionRepository = TransactionRepository.getInstance();
        llTransactions = findViewById(R.id.llTransactions);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemaining = findViewById(R.id.tvRemaining);
        pbBudget = findViewById(R.id.pbBudget);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        tvBudgetLabel = findViewById(R.id.tvBudgetLabel);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        tvUserName = findViewById(R.id.tvUserName);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            String username = intent.getStringExtra("USERNAME");
            tvUserName.setText(username);
        }

        Button btnIncome = findViewById(R.id.btnIncome);
        Button btnExpenses = findViewById(R.id.btnExpenses);
        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        TextView tvViewDetails = findViewById(R.id.tvViewDetails);

        btnIncome.setOnClickListener(v -> {
            Intent transactionIntent = new Intent(HomeScreenActivity.this, TransactionActivity.class);
            transactionIntent.putExtra("transactionType", "Income");
            startActivity(transactionIntent);
        });

        btnExpenses.setOnClickListener(v -> {
            Intent transactionIntent = new Intent(HomeScreenActivity.this, TransactionActivity.class);
            transactionIntent.putExtra("transactionType", "Expense");
            startActivity(transactionIntent);
        });

        tvSeeAll.setOnClickListener(v -> {
            Intent allTransactionsIntent = new Intent(HomeScreenActivity.this, AllTransactionsActivity.class);
            startActivity(allTransactionsIntent);
        });

        tvViewDetails.setOnClickListener(v -> {
            Intent budgetIntent = new Intent(HomeScreenActivity.this, BudgetActivity.class);
            startActivity(budgetIntent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_charts) {
                startActivity(new Intent(getApplicationContext(), ChartsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_goals) {
                startActivity(new Intent(getApplicationContext(), GoalsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_group_budget) {
                startActivity(new Intent(getApplicationContext(), GroupBudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        updateRecentTransactions();
        updateTotalBalance();
        updateBudgetStatus();
    }

    private void updateRecentTransactions() {
        llTransactions.removeAllViews();
        List<Transaction> transactions = transactionRepository.getTransactions();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
        }

        int count = 0;
        for (Transaction transaction : transactions) {
            if (count >= 5) {
                break;
            }
            View transactionView = inflater.inflate(R.layout.transaction_item, llTransactions, false);

            ImageView ivCategoryIcon = transactionView.findViewById(R.id.ivCategoryIcon);
            TextView tvTransactionTitle = transactionView.findViewById(R.id.tvTransactionTitle);
            TextView tvTransactionDate = transactionView.findViewById(R.id.tvTransactionDate);
            TextView tvTransactionAmount = transactionView.findViewById(R.id.tvTransactionAmount);

            ivCategoryIcon.setImageResource(transaction.iconResId);
            ivCategoryIcon.setColorFilter(ContextCompat.getColor(this, transaction.iconTint));
            tvTransactionTitle.setText(transaction.title);
            tvTransactionDate.setText(new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).format(new Date(transaction.timestamp)));

            String amountString = String.format(Locale.getDefault(), "%.2f", Math.abs(transaction.amount));
            if (transaction.amount < 0) {
                tvTransactionAmount.setText("-₱" + amountString);
                tvTransactionAmount.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tvTransactionAmount.setText("+₱" + amountString);
                tvTransactionAmount.setTextColor(ContextCompat.getColor(this, R.color.light_green_accent));
            }

            llTransactions.addView(transactionView);
            count++;
        }
    }

    private void updateTotalBalance() {
        double totalBalance = 0;
        for (Transaction transaction : transactionRepository.getTransactions()) {
            totalBalance += transaction.amount;
        }
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₱%.2f", totalBalance));
    }

    private void updateBudgetStatus() {
        SharedPreferences prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
        float budgetAmount = prefs.getFloat("budgetAmount", 10000);
        String budgetFrequency = prefs.getString("budgetFrequency", "Monthly");

        tvBudgetAmount.setText(String.format(Locale.getDefault(), "₱%.0f", budgetAmount));
        tvBudgetLabel.setText(budgetFrequency + " Budget");

        double totalSpent = 0;
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

        for (Transaction transaction : transactionRepository.getTransactions()) {
            if (transaction.type.equals("Expense")) {
                calendar.setTimeInMillis(transaction.timestamp);
                if ("Monthly".equals(budgetFrequency)) {
                    if (calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                        totalSpent += Math.abs(transaction.amount);
                    }
                } else { // Daily
                    if (calendar.get(Calendar.DAY_OF_YEAR) == currentDay && calendar.get(Calendar.YEAR) == currentYear) {
                        totalSpent += Math.abs(transaction.amount);
                    }
                }
            }
        }

        double spentPercent = 0.0;
        if (budgetAmount > 0) {
            spentPercent = (totalSpent / budgetAmount) * 100;
        }

        tvSpentAmount.setText(String.format(Locale.getDefault(), "₱%.2f", totalSpent));
        tvRemaining.setText(String.format(Locale.getDefault(), "%.0f%%", spentPercent));

        pbBudget.setProgress((int) spentPercent);
    }
}
