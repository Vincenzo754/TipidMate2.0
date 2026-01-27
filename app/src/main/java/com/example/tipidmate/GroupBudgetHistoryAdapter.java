package com.example.tipidmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tipidmate.models.GroupBudgetContribution;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupBudgetHistoryAdapter extends RecyclerView.Adapter<GroupBudgetHistoryAdapter.ViewHolder> {

    private final List<GroupBudgetContribution> contributions;
    private final Context context;
    private final OnContributionDeletedListener listener;
    private final int[] avatarBackgrounds = {
            R.drawable.shape_circular_background_blue,
            R.drawable.shape_circular_background_purple,
            R.drawable.shape_circular_background_orange,
            R.drawable.shape_circular_background_red
    };

    public interface OnContributionDeletedListener {
        void onContributionDeleted(GroupBudgetContribution contribution);
    }

    public GroupBudgetHistoryAdapter(Context context, List<GroupBudgetContribution> contributions, OnContributionDeletedListener listener) {
        this.context = context;
        this.contributions = contributions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_budget_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupBudgetContribution contribution = contributions.get(position);

        holder.tvContributorName.setText(contribution.getMemberName());
        holder.tvContributionAmount.setText(String.format(Locale.getDefault(), "₱%.2f", contribution.getAmount()));

        // Format date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(contribution.getContributionDate());
            holder.tvContributionDate.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvContributionDate.setText(contribution.getContributionDate());
        }

        holder.tvContributorAvatar.setText(contribution.getInitials());
        int colorIndex = Math.abs(contribution.getMemberName().hashCode()) % avatarBackgrounds.length;
        holder.tvContributorAvatar.setBackgroundResource(avatarBackgrounds[colorIndex]);

        holder.ivDeleteContribution.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Contribution")
                    .setMessage("Are you sure you want to delete this contribution?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (listener != null) {
                            listener.onContributionDeleted(contribution);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return contributions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContributorAvatar, tvContributorName, tvContributionDate, tvContributionAmount;
        ImageView ivDeleteContribution;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContributorAvatar = itemView.findViewById(R.id.tv_contributor_avatar);
            tvContributorName = itemView.findViewById(R.id.tv_contributor_name);
            tvContributionDate = itemView.findViewById(R.id.tv_contribution_date);
            tvContributionAmount = itemView.findViewById(R.id.tv_contribution_amount);
            ivDeleteContribution = itemView.findViewById(R.id.iv_delete_contribution);
        }
    }
}