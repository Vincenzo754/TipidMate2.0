package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Budget;
import com.example.tipidmate.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreenActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {

    // Header views
    private TextView tvGreeting, tvUserName;

    // Balance card views
    private TextView tvTotalBalance;
    private MaterialButton btnIncome, btnExpenses;

    // Budget card views
    private TextView tvBudgetLabel, tvBudgetAmount, tvSpentAmount, tvRemaining, tvViewDetails;
    private ProgressBar pbBudget;
    private androidx.cardview.widget.CardView cvBudget;

    // Recent section views
    private TextView tvHistory, tvNoTransactions, tvSeeHistoryMessage;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> recentTransactions = new ArrayList<>();

    private ApiService apiService;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        // Get user info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        userName = prefs.getString("username", "User");

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize views
        initializeViews();

        // Set greeting and username
        tvGreeting.setText(getGreeting());
        tvUserName.setText(userName);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load data from database
        loadTransactionsData();
        loadBudgetData();

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data every time user returns to home screen
        loadTransactionsData();
        loadBudgetData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void initializeViews() {
        // Header
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);

        // Balance card
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpenses = findViewById(R.id.btnExpenses);

        // Budget card
        tvBudgetLabel = findViewById(R.id.tvBudgetLabel);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvViewDetails = findViewById(R.id.tvViewDetails);
        pbBudget = findViewById(R.id.pbBudget);
        cvBudget = findViewById(R.id.cvBudget);

        // Recent section
        tvHistory = findViewById(R.id.tvHistory);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        tvSeeHistoryMessage = findViewById(R.id.tvSeeHistoryMessage);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'this' as the delete listener so delete works
        adapter = new TransactionAdapter(this, recentTransactions, this);
        rvRecentTransactions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Income button - navigate to transaction screen (income)
        btnIncome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreenActivity.this, TransactionActivity.class);
            intent.putExtra("type", "income");
            startActivity(intent);
        });

        // Expense button - navigate to transaction screen (expense)
        btnExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreenActivity.this, TransactionActivity.class);
            intent.putExtra("type", "expense");
            startActivity(intent);
        });

        // History - see all transactions
        tvHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreenActivity.this, AllTransactionsActivity.class);
            startActivity(intent);
        });

        // View Details - open budget activity
        tvViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreenActivity.this, BudgetActivity.class);
            startActivity(intent);
        });

        // Budget card click - also opens budget activity
        cvBudget.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreenActivity.this, BudgetActivity.class);
            startActivity(intent);
        });
    }

    private void loadTransactionsData() {
        Call<ApiResponse<List<Transaction>>> call = apiService.getTransactions(userId);
        call.enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Transaction>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Transaction> transactions = apiResponse.getData();

                        if (transactions.isEmpty()) {
                            showNoTransactionsState();
                        } else {
                            // Calculate totals
                            calculateAndDisplayTotals(transactions);

                            // Display recent transactions (last 3)
                            displayRecentTransactions(transactions);
                        }
                    } else {
                        showNoTransactionsState();
                    }
                } else {
                    showNoTransactionsState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                Toast.makeText(HomeScreenActivity.this,
                        "Failed to load data: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();

                showNoTransactionsState();
            }
        });
    }

    private void calculateAndDisplayTotals(List<Transaction> transactions) {
        double totalIncome = 0.0;
        double totalExpenses = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("income")) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpenses += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpenses;

        // Update Balance Card
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₱%.2f", balance));

        // Update Budget Card (showing expense info)
        tvBudgetAmount.setText(String.format(Locale.getDefault(), "₱%.2f", totalIncome));
        tvSpentAmount.setText(String.format(Locale.getDefault(), " ₱%.2f", totalExpenses));

        // Calculate percentage spent
        if (totalIncome > 0) {
            int percentage = (int) ((totalExpenses / totalIncome) * 100);
            percentage = Math.min(percentage, 100); // Cap at 100%
            pbBudget.setProgress(percentage);
            tvRemaining.setText(percentage + "%");
        } else {
            pbBudget.setProgress(0);
            tvRemaining.setText("0%");
        }
    }

    private void displayRecentTransactions(List<Transaction> transactions) {
        // Clear the list
        recentTransactions.clear();

        // Show only the most recent 3 transactions
        int limit = Math.min(transactions.size(), 3);
        recentTransactions.addAll(transactions.subList(0, limit));

        // Update UI
        rvRecentTransactions.setVisibility(View.VISIBLE);
        tvNoTransactions.setVisibility(View.GONE);

        // Show "See more" message if there are more than 3 transactions
        if (transactions.size() > 3) {
            tvSeeHistoryMessage.setVisibility(View.VISIBLE);
        } else {
            tvSeeHistoryMessage.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private void showNoTransactionsState() {
        // Show zeros
        tvTotalBalance.setText("₱0.00");
        tvBudgetAmount.setText("₱0");
        tvSpentAmount.setText(" ₱0");
        tvRemaining.setText("0%");
        pbBudget.setProgress(0);

        // Show no transactions message
        rvRecentTransactions.setVisibility(View.GONE);
        tvNoTransactions.setVisibility(View.VISIBLE);
        tvSeeHistoryMessage.setVisibility(View.GONE);
    }

    @Override
    public void onDeleteTransaction(Transaction transaction) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction(transaction))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        ApiService.DeleteRequest deleteRequest = new ApiService.DeleteRequest(transaction.getTransactionId());

        Call<ApiResponse<Void>> call = apiService.deleteTransaction(deleteRequest);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(HomeScreenActivity.this,
                                "Transaction deleted successfully",
                                Toast.LENGTH_SHORT).show();
                        loadTransactionsData(); // Reload data
                    } else {
                        Toast.makeText(HomeScreenActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeScreenActivity.this,
                            "Failed to delete transaction",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(HomeScreenActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void loadBudgetData() {
        Call<ApiResponse<Budget>> call = apiService.getActiveBudget(userId);
        call.enqueue(new Callback<ApiResponse<Budget>>() {
            @Override
            public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Budget> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Budget budget = apiResponse.getData();
                        displayBudgetInfo(budget);
                    } else {
                        // No budget set - show placeholder
                        showNoBudgetState();
                    }
                } else {
                    showNoBudgetState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                showNoBudgetState();
                t.printStackTrace();
            }
        });
    }

    private void displayBudgetInfo(Budget budget) {
        tvBudgetLabel.setText(budget.getFrequency().substring(0, 1).toUpperCase() +
                budget.getFrequency().substring(1) + " Budget");
        tvBudgetAmount.setText(String.format(Locale.getDefault(), "₱%.0f", budget.getAmount()));
        tvSpentAmount.setText(String.format(Locale.getDefault(), " ₱%.2f", budget.getSpent()));

        int percentage = (int) budget.getPercentage();
        percentage = Math.min(percentage, 100); // Cap at 100%
        pbBudget.setProgress(percentage);
        tvRemaining.setText(percentage + "%");
    }

    private void showNoBudgetState() {
        tvBudgetLabel.setText("No Budget Set");
        tvBudgetAmount.setText("₱0");
        tvSpentAmount.setText(" ₱0");
        tvRemaining.setText("0%");
        pbBudget.setProgress(0);
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }
}
