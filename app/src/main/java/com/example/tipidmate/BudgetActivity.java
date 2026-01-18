package com.example.tipidmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetActivity extends AppCompatActivity {

    private EditText etBudgetAmount;
    private RadioGroup rgBudgetFrequency;
    private RadioButton rbMonthly, rbDaily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_details_screen);

        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        rgBudgetFrequency = findViewById(R.id.rgBudgetFrequency);
        rbMonthly = findViewById(R.id.rbMonthly);
        rbDaily = findViewById(R.id.rbDaily);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        ImageView ivBack = findViewById(R.id.ivBack);

        loadBudget();

        btnSaveBudget.setOnClickListener(v -> {
            saveBudget();
            finish();
        });

        ivBack.setOnClickListener(v -> finish());
    }

    private void saveBudget() {
        SharedPreferences prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String amountStr = etBudgetAmount.getText().toString();
        float amount = 0;
        if (!amountStr.isEmpty()) {
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount entered.", Toast.LENGTH_SHORT).show();
                return; // Stop the save if the number is not valid
            }
        }
        editor.putFloat("budgetAmount", amount);

        int selectedFrequencyId = rgBudgetFrequency.getCheckedRadioButtonId();
        if (selectedFrequencyId == R.id.rbMonthly) {
            editor.putString("budgetFrequency", "Monthly");
        } else if (selectedFrequencyId == R.id.rbDaily) {
            editor.putString("budgetFrequency", "Daily");
        }

        editor.apply();
        Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadBudget() {
        SharedPreferences prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
        // Only display a value if a budget has been previously set.
        if (prefs.contains("budgetAmount")) {
            float budgetAmount = prefs.getFloat("budgetAmount", 0f);
            etBudgetAmount.setText(String.valueOf(budgetAmount));
        }

        String budgetFrequency = prefs.getString("budgetFrequency", "Monthly");
        if ("Monthly".equals(budgetFrequency)) {
            rbMonthly.setChecked(true);
        } else {
            rbDaily.setChecked(true);
        }
    }
}
