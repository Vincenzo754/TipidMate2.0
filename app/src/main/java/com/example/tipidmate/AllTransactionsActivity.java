package com.example.tipidmate;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllTransactionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_transactions_screen);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvAllTransactions = findViewById(R.id.rvAllTransactions);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        TransactionAdapter adapter = new TransactionAdapter(TransactionRepository.getInstance().getTransactions());
        rvAllTransactions.setAdapter(adapter);
    }
}
