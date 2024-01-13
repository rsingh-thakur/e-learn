package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.auth.User;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.SharedPreferencesManager;

public class HomeActivity extends AppCompatActivity {

    private static final long SESSION_EXPIRATION_TIME = 600000000000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserLoggedIn() || isSessionExpired()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_home);
        // Initialize your main activity components...
    }

    private boolean isUserLoggedIn() {
        // Retrieve user login state from SharedPreferences
        return SharedPreferencesManager.isLoggedInUser(this);
    }

    private boolean isSessionExpired() {
        // Check if the session has expired (e.g., 24 hours limit)
        long loginTime = SharedPreferencesManager.getLoginTime(this);
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - loginTime;
        return sessionDuration > SESSION_EXPIRATION_TIME;
    }


    public void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        AndroidUtil.showToast(getApplicationContext(),"Session Expired ..!");
        startActivity(intent);
        // finish(); // Optional, to close MainActivity
    }
    // Other methods...

    public void logOutUser(View view) {
        SharedPreferencesManager.setLoggedInUser(getApplicationContext(), false);
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        AndroidUtil.showToast(getApplicationContext(), "User logged out Successfully ");

    }


}
