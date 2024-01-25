package com.nrt.e_learning.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.R;
import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView phoneNumberTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize TextViews
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);

        // Retrieve the current user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Fetch additional user data from Firestore
            fetchUserData(currentUser.getUid());
        }

        return view;
    }

    private void fetchUserData(String userId) {
        // Get a reference to the "users" collection in Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        // Read user data from Firestore
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    AndroidUtil.showToast(getContext(), "exists");

                    // Create a UserModel object with the user's information
                    UserModel user = document.toObject(UserModel.class);

                    // Update TextViews with user information
                    if (user != null) {
                        AndroidUtil.showToast(getContext(), "data2" + user.getEmail());
                        usernameTextView.setText(user.getUsername());
                        emailTextView.setText(user.getEmail());
                        phoneNumberTextView.setText(user.getPhoneNumber());
                    }
                } else {
                    AndroidUtil.showToast(getContext(), "Document does not exist");
                }
            } else {
                AndroidUtil.showToast(getContext(), "Failed to fetch user data: " + task.getException());
            }
        });
    }
}
