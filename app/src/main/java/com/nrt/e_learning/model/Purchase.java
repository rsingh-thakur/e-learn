package com.nrt.e_learning.model;

public class Purchase {
    private String userEmail;
    private String courseName;

    public Purchase() {
        // Empty constructor required for Firestore
    }

    public Purchase(String userEmail, String courseName) {
        this.userEmail = userEmail;
        this.courseName = courseName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }


}