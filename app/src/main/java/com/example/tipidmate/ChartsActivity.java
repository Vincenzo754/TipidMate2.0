package com.example.tipidmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Goal;
import com.example.tipidmate.models.GroupBudget;
import com.example.tipidmate.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChartsActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart goalsPieChart;
    private LinearLayout goalsLegendContainer;
    private PieChart groupBudgetPieChart;
    private LinearLayout groupBudgetLegendContainer;
    private TextView barChartLastUpdated, goalsLastUpdated, groupBudgetLastUpdated;
    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts_screen);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChartsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize views
        barChart = findViewById(R.id.barChart);
        barChartLastUpdated = findViewById(R.id.bar_chart_last_updated);

        goalsPieChart = findViewById(R.id.pieChart);
        goalsLegendContainer = findViewById(R.id.legend_container);
        goalsLastUpdated = findViewById(R.id.goals_last_updated);

        groupBudgetPieChart = findViewById(R.id.groupBudgetPieChart);
        groupBudgetLegendContainer = findViewById(R.id.group_budget_legend_container);
        groupBudgetLastUpdated = findViewById(R.id.group_budget_last_updated);

        // Setup charts
        setupBarChart();
        setupPieChart(goalsPieChart, "Goals");
        setupPieChart(groupBudgetPieChart, "Group Budgets");

        // Load data from database
        loadBarChartData();
        loadGoalsPieChartData();
        loadGroupBudgetPieChartData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_charts);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBarChartData();
        loadGoalsPieChartData();
        loadGroupBudgetPieChartData();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_charts);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setValueFormatter(new LargeValueFormatter());

        Legend l = barChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(10f);

        LegendEntry legendEntryIncome = new LegendEntry("Income", Legend.LegendForm.SQUARE, 10f, 0f, null, ContextCompat.getColor(this, R.color.light_green_accent));
        LegendEntry legendEntryExpense = new LegendEntry("Expenses", Legend.LegendForm.SQUARE, 10f, 0f, null, Color.RED);

        l.setCustom(new LegendEntry[]{legendEntryIncome, legendEntryExpense});
    }

    private void loadBarChartData() {
        Call<ApiResponse<List<Transaction>>> call = apiService.getTransactions(userId);
        call.enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Transaction>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Transaction> transactions = apiResponse.getData();

                        if (transactions.isEmpty()) {
                            barChart.setVisibility(View.GONE);
                            TextView barChartTitle = findViewById(R.id.bar_chart_title);
                            if (barChartTitle != null) {
                                barChartTitle.setText("No Transactions Yet.");
                            }
                            return;
                        }

                        barChart.setVisibility(View.VISIBLE);
                        displayBarChart(transactions);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayBarChart(List<Transaction> transactions) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        String lastTransactionDate = "";

        // Show last 10 transactions
        int limit = Math.min(transactions.size(), 10);
        List<Transaction> recentTransactions = transactions.subList(0, limit);

        for (int i = 0; i < recentTransactions.size(); i++) {
            Transaction transaction = recentTransactions.get(i);
            entries.add(new BarEntry(i, (float) transaction.getAmount()));
            labels.add(transaction.getCategory());

            // Color based on type
            if (transaction.getType().equals("income")) {
                colors.add(ContextCompat.getColor(this, R.color.light_green_accent));
            } else {
                colors.add(Color.RED);
            }

            if (i == 0) {
                lastTransactionDate = transaction.getTransactionDate();
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelRotationAngle(-45);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();

        updateLastUpdatedFromDate(barChartLastUpdated, lastTransactionDate);
    }

    private void setupPieChart(PieChart chart, String centerText) {
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(80f);
        chart.setCenterText(centerText);
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(18f);

        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.getLegend().setEnabled(false);
    }

    private void loadGoalsPieChartData() {
        Call<ApiResponse<List<Goal>>> call = apiService.getGoals(userId);
        call.enqueue(new Callback<ApiResponse<List<Goal>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Goal>>> call, Response<ApiResponse<List<Goal>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Goal>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Goal> goals = apiResponse.getData();

                        if (goals.isEmpty()) {
                            goalsPieChart.setVisibility(View.GONE);
                            goalsLegendContainer.setVisibility(View.GONE);
                            TextView breakdownTitle = findViewById(R.id.breakdown_title);
                            if (breakdownTitle != null) {
                                breakdownTitle.setText("No Goals Yet.");
                            }
                            return;
                        }

                        goalsPieChart.setVisibility(View.VISIBLE);
                        goalsLegendContainer.setVisibility(View.VISIBLE);
                        displayGoalsPieChart(goals);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Goal>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayGoalsPieChart(List<Goal> goals) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        String lastGoalDate = "";

        for (Goal goal : goals) {
            entries.add(new PieEntry((float) goal.getTargetAmount(), goal.getGoalName()));
            dates.add(goal.getCreationDate());
            if (lastGoalDate.isEmpty()) {
                lastGoalDate = goal.getCreationDate();
            }
        }

        PieData data = createPieData(entries, "Goals");
        goalsPieChart.setData(data);
        goalsPieChart.invalidate();

        createCustomLegend(goalsLegendContainer, entries, data, dates);
        updateLastUpdatedFromDate(goalsLastUpdated, lastGoalDate);
    }

    private void loadGroupBudgetPieChartData() {
        Call<ApiResponse<List<GroupBudget>>> call = apiService.getGroupBudgets(userId);
        call.enqueue(new Callback<ApiResponse<List<GroupBudget>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupBudget>>> call, Response<ApiResponse<List<GroupBudget>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupBudget>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<GroupBudget> groupBudgets = apiResponse.getData();

                        if (groupBudgets.isEmpty()) {
                            groupBudgetPieChart.setVisibility(View.GONE);
                            groupBudgetLegendContainer.setVisibility(View.GONE);
                            TextView breakdownTitle = findViewById(R.id.group_budget_breakdown_title);
                            if (breakdownTitle != null) {
                                breakdownTitle.setText("No Group Budgets Yet.");
                            }
                            return;
                        }

                        groupBudgetPieChart.setVisibility(View.VISIBLE);
                        groupBudgetLegendContainer.setVisibility(View.VISIBLE);
                        displayGroupBudgetPieChart(groupBudgets);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GroupBudget>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayGroupBudgetPieChart(List<GroupBudget> groupBudgets) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        String lastBudgetDate = "";

        for (GroupBudget groupBudget : groupBudgets) {
            entries.add(new PieEntry((float) groupBudget.getTargetAmount(), groupBudget.getBudgetName()));
            dates.add(groupBudget.getCreatedAt());
            if (lastBudgetDate.isEmpty()) {
                lastBudgetDate = groupBudget.getCreatedAt();
            }
        }

        PieData data = createPieData(entries, "Group Budgets");
        groupBudgetPieChart.setData(data);
        groupBudgetPieChart.invalidate();

        createCustomLegend(groupBudgetLegendContainer, entries, data, dates);
        updateLastUpdatedFromDate(groupBudgetLastUpdated, lastBudgetDate);
    }

    private PieData createPieData(ArrayList<PieEntry> entries, String label) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        return new PieData(dataSet);
    }

    private void createCustomLegend(LinearLayout legendLayout, ArrayList<PieEntry> entries, PieData pieData, ArrayList<String> dates) {
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            float percentage = (entry.getValue() / pieData.getYValueSum()) * 100f;

            View legendItem = inflater.inflate(R.layout.legend_item, legendLayout, false);
            View legendColor = legendItem.findViewById(R.id.legend_color);
            TextView legendLabel = legendItem.findViewById(R.id.legend_label);
            TextView legendTimestamp = legendItem.findViewById(R.id.legend_timestamp);
            TextView legendPercentage = legendItem.findViewById(R.id.legend_percentage);

            legendColor.setBackgroundColor(((PieDataSet) pieData.getDataSet()).getColors().get(i % pieData.getDataSet().getColors().size()));
            legendLabel.setText(entry.getLabel());

            // Format date from database (yyyy-MM-dd HH:mm:ss)
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dates.get(i));
                legendTimestamp.setText(outputFormat.format(date));
            } catch (ParseException e) {
                legendTimestamp.setText(dates.get(i));
            }

            legendPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

            legendLayout.addView(legendItem);
        }
    }

    private void updateLastUpdatedFromDate(TextView textView, String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                // Try parsing transaction date format (yyyy-MM-dd)
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                textView.setText("Last Updated: " + outputFormat.format(date));
            } catch (ParseException e) {
                // Try parsing with time (yyyy-MM-dd HH:mm:ss)
                try {
                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
                    Date date = inputFormat2.parse(dateString);
                    textView.setText("Last Updated: " + outputFormat.format(date));
                } catch (ParseException e2) {
                    textView.setText("Last Updated: " + dateString);
                }
            }
        }
    }
}