package com.example.tipidmate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartsActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart goalsPieChart;
    private LinearLayout goalsLegendContainer;
    private PieChart groupBudgetPieChart;
    private LinearLayout groupBudgetLegendContainer;
    private TextView barChartLastUpdated, goalsLastUpdated, groupBudgetLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts_screen);

        // Bar Chart
        barChart = findViewById(R.id.barChart);
        barChartLastUpdated = findViewById(R.id.bar_chart_last_updated);
        setupBarChart();
        loadBarChartData();

        // Goals Chart
        goalsPieChart = findViewById(R.id.pieChart);
        goalsLegendContainer = findViewById(R.id.legend_container);
        goalsLastUpdated = findViewById(R.id.goals_last_updated);
        setupPieChart(goalsPieChart, "Goals");
        loadGoalsPieChartData();

        // Group Budget Chart
        groupBudgetPieChart = findViewById(R.id.groupBudgetPieChart);
        groupBudgetLegendContainer = findViewById(R.id.group_budget_legend_container);
        groupBudgetLastUpdated = findViewById(R.id.group_budget_last_updated);
        setupPieChart(groupBudgetPieChart, "Group Budgets");
        loadGroupBudgetPieChartData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_charts);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
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
        TransactionRepository transactionRepository = TransactionRepository.getInstance();
        List<Transaction> transactions = transactionRepository.getTransactions();

        if (transactions.isEmpty()) {
            barChart.setVisibility(View.GONE);
            TextView barChartTitle = findViewById(R.id.bar_chart_title);
            if (barChartTitle != null) {
                barChartTitle.setText("No Data yet.");
            }
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        long lastTransactionTimestamp = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            entries.add(new BarEntry(i, (float) transaction.amount));
            labels.add(transaction.title);
            if (transaction.amount < 0) {
                colors.add(Color.RED);
            } else {
                colors.add(ContextCompat.getColor(this, R.color.light_green_accent));
            }

            if (transaction.timestamp > lastTransactionTimestamp) {
                lastTransactionTimestamp = transaction.timestamp;
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

        updateLastUpdated(barChartLastUpdated, lastTransactionTimestamp);
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
        GoalRepository goalRepository = GoalRepository.getInstance();
        List<Goal> goals = goalRepository.getGoalList();

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Long> timestamps = new ArrayList<>();
        long lastGoalTimestamp = 0;
        if (goals.isEmpty()) {
            findViewById(R.id.pieChart).setVisibility(View.GONE);
            findViewById(R.id.legend_container).setVisibility(View.GONE);
            TextView breakdownTitle = findViewById(R.id.breakdown_title);
            if(breakdownTitle != null) {
                breakdownTitle.setText("No Goals Yet.");
            }
            return;
        }

        for (Goal goal : goals) {
            entries.add(new PieEntry((float) goal.getTargetAmount(), goal.getTitle()));
            timestamps.add(goal.getTimestamp());
            if (goal.getTimestamp() > lastGoalTimestamp) {
                lastGoalTimestamp = goal.getTimestamp();
            }
        }

        PieData data = createPieData(entries, "Goals");
        goalsPieChart.setData(data);
        goalsPieChart.invalidate();

        createCustomLegend(goalsLegendContainer, entries, data, timestamps);
        updateLastUpdated(goalsLastUpdated, lastGoalTimestamp);
    }

    private void loadGroupBudgetPieChartData() {
        GroupBudgetRepository groupBudgetRepository = GroupBudgetRepository.getInstance();
        List<GroupBudget> groupBudgets = groupBudgetRepository.getGroupBudgets();

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Long> timestamps = new ArrayList<>();
        long lastGroupBudgetTimestamp = 0;
        if (groupBudgets.isEmpty()) {
            findViewById(R.id.groupBudgetPieChart).setVisibility(View.GONE);
            findViewById(R.id.group_budget_legend_container).setVisibility(View.GONE);
            TextView breakdownTitle = findViewById(R.id.group_budget_breakdown_title);
            if(breakdownTitle != null) {
                breakdownTitle.setText("No Group Budgets Yet.");
            }
            return;
        }

        for (GroupBudget groupBudget : groupBudgets) {
            entries.add(new PieEntry((float) groupBudget.getTargetAmount(), groupBudget.getTitle()));
            timestamps.add(groupBudget.getTimestamp());
            if (groupBudget.getTimestamp() > lastGroupBudgetTimestamp) {
                lastGroupBudgetTimestamp = groupBudget.getTimestamp();
            }
        }

        PieData data = createPieData(entries, "Group Budgets");
        groupBudgetPieChart.setData(data);
        groupBudgetPieChart.invalidate();

        createCustomLegend(groupBudgetLegendContainer, entries, data, timestamps);
        updateLastUpdated(groupBudgetLastUpdated, lastGroupBudgetTimestamp);
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

    private void createCustomLegend(LinearLayout legendLayout, ArrayList<PieEntry> entries, PieData pieData, ArrayList<Long> timestamps) {
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            float percentage = (entry.getValue() / pieData.getYValueSum()) * 100f;

            View legendItem = inflater.inflate(R.layout.legend_item, legendLayout, false);
            View legendColor = legendItem.findViewById(R.id.legend_color);
            TextView legendLabel = legendItem.findViewById(R.id.legend_label);
            TextView legendTimestamp = legendItem.findViewById(R.id.legend_timestamp);
            TextView legendPercentage = legendItem.findViewById(R.id.legend_percentage);

            legendColor.setBackgroundColor(((PieDataSet)pieData.getDataSet()).getColors().get(i % pieData.getDataSet().getColors().size()));
            legendLabel.setText(entry.getLabel());
            legendTimestamp.setText(sdf.format(new Date(timestamps.get(i))));
            legendPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

            legendLayout.addView(legendItem);
        }
    }

    private void updateLastUpdated(TextView textView, long timestamp) {
        if (timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            textView.setText("Last Updated: " + sdf.format(new Date(timestamp)));
        }
    }
}
