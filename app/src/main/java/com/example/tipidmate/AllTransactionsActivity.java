package com.example.tipidmate;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllTransactionsActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeletedListener {

    private TransactionAdapter adapter;
    private TransactionRepository transactionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_transactions_screen);

        transactionRepository = TransactionRepository.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvAllTransactions = findViewById(R.id.rvAllTransactions);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));

        List<Transaction> transactions = transactionRepository.getTransactions();
        adapter = new TransactionAdapter(this, transactions, this);
        rvAllTransactions.setAdapter(adapter);
    }

    @Override
    public void onTransactionDeleted() {
        adapter.updateTransactions(transactionRepository.getTransactions());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.updateTransactions(transactionRepository.getTransactions());
        }
    }
}
