package com.example.tipidmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tipidmate.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactions;
    private OnTransactionDeleteListener deleteListener;

    public interface OnTransactionDeleteListener {
        void onDeleteTransaction(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionDeleteListener deleteListener) {
        this.context = context;
        this.transactions = transactions;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set category icon and color
        holder.ivCategoryIcon.setImageResource(getCategoryIcon(transaction.getCategory()));
        holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.light_green_accent), PorterDuff.Mode.SRC_IN);

        // Set title (category name)
        holder.tvTransactionTitle.setText(transaction.getCategory());

        // Set note
        String note = transaction.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.tvTransactionNote.setText(note);
            holder.tvTransactionNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvTransactionNote.setVisibility(View.GONE);
        }

        // Format and set amount with color
        String amountText = String.format(Locale.getDefault(), "₱%.2f", transaction.getAmount());
        if ("expense".equalsIgnoreCase(transaction.getType())) {
            amountText = "- " + amountText;
            holder.tvTransactionAmount.setTextColor(Color.RED);
        } else {
            amountText = "+ " + amountText;
            holder.tvTransactionAmount.setTextColor(context.getResources().getColor(R.color.light_green_accent));
        }
        holder.tvTransactionAmount.setText(amountText);


        // Format and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(transaction.getTransactionDate());
            holder.tvTransactionDate.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvTransactionDate.setText(transaction.getTransactionDate());
        }

        holder.ivDeleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteTransaction(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon, ivDeleteIcon;
        TextView tvTransactionTitle, tvTransactionAmount, tvTransactionDate, tvTransactionNote;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivDeleteIcon = itemView.findViewById(R.id.ivDeleteIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionNote = itemView.findViewById(R.id.tvTransactionNote);
        }
    }

    private int getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "food": return R.drawable.ic_food;
            case "transport": return R.drawable.ic_transport;
            case "school": return R.drawable.ic_school;
            case "shopping": return R.drawable.ic_shopping;
            case "entertainment": return R.drawable.ic_fun;
            case "bills": return R.drawable.ic_bills;
            case "health": return R.drawable.ic_health;
            case "money":
            case "salary": 
                return R.drawable.ic_money;
            default: return R.drawable.ic_others;
        }
    }
}
