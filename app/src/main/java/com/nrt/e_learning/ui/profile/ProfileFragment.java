package com.nrt.e_learning.ui.profile;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nrt.e_learning.LoginActivity;
import com.nrt.e_learning.R;

import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.SharedPreferencesManager;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {



    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText usernameEditText,emailEditText,phoneNumberEditText;

    private TextView usernameTextView;
    private TextView emailTextView;

    private ProgressBar progressBar;
    private TextView phoneNumberTextView ,overlayTextImage;

    private RewardedAd rewardedAd;
    private AppCompatButton  logOutButton, editProfileButton , saveProfileInfoButton;
    private ImageView profilePhotoImageView;

    private AppCompatButton saveProfileButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final String TAG = "ProfileFragment";

    private StorageReference storageReference;
    private Uri selectedImageUri;
    private boolean isPictureSelected;
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Initialize TextViews

        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);


        logOutButton = view.findViewById(R.id.logOutButton);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        saveProfileInfoButton = view.findViewById(R.id.saveInfoButton);

        profilePhotoImageView = view.findViewById(R.id.profilePhotoImageView);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        overlayTextImage = view.findViewById(R.id.overlayTextImage);
        progressBar = view.findViewById(R.id.progressBarProfile);




        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);



        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the Save button, hide Logout button
                saveProfileInfoButton.setVisibility(View.VISIBLE);
                logOutButton.setVisibility(View.GONE);
                editProfileButton.setVisibility(View.GONE);
                // Show the EditTexts and set their text with current data
                setEditTextValues();
                toggleEditMode(true);
            }
        });




        saveProfileInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from EditText and update the corresponding TextViews or perform save logic
                String newUsername = usernameEditText.getText().toString();
                String newEmail = emailEditText.getText().toString();
                String newPhoneNumber = phoneNumberEditText.getText().toString();

                if (isValidPhoneNumber(newPhoneNumber)) {
                    updateTextViews(newUsername, newEmail, newPhoneNumber);
                } else {
                    // Display an error message or handle invalid phone number as needed
                    Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                }

                // Update TextViews or perform save logic as needed


                // Save the updated data
                updateUserDetails(newUsername, newEmail, newPhoneNumber);

                // Hide the EditTexts and show TextViews or perform other UI changes
                toggleEditMode(false);

                // Show Logout button, hide Save button
                logOutButton.setVisibility(View.VISIBLE);

            }
        });


        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesManager.logOut(getContext().getApplicationContext());
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();

            }
        });

        profilePhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (rewardedAd != null) {
                    Activity activityContext =getActivity();
                    rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            Log.d(TAG, "The user earned the reward.");
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();
                        }
                    });
                } else {
                    Log.d(TAG, "The rewarded ad wasn't ready yet.");
                }

            }
        },3000);

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPictureSelected) {
                    progressBar.setVisibility(View.VISIBLE);
                    // Upload the selected image to Firebase Storage
                    uploadImageToStorage();
                } else {
                    // Handle case where no image is selected
                    Toast.makeText(getContext(), "Please select a profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Retrieve the current user from Firebase Authentication and display the details
        fetchAndDisplayCurrentUser();
//        loadRewardedAds();
        return view;
    }



    private void setEditTextValues() {
        // Set username and email values as usual
        usernameEditText.setText(usernameTextView.getText());
        emailEditText.setText(emailTextView.getText());

        // Validate and set phone number value
        String phoneNumber = phoneNumberTextView.getText().toString().trim();
        if (isValidPhoneNumber(phoneNumber)) {
            phoneNumberEditText.setText(phoneNumber);
        } else {
            // Display an error message or handle invalid phone number as needed
            Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Check if phone number length is within the range of 10-12 characters
        return phoneNumber.length() >= 10 && phoneNumber.length() <= 12;
    }
    // Update TextViews with new information
    private void updateTextViews(String newUsername, String newEmail, String newPhoneNumber) {
        usernameTextView.setText(newUsername);
        emailTextView.setText(newEmail);
        phoneNumberTextView.setText(newPhoneNumber);
    }

    // Toggle between edit mode (showing EditTexts) and view mode (showing TextViews)
    private void toggleEditMode(boolean editMode) {
        int editTextVisibility = editMode ? View.VISIBLE : View.GONE;
        int textViewVisibility = editMode ? View.GONE : View.VISIBLE;

        usernameEditText.setVisibility(editTextVisibility);
        emailEditText.setVisibility(editTextVisibility);
        phoneNumberEditText.setVisibility(editTextVisibility);

        usernameTextView.setVisibility(textViewVisibility);
        emailTextView.setVisibility(textViewVisibility);
        phoneNumberTextView.setVisibility(textViewVisibility);
    }


    private void updateUserDetails(String newUsername, String newEmail, String newPhoneNumber) {
        // Reference to the "users" collection
        saveProfileInfoButton.setVisibility(View.GONE);
        editProfileButton.setVisibility(View.VISIBLE);
        CollectionReference usersCollection = db.collection("users");

        // Create a query to find the user document with the specified UID
      String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail()  ;
        Query userQuery = usersCollection.whereEqualTo("email", currentUserEmail);

        // Execute the query
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Get the document ID of the user document
                        String documentId = document.getId();

                        // Create a map with the updated fields
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("username", newUsername);
                        updates.put("phoneNumber", newPhoneNumber);

                        // Update the user document
                        usersCollection.document(documentId).update(updates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> updateTask) {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(getContext().getApplicationContext(), "User details updated successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext().getApplicationContext(), "Error updating user details", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getContext().getApplicationContext(), "Error querying user details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImagePicker() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Log.i("imageUrl", String.valueOf(selectedImageUri));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);

                // Set the selected image to the profilePhotoImageView
                profilePhotoImageView.setImageBitmap(bitmap);
                isPictureSelected=true;
                saveProfileButton.setVisibility(View.VISIBLE);
                // You may want to save the selected image URI to SharedPreferences or elsewhere
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToStorage() {
        storageReference = FirebaseStorage.getInstance().getReference();

        // Create a reference to the image file
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final String imageName = userEmail + ".jpg";
        final StorageReference profileImageRef = storageReference.child("profile_images/" + imageName);
        Log.i("imageUrl", String.valueOf(selectedImageUri));

        // Check if the image already exists
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri existingDownloadUrl) {
                // If the image exists, delete it first
                deleteImageAndUploadNew(profileImageRef, imageName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // If onFailure is called, it means the image does not exist, so proceed with the upload
                uploadNewImage(profileImageRef);
            }
        });
    }

    private void deleteImageAndUploadNew(final StorageReference profileImageRef, final String imageName) {
        profileImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully, now upload the new image
                uploadNewImage(profileImageRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBar.setVisibility(View.GONE);
                // Handle the failure to delete the existing image
                Log.e("deleteImage", "Failed to delete existing image: " + exception.getMessage());
            }
        });
    }

    private void uploadNewImage(final StorageReference profileImageRef) {
        // Upload the new image to Firebase Storage
        profileImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                // Save the download URL to the current user's profileUrl field in Firestore
                                progressBar.setVisibility(View.GONE);
                                Log.i("downloadurl", downloadUrl.toString());
                                saveProfileUrlToFirestore(downloadUrl.toString());
                            }
                        });
                    }
                });
    }




    private void saveProfileUrlToFirestore(String profileUrl) {
        // Get the current user's email
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Reference to the users collection in Firestore
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Query to find the user document by email
        Query query = usersCollection.whereEqualTo("email", userEmail);

        // Execute the query
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                // Check if there is any user with the given email (there should be only one)
                if (!querySnapshot.isEmpty()) {
                    // Get the first document (should be the only one)
                    DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                    // Update the profileUrl field with the provided URL
                    userDoc.getReference().update("profileUrl", profileUrl)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Profile URL successfully updated in Firestore
                                    fetchUserData(userEmail);
                                    saveProfileButton.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Profile pic. saved successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    // Handle the failure to update the profileUrl field
                                    Log.e("updateProfileUrl", "Failed to update profileUrl: " + e.getMessage());
                                }
                            });
                } else {
                    // Handle the case where no user is found with the given email
                    Log.e("updateProfileUrl", "No user found with email: " + userEmail);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to execute the query
                Log.e("updateProfileUrl", "Query failed: " + e.getMessage());
            }
        });
    }






    private void fetchAndDisplayCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
