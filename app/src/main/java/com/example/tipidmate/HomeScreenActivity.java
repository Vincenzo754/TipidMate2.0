package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeScreenActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeletedListener {

    private TransactionRepository transactionRepository;
    private RecyclerView rvRecentTransactions;
    private TextView tvTotalBalance, tvSpentAmount, tvRemaining, tvBudgetAmount, tvBudgetLabel, tvNoTransactions, tvUserName, tvGreeting;
    private ProgressBar pbBudget;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        transactionRepository = TransactionRepository.getInstance();
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemaining = findViewById(R.id.tvRemaining);
        pbBudget = findViewById(R.id.pbBudget);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        tvBudgetLabel = findViewById(R.id.tvBudgetLabel);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        tvUserName = findViewById(R.id.tvUserName);
        tvGreeting = findViewById(R.id.tvGreeting);

        // Handle username display
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "User"); // Default to "User"
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            username = intent.getStringExtra("USERNAME");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USERNAME", username);
            editor.apply();
        }
        tvUserName.setText(username);

        Button btnIncome = findViewById(R.id.btnIncome);
        Button btnExpenses = findViewById(R.id.btnExpenses);
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

        tvViewDetails.setOnClickListener(v -> {
            Intent budgetIntent = new Intent(HomeScreenActivity.this, BudgetActivity.class);
            startActivity(budgetIntent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        setupRecyclerView();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactionRepository.getTransactions(), this);
        rvRecentTransactions.setAdapter(adapter);
    }

    public void updateUI() {
        updateGreeting();
        updateRecentTransactions();
        updateTotalBalance();
        updateBudgetStatus();
    }

    private void updateGreeting() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 6 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        tvGreeting.setText(greeting);
    }

    private void updateRecentTransactions() {
        List<Transaction> transactions = transactionRepository.getTransactions();
        if (transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvRecentTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvRecentTransactions.setVisibility(View.VISIBLE);
            adapter.updateTransactions(transactions);
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
        float budgetAmount = prefs.getFloat("budgetAmount", 0f);
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

    @Override
    public void onTransactionDeleted() {
        updateUI();
    }
}
