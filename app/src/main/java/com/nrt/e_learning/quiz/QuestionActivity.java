package com.nrt.e_learning.quiz;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.nrt.e_learning.R;
import com.nrt.e_learning.databinding.ActivityQuestionBinding;
import com.nrt.e_learning.databinding.ScoreDialogBinding;
import com.nrt.e_learning.util.AndroidUtil;


import java.util.List;


public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private static List<QuestionModel> questionModelList;
    private static String time;

    private ActivityQuestionBinding binding;
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btn0.setOnClickListener(this);
        binding.btn1.setOnClickListener(this);
        binding.btn2.setOnClickListener(this);
        binding.btn3.setOnClickListener(this);
        binding.nextBtn.setOnClickListener(this);

        loadQuestions();
        startTimer();
    }

    private void startTimer() {
        long totalTimeInMillis = Integer.parseInt(time) * 60 * 1000L;
        new CountDownTimer(totalTimeInMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                binding.timerIndicatorTextview.setText(String.format("%02d:%02d", minutes, remainingSeconds));
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }

        }.start();
    }

    private void loadQuestions() {

        Log.i("data", String.valueOf(questionModelList.size()));
        Log.i("data", String.valueOf(questionModelList.get(0).getCorrect()));
        selectedAnswer = "";
        if(questionModelList==null) {
            AndroidUtil.showToast(getApplicationContext(), "null questions available");
            finishQuiz();
            return;
        }
        if (currentQuestionIndex == questionModelList.size()) {
            finishQuiz();
            return;
        }

        QuestionModel currentQuestion = questionModelList.get(currentQuestionIndex);
        Log.i("data", currentQuestion.getQuestion());
        binding.questionIndicatorTextview.setText("Question " + (currentQuestionIndex + 1) + "/ " + questionModelList.size());
        int progress = (int) ((currentQuestionIndex * 1.0 / questionModelList.size()) * 100);
        binding.questionProgressIndicator.setProgress(progress);
        binding.questionTextview.setText(currentQuestion.getQuestion());
        List<String> options = currentQuestion.getOptions();
       Log.i("data", options.toString());
        if(options!=null) {
            binding.btn0.setText(options.get(0));
            binding.btn1.setText(options.get(1));
            binding.btn2.setText(options.get(2));
            binding.btn3.setText(options.get(3));
        }else{
            AndroidUtil.showToast(getApplicationContext(),"not options available");
        }
    }

    @Override
    public void onClick(View view) {
        binding.btn0.setBackgroundColor(getColor(R.color.light_gray));
        binding.btn1.setBackgroundColor(getColor(R.color.light_gray));
        binding.btn2.setBackgroundColor(getColor(R.color.light_gray));
        binding.btn3.setBackgroundColor(getColor(R.color.light_gray));

        if (view.getId() == R.id.next_btn) {
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please select answer to continue", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedAnswer.equals(questionModelList.get(currentQuestionIndex).getCorrect())) {
                score++;
            }
            currentQuestionIndex++;
            loadQuestions();
        } else {
            selectedAnswer = ((Button) view).getText().toString();
            view.setBackgroundColor(getColor(R.color.orange));
        }
    }

    private void finishQuiz() {
        int totalQuestions = questionModelList.size();
        int percentage = (int) (((float) score / totalQuestions) * 100);

        ScoreDialogBinding dialogBinding = ScoreDialogBinding.inflate(getLayoutInflater());
        dialogBinding.scoreProgressIndicator.setProgress(percentage);
        dialogBinding.scoreProgressText.setText(percentage + " %");

        if (percentage > 60) {
            dialogBinding.scoreTitle.setText("Congrats! You have passed");
            dialogBinding.scoreTitle.setTextColor(Color.BLUE);
        } else {
            dialogBinding.scoreTitle.setText("Oops! You have failed");
            dialogBinding.scoreTitle.setTextColor(Color.RED);
        }

        dialogBinding.scoreSubtitle.setText(score + " out of " + totalQuestions + " are correct");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        builder.setCancelable(false);
        builder.show();
    }

    public static void setQuestionModelList(List<QuestionModel> questionModelList) {
        QuestionActivity.questionModelList = questionModelList;
    }

    public static void setTime(String time) {
        QuestionActivity.time = time;
    }

    public void gotoQuizHome(View view){
        finish();
    }
}