//            loadRewardedAds();
            // Fetch additional user data from Firestore using UID
            fetchUserData(currentUser.getEmail().toString());
        } else {
            AndroidUtil.showToast(getContext(), "No current user. Please log in.");
        }
    }
    private void fetchUserData(String userEmail) {
        // Get a reference to the "users" collection in Firestore
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Query to search for the document with the given email
        Query query = usersCollection.whereEqualTo("email", userEmail);

        // Execute the query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();

                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Retrieve the first document matching the query
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
//                    AndroidUtil.showToast(getContext(), document.getId());

                    // Document exists, retrieve and display user data
                    UserModel user = document.toObject(UserModel.class);

                    if (user != null) {
                        usernameTextView.setText(user.getUsername().trim());
                        emailTextView.setText(user.getEmail().trim());
                        phoneNumberTextView.setText(user.getPhoneNumber().trim());

                        if (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(user.getProfileUrl())
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.circular_bg)
                                    .into(profilePhotoImageView);
                        }
                    } else {
                        AndroidUtil.showToast(getContext(), "User data is null");
                    }
                } else {
                    AndroidUtil.showToast(getContext(), "No user found with the provided email");
                }
            } else {
                AndroidUtil.showToast(getContext(), "Failed to fetch user data: " + task.getException());
            }
        });
    }




    public void loadRewardedAds(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(getContext().getApplicationContext(), "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Ad was loaded.");
                        ServerSideVerificationOptions options = new ServerSideVerificationOptions
                                .Builder()
                                .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                                .build();
                        rewardedAd.setServerSideVerificationOptions(options);

                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }
                });
    }



}
