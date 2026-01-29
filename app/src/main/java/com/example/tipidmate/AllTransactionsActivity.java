package com.example.tipidmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllTransactionsActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {

    private ImageView ivBack;
    private RecyclerView rvAllTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_transactions_screen);

        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            // Handle user not logged in
            finish();
            return;
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadAllTransactions();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        rvAllTransactions = findViewById(R.id.rvAllTransactions);
    }

    private void setupRecyclerView() {
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, allTransactions, this);
        rvAllTransactions.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadAllTransactions() {
        Call<ApiResponse<List<Transaction>>> call = apiService.getTransactions(userId);
        call.enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allTransactions.clear();
                    allTransactions.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AllTransactionsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                Toast.makeText(AllTransactionsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
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
                        Toast.makeText(AllTransactionsActivity.this,
                                "Transaction deleted successfully",
                                Toast.LENGTH_SHORT).show();
                        loadAllTransactions(); // Reload data
                    } else {
                        Toast.makeText(AllTransactionsActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AllTransactionsActivity.this,
                            "Failed to delete transaction",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AllTransactionsActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}
