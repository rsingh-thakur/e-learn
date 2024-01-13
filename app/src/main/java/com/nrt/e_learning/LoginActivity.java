package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.SharedPreferencesManager;

public class LoginActivity extends AppCompatActivity {
    private EditText loginUserId, loginPasswordId;
    private Button loginButton;
    private ProgressBar loginProgressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        loginUserId = findViewById(R.id.loginUserId);
        loginPasswordId = findViewById(R.id.loginPasswordId);
        loginButton = findViewById(R.id.loginButton);
        loginProgressBar = findViewById(R.id.loginProgressBarId);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginUserId.getText().toString().trim();
                String password = loginPasswordId.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // Handle empty email or password
                    return;
                }

                loginProgressBar.setVisibility(View.VISIBLE);
                Log.d("LoginActivity", "Email: " + email + ", Password: " + password);

                // Perform Firebase authentication
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            loginProgressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                // Authentication successful, navigate to MainActivity
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    SharedPreferencesManager.setLoggedInUser(getApplicationContext(), true);
                                    SharedPreferencesManager.setLoginTime(getApplicationContext(), System.currentTimeMillis());

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("USERNAME_EXTRA", user.getDisplayName());
                                    startActivity(intent);
                                    finish(); // Finish the login activity
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                AndroidUtil.showToast(getApplicationContext(), "Authentication failed. Check email and password.");
                            }
                        });
            }
        });

    }


    public void redirectToSignUp(View view) {
        Intent intent = new Intent(LoginActivity.this, UserSignUpActivity.class);
        startActivity(intent);
    }


    public void forgotPassowrd(View view) {
        Intent intent = new Intent(LoginActivity.this, SendOTPActivity.class);
        startActivity(intent);
    }
}