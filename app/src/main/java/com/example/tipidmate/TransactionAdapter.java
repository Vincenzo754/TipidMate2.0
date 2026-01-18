package com.example.tipidmate;

import android.app.AlertDialog;
import android.content.Context;
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

    private final List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
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
        holder.bind(transaction, position);
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
        private final Context context;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivDeleteIcon = itemView.findViewById(R.id.ivDeleteIcon);
        }

        public void bind(Transaction transaction, int position) {
            ivCategoryIcon.setImageResource(transaction.iconResId);
            ivCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), transaction.iconTint));
            tvTransactionTitle.setText(transaction.title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            tvTransactionDate.setText(dateFormat.format(new Date(transaction.timestamp)));

            String amountString = String.format(Locale.getDefault(), "%.2f", Math.abs(transaction.amount));
            if (transaction.amount < 0) {
                tvTransactionAmount.setText("-₱" + amountString);
                tvTransactionAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                tvTransactionAmount.setText("+₱" + amountString);
                tvTransactionAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.light_green_accent));
            }

            ivDeleteIcon.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            TransactionRepository.getInstance().removeTransaction(transaction);
                            transactions.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, transactions.size());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}
