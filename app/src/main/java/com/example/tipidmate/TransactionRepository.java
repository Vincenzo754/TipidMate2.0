package com.example.tipidmate;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private static TransactionRepository instance;
    private final List<Transaction> transactions = new ArrayList<>();

    private TransactionRepository() {
        // Private constructor for singleton
    }

    public static synchronized TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(0, transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }
}
