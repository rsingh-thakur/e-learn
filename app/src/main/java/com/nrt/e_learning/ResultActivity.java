package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

        TextView txtTotalQuestions, txtCorrectAnswers;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_result);

            txtTotalQuestions = findViewById(R.id.txtTotalQuestions);
            txtCorrectAnswers = findViewById(R.id.txtCorrectAnswers);

            int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
            int correctAnswers = getIntent().getIntExtra("correctAnswers", 0);

            txtTotalQuestions.setText("Total Questions: " + totalQuestions);
            txtCorrectAnswers.setText("Correct Answers: " + correctAnswers);
        }
    }
