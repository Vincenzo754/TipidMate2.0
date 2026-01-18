package com.example.tipidmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalsSavingsActivity extends AppCompatActivity {

    private EditText etAmount, etNote;
    private TextView tvDate, tvTime;
    private Button btnAddToGoal;
    private Calendar calendar;
    private Goal goal;
    private GoalRepository goalRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_savings);

        goalRepository = GoalRepository.getInstance();
        String goalId = getIntent().getStringExtra("goal_id");
        goal = goalRepository.findGoalById(goalId);

        if (goal == null) {
            Toast.makeText(this, "Goal not found. It might have been deleted.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnAddToGoal = findViewById(R.id.btnAddToGoal);
        calendar = Calendar.getInstance();

        TextView tvGoalTitle = findViewById(R.id.tvGoalTitle);
        ImageView ivGoalIcon = findViewById(R.id.ivGoalIcon);
        tvGoalTitle.setText(goal.getTitle());
        ivGoalIcon.setImageResource(goal.getIconResId());

        tvDate.setOnClickListener(v -> showDatePickerDialog());
        tvTime.setOnClickListener(v -> showTimePickerDialog());

        btnAddToGoal.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long timestamp = calendar.getTimeInMillis();

            Contribution contribution = new Contribution(amount, timestamp, note);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("contribution", contribution);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_goals);
        BottomNavigationHelper.setupBottomNavigationView(bottomNavigationView, this);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        };

        new DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvDate.setText(sdf.format(calendar.getTime()));
    }

    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeLabel();
        };

        new TimePickerDialog(this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false).show();
    }

    private void updateTimeLabel() {
        String myFormat = "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvTime.setText(sdf.format(calendar.getTime()));
    }
}
