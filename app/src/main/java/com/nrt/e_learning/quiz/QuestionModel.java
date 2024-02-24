package com.nrt.e_learning.quiz;



import java.util.List;

public class QuestionModel {
    private String question;
    private List<String> options;
    private String correct;

    public QuestionModel(String question, List<String> options, String correct) {
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    public QuestionModel() {
        this.question = "";
        this.options = null;
        this.correct = "";
    }

    // Getters and Setters
    // Note: You need to implement getters and setters for all fields
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }
}
