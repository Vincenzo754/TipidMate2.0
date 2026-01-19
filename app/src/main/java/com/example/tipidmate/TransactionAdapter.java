package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private final Context context;
    private final OnTransactionDeletedListener listener;

    public interface OnTransactionDeletedListener {
        void onTransactionDeleted();
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionDeletedListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    public void updateTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCategoryIcon;
        private final TextView tvTransactionTitle;
        private final TextView tvTransactionDate;
        private final TextView tvTransactionAmount;
        private final ImageView ivDeleteIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivDeleteIcon = itemView.findViewById(R.id.ivDeleteIcon);

            ivDeleteIcon.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                TransactionRepository.getInstance().deleteTransaction(transactions.get(position));
                                if (listener != null) {
                                    listener.onTransactionDeleted();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        public void bind(Transaction transaction) {
            ivCategoryIcon.setImageResource(transaction.iconResId);
            ivCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), transaction.iconTint));
            tvTransactionTitle.setText(transaction.title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            tvTransactionDate.setText(dateFormat.format(new Date(transaction.timestamp)));

            String amountString = String.format(Locale.getDefault(), "%.2f", Math.abs(transaction.amount));
            if (transaction.amount < 0) {
                tvTransactionAmount.setText("-₱" + amountString);
                tvTransactionAmount.setTextColor(Color.RED);
            } else {
                tvTransactionAmount.setText("+₱" + amountString);
                tvTransactionAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.light_green_accent));
            }
        }
    }
}
