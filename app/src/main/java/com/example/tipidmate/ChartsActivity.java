package com.example.tipidmate;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChartsActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart goalsPieChart;
    private LinearLayout goalsLegendContainer;
    private PieChart groupBudgetPieChart;
    private LinearLayout groupBudgetLegendContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts_screen);

        // Bar Chart
        barChart = findViewById(R.id.barChart);
        setupBarChart();
        loadBarChartData();

        // Goals Chart
        goalsPieChart = findViewById(R.id.pieChart);
        goalsLegendContainer = findViewById(R.id.legend_container);
        setupPieChart(goalsPieChart, "Goals");
        loadGoalsPieChartData();

        // Group Budget Chart
        groupBudgetPieChart = findViewById(R.id.groupBudgetPieChart);
        groupBudgetLegendContainer = findViewById(R.id.group_budget_legend_container);
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
        xAxis.setCenterAxisLabels(true); // Center labels under the bars

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setValueFormatter(new LargeValueFormatter());

        Legend l = barChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    private void loadBarChartData() {
        TransactionRepository transactionRepository = TransactionRepository.getInstance();
        List<Transaction> transactions = transactionRepository.getTransactions();

        Map<String, float[]> categoryTotals = new HashMap<>();
        Set<String> categorySet = new LinkedHashSet<>();

        for (Transaction transaction : transactions) {
            String category = transaction.title; // Using title as the category
            categorySet.add(category);
            float[] totals = categoryTotals.getOrDefault(category, new float[2]);
            if (transaction.amount > 0) {
                totals[0] += transaction.amount; // Index 0 for Income
            } else {
                totals[1] += Math.abs(transaction.amount); // Index 1 for Expenses
            }
            categoryTotals.put(category, totals);
        }

        List<String> labels = new ArrayList<>(categorySet);
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            String category = labels.get(i);
            float[] totals = categoryTotals.get(category);
            incomeEntries.add(new BarEntry(i, totals[0]));
            expenseEntries.add(new BarEntry(i, totals[1]));
        }

        if (labels.isEmpty()) {
            barChart.setVisibility(View.GONE);
            TextView barChartTitle = findViewById(R.id.bar_chart_title);
            if (barChartTitle != null) {
                barChartTitle.setText("No data yet.");
            }
            return;
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(ContextCompat.getColor(this, R.color.light_green_accent));
        incomeDataSet.setDrawValues(false);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.RED);
        expenseDataSet.setDrawValues(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        float groupSpace = 0.4f;
        float barSpace = 0.05f;
        float barWidth = 0.25f;

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barChart.setData(barData);
        barData.setBarWidth(barWidth);
        barChart.getXAxis().setAxisMinimum(0);
        // Set axisMaximum so the centered labels don't get cut off
        barChart.getXAxis().setAxisMaximum(labels.size());
        barChart.groupBars(0, groupSpace, barSpace);
        barChart.invalidate();
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
        if (goals.isEmpty()) {
            findViewById(R.id.pieChart).setVisibility(View.GONE);
            findViewById(R.id.legend_container).setVisibility(View.GONE);
            TextView breakdownTitle = findViewById(R.id.breakdown_title);
            if(breakdownTitle != null) {
                breakdownTitle.setText("No Goals yet.");
            }
            return;
        }

        for (Goal goal : goals) {
            entries.add(new PieEntry((float) goal.getTargetAmount(), goal.getTitle()));
        }

        PieData data = createPieData(entries, "Goals");
        goalsPieChart.setData(data);
        goalsPieChart.invalidate();

        createCustomLegend(goalsLegendContainer, entries, data);
    }

    private void loadGroupBudgetPieChartData() {
        GroupBudgetRepository groupBudgetRepository = GroupBudgetRepository.getInstance();
        List<GroupBudget> groupBudgets = groupBudgetRepository.getGroupBudgets();

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (groupBudgets.isEmpty()) {
            findViewById(R.id.groupBudgetPieChart).setVisibility(View.GONE);
            findViewById(R.id.group_budget_legend_container).setVisibility(View.GONE);
            TextView breakdownTitle = findViewById(R.id.group_budget_breakdown_title);
            if(breakdownTitle != null) {
                breakdownTitle.setText("No Group Budgets yet.");
            }
            return;
        }

        for (GroupBudget groupBudget : groupBudgets) {
            entries.add(new PieEntry((float) groupBudget.getTargetAmount(), groupBudget.getTitle()));
        }

        PieData data = createPieData(entries, "Group Budgets");
        groupBudgetPieChart.setData(data);
        groupBudgetPieChart.invalidate();

        createCustomLegend(groupBudgetLegendContainer, entries, data);
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

    private void createCustomLegend(LinearLayout legendLayout, ArrayList<PieEntry> entries, PieData pieData) {
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            float percentage = (entry.getValue() / pieData.getYValueSum()) * 100f;

            View legendItem = inflater.inflate(R.layout.legend_item, legendLayout, false);
            View legendColor = legendItem.findViewById(R.id.legend_color);
            TextView legendLabel = legendItem.findViewById(R.id.legend_label);
            TextView legendPercentage = legendItem.findViewById(R.id.legend_percentage);

            legendColor.setBackgroundColor(((PieDataSet)pieData.getDataSet()).getColors().get(i % pieData.getDataSet().getColors().size()));
            legendLabel.setText(entry.getLabel());
            legendPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

            legendLayout.addView(legendItem);
        }
    }
}
