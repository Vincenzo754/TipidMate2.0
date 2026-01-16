package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalsCreateActivity extends AppCompatActivity {

    private EditText etGoalTitle, etTargetAmount, etTargetDate, etDescription;
    private RelativeLayout spinnerCategory;
    private TextView tvCategory;
    private Button btnSetGoal;
    private Calendar calendar;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_create);

        etGoalTitle = findViewById(R.id.etGoalTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvCategory = findViewById(R.id.tvCategory);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etTargetDate = findViewById(R.id.etTargetDate);
        etDescription = findViewById(R.id.etDescription);
        btnSetGoal = findViewById(R.id.btnSetGoal);
        calendar = Calendar.getInstance();

        spinnerCategory.setOnClickListener(v -> showCategoryDialog());
        etTargetDate.setOnClickListener(v -> showDatePickerDialog());

        btnSetGoal.setOnClickListener(v -> {
            String title = etGoalTitle.getText().toString().trim();
            String targetAmountStr = etTargetAmount.getText().toString().trim();
            String targetDateStr = etTargetDate.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(targetAmountStr) || TextUtils.isEmpty(targetDateStr) || TextUtils.isEmpty(selectedCategory)) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double targetAmount = Double.parseDouble(targetAmountStr);
            long targetDate = calendar.getTimeInMillis();
            int iconResId = getIconForCategory(selectedCategory);

            Goal newGoal = new Goal(title, selectedCategory, targetAmount, targetDate, description, iconResId);
            GoalRepository.getInstance().addGoal(newGoal);

            setResult(RESULT_OK);
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        final String[] categories = getResources().getStringArray(R.array.goal_categories);
        builder.setItems(categories, (dialog, which) -> {
            selectedCategory = categories[which];
            tvCategory.setText(selectedCategory);
        });
        builder.show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        new DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etTargetDate.setText(sdf.format(calendar.getTime()));
    }

    private int getIconForCategory(String category) {
        switch (category) {
            case "Electronic":
                return R.drawable.ic_eletronic;
            case "Vehicle":
                return R.drawable.ic_transport;
            case "Appliances":
                return R.drawable.ic_appliances;
            case "Travel":
                return R.drawable.ic_travel;
            case "Furniture":
                return R.drawable.ic_furniture;
            case "Educations":
                return R.drawable.ic_education;
            default:
                return R.drawable.ic_others;
        }
    }
}
