package com.nrt.e_learning.ui.mycourses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyCoursesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyCoursesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}