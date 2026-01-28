package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tipidmate.api.ApiService;
import com.example.tipidmate.api.RetrofitClient;
import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionActivity extends AppCompatActivity {

    private TextView tvDate, tvTime, tvExpenseTab, tvIncomeTab;
    private EditText etNote, etAmount;
    private MaterialButton btnContinue;
    private ImageView ivBack;
    private LinearLayout llDateRow, llTimeRow;

    // Category views
    private LinearLayout categoryFood, categoryTransport, categoryShopping, categoryEntertainment;
    private LinearLayout categoryBills, categoryHealth, categoryMoney, categorySchool;

    private String selectedCategory = "";
    private String selectedDate = "";
    private String selectedTime = "";
    private String transactionType = "expense"; // default
    private int userId;
    private ApiService apiService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_screen);

        // Get user info
        SharedPreferences prefs = getSharedPreferences("TipidMatePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TransactionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize views
        initializeViews();

        // Set default date and time
        updateDateDisplay();
        updateTimeDisplay();

        // Setup listeners
        setupListeners();

        // Set initial tab state
        selectTab(transactionType);

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void initializeViews() {
        etAmount = findViewById(R.id.etAmount);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        etNote = findViewById(R.id.etNote);
        btnContinue = findViewById(R.id.btnContinue);
        ivBack = findViewById(R.id.ivBack);
        llDateRow = findViewById(R.id.llDateRow);
        llTimeRow = findViewById(R.id.llTimeRow);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);

        // Category views
        categoryFood = findViewById(R.id.category_food);
        categoryTransport = findViewById(R.id.category_transport);
        categoryShopping = findViewById(R.id.category_shopping);
        categoryEntertainment = findViewById(R.id.category_fun);
        categoryBills = findViewById(R.id.category_bills);
        categoryHealth = findViewById(R.id.category_health);
        categoryMoney = findViewById(R.id.category_more);
        categorySchool = findViewById(R.id.category_school);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Tab listeners
        tvExpenseTab.setOnClickListener(v -> selectTab("expense"));
        tvIncomeTab.setOnClickListener(v -> selectTab("income"));

        // Category listeners
        categoryFood.setOnClickListener(v -> selectCategory("Food", categoryFood));
        categoryTransport.setOnClickListener(v -> selectCategory("Transport", categoryTransport));
        categoryShopping.setOnClickListener(v -> selectCategory("Shopping", categoryShopping));
        categoryEntertainment.setOnClickListener(v -> selectCategory("Entertainment", categoryEntertainment));
        categoryBills.setOnClickListener(v -> selectCategory("Bills", categoryBills));
        categoryHealth.setOnClickListener(v -> selectCategory("Health", categoryHealth));
        categoryMoney.setOnClickListener(v -> selectCategory("Money", categoryMoney));
        categorySchool.setOnClickListener(v -> selectCategory("School", categorySchool));

        // Date picker
        llDateRow.setOnClickListener(v -> showDatePicker());

        // Time picker
        llTimeRow.setOnClickListener(v -> showTimePicker());

        // Save transaction button
        btnContinue.setOnClickListener(v -> saveTransaction());

        // Amount input (from previous activity or input dialog)
        Intent intent = getIntent();
        if (intent.hasExtra("amount")) {
            double amount = intent.getDoubleExtra("amount", 0.0);
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", amount));
        }

        if (intent.hasExtra("type")) {
            transactionType = intent.getStringExtra("type");
        }
    }

    private void selectTab(String type) {
        transactionType = type;
        if ("expense".equals(type)) {
            tvExpenseTab.setBackgroundResource(R.drawable.bg_tab_selected);
            tvExpenseTab.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvIncomeTab.setBackgroundResource(android.R.color.transparent);
            tvIncomeTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tvIncomeTab.setBackgroundResource(R.drawable.bg_tab_selected);
            tvIncomeTab.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvExpenseTab.setBackgroundResource(android.R.color.transparent);
            tvExpenseTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        resetCategorySelection();
    }

    private void selectCategory(String category, LinearLayout selectedView) {
        // Reset all categories
        resetCategorySelection();

        // Highlight selected category
        ImageView icon = (ImageView) selectedView.getChildAt(0);
        TextView label = (TextView) selectedView.getChildAt(1);

        icon.setBackgroundResource(R.drawable.bg_category_selected);
        icon.setColorFilter(ContextCompat.getColor(this, R.color.light_green_accent));
        label.setTextColor(ContextCompat.getColor(this, R.color.white));

        selectedCategory = category;
    }

    private void resetCategorySelection() {
        LinearLayout[] categories = {categoryFood, categoryTransport, categoryShopping,
                categoryEntertainment, categoryBills, categoryHealth, categoryMoney, categorySchool};

        for (LinearLayout category : categories) {
            ImageView icon = (ImageView) category.getChildAt(0);
            TextView label = (TextView) category.getChildAt(1);

            icon.setBackgroundResource(R.drawable.bg_category_unselected);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.light_gray_text));
            label.setTextColor(ContextCompat.getColor(this, R.color.light_gray_text));
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        tvDate.setText(displayFormat.format(calendar.getTime()));
    }

    private void updateTimeDisplay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        selectedTime = timeFormat.format(calendar.getTime());
        tvTime.setText(displayFormat.format(calendar.getTime()));
    }

    private void saveTransaction() {
        // Validate inputs
        String amountText = etAmount.getText().toString().replace("₱", "").replace(",", "").trim();

        if (amountText.isEmpty() || amountText.equals("0.00")) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText);
        String note = etNote.getText().toString().trim();

        // Create transaction object
        Transaction transaction = new Transaction(userId, transactionType, selectedCategory,
                amount, note, selectedDate, selectedTime);

        // Show loading
        btnContinue.setEnabled(false);
        btnContinue.setText("Saving...");

        // Make API call
        Call<ApiResponse<Transaction>> call = apiService.addTransaction(transaction);
        call.enqueue(new Callback<ApiResponse<Transaction>>() {
            @Override
            public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Save Transaction");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Transaction> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(TransactionActivity.this,
                                "Transaction saved successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Add the new transaction to the repository so the home screen updates
                        Transaction newTransaction = apiResponse.getData();
                        if (newTransaction != null) {
                            TransactionRepository.getInstance().addTransaction(newTransaction);
                        }

                        finish();
                    } else {
                        Toast.makeText(TransactionActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TransactionActivity.this,
                            "Failed to save transaction",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Save Transaction");

                Toast.makeText(TransactionActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
