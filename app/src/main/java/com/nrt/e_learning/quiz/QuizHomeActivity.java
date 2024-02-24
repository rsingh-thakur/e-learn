package com.nrt.e_learning.quiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nrt.e_learning.databinding.QuizHomeBinding;
import com.nrt.e_learning.util.AndroidUtil;


import java.util.ArrayList;
import java.util.List;


public class QuizHomeActivity extends AppCompatActivity {
    private QuizHomeBinding binding;
    private List<QuizModel> quizModelList;
    private QuizHomeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = QuizHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quizModelList = new ArrayList<>();
        getDataFromFirebase();
    }

    private void setupRecyclerView() {
        binding.progressBar.setVisibility(View.GONE);
        adapter = new QuizHomeListAdapter(quizModelList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }


    private void getDataFromFirebase() {
        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuizModel quizModel = snapshot.getValue(QuizModel.class);
                    if (quizModel != null) {
                        quizModelList.add(quizModel);
                    }
                }
            }
            setupRecyclerView();
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }




//    private void getDataFromFirebase() {
//
//        // Create QuizModel objects and add them to the list
//        QuizModel quiz1 = new QuizModel("1", "Quiz 1", "Subtitle 1", "10", createQuestionList(3));
//        QuizModel quiz2 = new QuizModel("2", "Quiz 2", "Subtitle 2", "15", createQuestionList(2));
//        QuizModel quiz3 = new QuizModel("3", "Quiz 3", "Subtitle 3", "20", createQuestionList(4));
//        QuizModel quiz4 = new QuizModel("4", "Quiz 4", "Subtitle 4", "25", createQuestionList(5));
//
//        // Add QuizModel objects to the list
//        quizModelList.add(quiz1);
//        quizModelList.add(quiz2);
//        quizModelList.add(quiz3);
//        quizModelList.add(quiz4);
//        setupRecyclerView();
//    }


//    private static List<QuestionModel> createQuestionList(int numQuestions) {
//        List<QuestionModel> questionList = new ArrayList<>();
//        for (int i = 1; i <= numQuestions; i++) {
//            questionList.add(new QuestionModel("Question " + i, createOptions(), "Option " + (i % 4 + 1)));
//        }
//        return questionList;
//    }
//
//    private static List<String> createOptions() {
//        List<String> options = new ArrayList<>();
//        options.add("Option 1");
//        options.add("Option 2");
//        options.add("Option 3");
//        options.add("Option 4");
//        return options;
//    }

}
