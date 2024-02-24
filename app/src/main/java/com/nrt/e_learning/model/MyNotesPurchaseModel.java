package com.nrt.e_learning.model;

public class MyNotesPurchaseModel {

    private String userEmail;
    private String NotesSetName;

    public MyNotesPurchaseModel(String userEmail, String notesSetName) {
        this.userEmail = userEmail;
        NotesSetName = notesSetName;
    }

    public MyNotesPurchaseModel() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getNotesSetName() {
        return NotesSetName;
    }

    public void setNotesSetName(String notesSetName) {
        NotesSetName = notesSetName;
    }
}
