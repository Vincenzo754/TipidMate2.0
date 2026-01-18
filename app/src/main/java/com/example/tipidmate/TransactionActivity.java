package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {

    private TextView tvExpenseTab, tvIncomeTab;
    private LinearLayout selectedCategory;
    private final Map<LinearLayout, TextView> categoryTextMap = new HashMap<>();
    private final Calendar calendar = Calendar.getInstance();
    private TextView tvDate, tvTime;
    private EditText etAmount, etNote;
    private String transactionType = "Expense"; // Default to Expense
    private int selectedIconResId = R.drawable.ic_food;
    private int selectedIconTint = R.color.light_green_accent;
    private String selectedCategoryName = "Food";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_screen);

        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);

        // Back Button
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // Expense/Income Toggle
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);

        // Set initial state from Intent
        String typeFromIntent = getIntent().getStringExtra("transactionType");
        if (typeFromIntent != null) {
            transactionType = typeFromIntent;
        }
        updateTabSelection();

        // Set click listeners
        tvExpenseTab.setOnClickListener(v -> {
            if (!transactionType.equals("Expense")) {
                transactionType = "Expense";
                updateTabSelection();
            }
        });

        tvIncomeTab.setOnClickListener(v -> {
            if (!transactionType.equals("Income")) {
                transactionType = "Income";
                updateTabSelection();
            }
        });

        // Category Selection
        setupCategoryListeners();

        // Date and Time Click Listeners
        LinearLayout llDateRow = findViewById(R.id.llDateRow);
        if (llDateRow != null) {
            llDateRow.setOnClickListener(v -> showDatePicker());
        }

        LinearLayout llTimeRow = findViewById(R.id.llTimeRow);
        if (llTimeRow != null) {
            llTimeRow.setOnClickListener(v -> showTimePicker());
        }

        // Continue Button
        Button btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                saveTransaction();
                finish();
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Note: No item is selected here as this is a transactional screen, not a main tab.
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);

        updateDateAndTime();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        // Expenses should always be negative, Income always positive
        if (transactionType.equals("Expense")) {
            amount = -Math.abs(amount);
        } else {
            amount = Math.abs(amount);
        }

        String note = etNote.getText().toString();
        if (note.isEmpty()) {
            note = selectedCategoryName;
        }

        Transaction transaction = new Transaction(note, calendar.getTimeInMillis(), amount, transactionType, selectedIconResId, selectedIconTint);
        TransactionRepository.getInstance().addTransaction(transaction);
    }

    private void updateTabSelection() {
        if ("Income".equals(transactionType)) {
            tvIncomeTab.setBackgroundResource(R.drawable.bg_tab_selected);
            tvIncomeTab.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvExpenseTab.setBackgroundResource(android.R.color.transparent);
            tvExpenseTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tvExpenseTab.setBackgroundResource(R.drawable.bg_tab_selected);
            tvExpenseTab.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvIncomeTab.setBackgroundResource(android.R.color.transparent);
            tvIncomeTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateAndTime();
        };

        new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateDateAndTime();
        };

        new TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void updateDateAndTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDate.setText(dateFormat.format(calendar.getTime()));

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void setupCategoryListeners() {
        Map<Integer, Integer> iconResIdMap = new HashMap<>();
        iconResIdMap.put(R.id.category_food, R.drawable.ic_food);
        iconResIdMap.put(R.id.category_transport, R.drawable.ic_transport);
        iconResIdMap.put(R.id.category_school, R.drawable.ic_school);
        iconResIdMap.put(R.id.category_fun, R.drawable.ic_fun);
        iconResIdMap.put(R.id.category_shopping, R.drawable.ic_shopping);
        iconResIdMap.put(R.id.category_bills, R.drawable.ic_bills);
        iconResIdMap.put(R.id.category_health, R.drawable.ic_health);
        iconResIdMap.put(R.id.category_more, R.drawable.ic_money);

        for (Map.Entry<Integer, Integer> entry : iconResIdMap.entrySet()) {
            LinearLayout categoryLayout = findViewById(entry.getKey());
            if (categoryLayout != null && categoryLayout.getChildCount() > 1 && categoryLayout.getChildAt(1) instanceof TextView) {
                TextView categoryTextView = (TextView) categoryLayout.getChildAt(1);
                categoryTextMap.put(categoryLayout, categoryTextView);

                categoryLayout.setOnClickListener(v -> {
                    selectCategory((LinearLayout) v);
                    selectedIconResId = iconResIdMap.get(v.getId());
                    selectedIconTint = R.color.light_green_accent;
                    selectedCategoryName = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                });
            }
        }

        // Set initial selection
        LinearLayout initialCategory = findViewById(R.id.category_food);
        if (initialCategory != null) {
            selectCategory(initialCategory);
        }
    }

    private void selectCategory(LinearLayout categoryLayout) {
        if (categoryLayout == null) {
            return;
        }

        // Unselect previous category
        if (selectedCategory != null) {
            if (selectedCategory.getChildCount() > 0 && selectedCategory.getChildAt(0) instanceof ImageView) {
                ImageView prevImageView = (ImageView) selectedCategory.getChildAt(0);
                prevImageView.setBackgroundResource(R.drawable.bg_category_unselected);
                prevImageView.setColorFilter(ContextCompat.getColor(this, R.color.light_gray_text));
            }
            TextView prevTextView = categoryTextMap.get(selectedCategory);
            if (prevTextView != null) {
                prevTextView.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
            }
        }

        // Select new category
        selectedCategory = categoryLayout;
        if (selectedCategory.getChildCount() > 0 && selectedCategory.getChildAt(0) instanceof ImageView) {
            ImageView imageView = (ImageView) selectedCategory.getChildAt(0);
            imageView.setBackgroundResource(R.drawable.bg_category_selected);
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.light_green_accent));
        }
        TextView textView = categoryTextMap.get(selectedCategory);
        if (textView != null) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }
}
