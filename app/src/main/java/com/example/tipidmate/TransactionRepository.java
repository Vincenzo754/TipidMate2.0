package com.example.tipidmate;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private static TransactionRepository instance;
    private final List<Transaction> transactions = new ArrayList<>();

    private TransactionRepository() {
        // Dummy data removed to start with an empty list
    }

    public static synchronized TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(0, transaction);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }
}
