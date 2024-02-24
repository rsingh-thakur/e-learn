package com.nrt.e_learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;
import com.nrt.e_learning.util.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class LoginActivity extends AppCompatActivity {
    private EditText loginUserId, loginPasswordId;
    private TextView googleSignIn;
    private Button loginButton;
    private ProgressBar loginProgressBar;
    private boolean isUserExist;
    private AdView mAdView;
    private FirebaseFirestore firestore;


    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 10;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firestore = FirebaseFirestore.getInstance();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        firebaseAuth = FirebaseAuth.getInstance();
        googleSignIn= findViewById(R.id.googleSignIn);

        loginUserId = findViewById(R.id.loginUserId);
        loginPasswordId = findViewById(R.id.loginPasswordId);
        loginButton = findViewById(R.id.loginButton);
        loginProgressBar = findViewById(R.id.loginProgressBarId);


        firebaseAuth = FirebaseAuth.getInstance();

        // Google Sign-In client configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginUserId.getText().toString().trim();
                String password = loginPasswordId.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    AndroidUtil.showToast(getApplicationContext(), "Fill the all fields");

                    return;
                }

                loginProgressBar.setVisibility(View.VISIBLE);
                Log.d("LoginActivity", "Email: " + email + ", Password: " + password);

                // Perform Firebase authentication
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            loginProgressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                // Authentication successful, navigate to HomeActivity
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    SharedPreferencesManager.setLoggedInUser(getApplicationContext(), true);
                                    SharedPreferencesManager.setLoginTime(getApplicationContext(), System.currentTimeMillis());

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("USERNAME_EXTRA", user.getDisplayName());
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                AndroidUtil.showToast(getApplicationContext(), "Authentication failed. Check email and password.");
                            }
                        });
            }
        });


        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
          });

    }
    public void redirectToSignUp(View view) {
        Intent intent = new Intent(LoginActivity.this, UserSignUpActivity.class);
        startActivity(intent);
    }


    public void forgotPassword(View view) {
        Intent intent = new Intent(LoginActivity.this, SendOTPActivity.class);
        startActivity(intent);
    }


    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }

    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.i("signup", "method called");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            checkUserExist(account);

        } catch (ApiException e) {
            // Google Sign-In failed
            Log.w("LoginActivity", "Google sign-in failed", e);
            AndroidUtil.showToast(getApplicationContext(), "Google Sign-In failed "+e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Your existing code for successful authentication
                                Log.i("login","user email  "+user.getEmail());
                                // Redirect to HomeActivity
                                if(user.getEmail()!=null) {

                                    SharedPreferencesManager.setLoggedInUser(getApplicationContext(), true);
                                    SharedPreferencesManager.setLoginTime(getApplicationContext(), System.currentTimeMillis());

                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                    finish(); // Finish the login activity
                                }else
                                {
                                    Log.i("login","user not found ");
                                }
                            }
                        } else {
                            // If sign in fails
                            AndroidUtil.showToast(getApplicationContext(), "Authentication failed.");
                        }
                    }
                });
    }



    public void checkUserExist( GoogleSignInAccount  account){
        String email = account.getEmail();
        String username = account.getDisplayName();

        firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

        if (!queryDocumentSnapshots.isEmpty()) {
               // User with this email already exists
                firebaseAuthWithGoogle(account.getIdToken());
            }
            else {
                  createUserInFirestore(email,username);
                  firebaseAuthWithGoogle(account.getIdToken());
               }
         });

    }


   public void createUserInFirestore(String email ,String username){
       String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
               .format(Calendar.getInstance().getTime());

       // Create a User object
       UserModel user = new UserModel(username, email, "", "", "", creationDate, FirebaseUtil.currentUserId());

       // Add the user object to Firestore
       firestore.collection("users")
               .add(user)
               .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                   @Override
                   public void onSuccess(DocumentReference documentReference) {
                       // User signed up successfully
                       AndroidUtil.showToast(getApplicationContext(), "User signed up successfully!");
                       Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                       startActivity(intent);
                       finish();
                   }

               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       // Error occurred while saving data to Firestore
                       AndroidUtil.showToast(getApplicationContext(), "Failed to sign up. Please try again.");
                   }
               });

   }
}