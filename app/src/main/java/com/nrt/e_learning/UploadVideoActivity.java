package com.nrt.e_learning;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;

    private Uri videoUri;
    private VideoView videoView;
    private AppCompatButton btnChooseVideo, btnUploadVideo;

    private EditText editTextVideoTitle;
    private ImageView playPauseIcon;

    private AlertDialog AlertDialog;
    private StorageReference storageReference;

    private boolean isUploadCancelled = false;
    private UploadTask uploadTask;
    private AdView mAdView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        videoView = findViewById(R.id.videoView);
        btnChooseVideo = findViewById(R.id.btnChooseVideo);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);

        editTextVideoTitle = findViewById(R.id.editTextVideoTitle);
        playPauseIcon = findViewById(R.id.playPauseIcon);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adViews);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



//        fetchCurrentUserAndSendNotification();

        storageReference = FirebaseStorage.getInstance().getReference().child("videos");

        btnChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
            }
        });

        btnUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadVideo();
            }
        });


        playPauseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });


        playPauseIcon = findViewById(R.id.playPauseIcon); // Assuming you have an ImageView in your layout
        updatePlayPauseIcon();
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void uploadVideo() {
        String videoTitle = editTextVideoTitle.getText().toString().trim();
        if (!videoTitle.isEmpty() && videoUri != null) {
            // Storage Reference for the video
            StorageReference videoRef = storageReference.child(videoTitle + "_" + System.currentTimeMillis() + ".mp4");
            uploadTask = videoRef.putFile(videoUri);

            // Show the progress bar and start the progressDialog
            showProgressBar();

            uploadTask.addOnProgressListener(taskSnapshot -> {
                // Update the progress bar based on the upload progress
                int progress = (int) ((120.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                updateProgress(progress - 20);
            });

            uploadTask.addOnFailureListener(e -> {
                if (isUploadCancelled) {
                    // Handle cancellation logic
                    hideProgressBar();
                    Toast.makeText(UploadVideoActivity.this, "Upload canceled", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle other failure cases
                    hideProgressBar();
                    Toast.makeText(UploadVideoActivity.this, "Failed to upload video", Toast.LENGTH_SHORT).show();
                }

            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Video uploaded successfully, get the download URL
                videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Now, you can store the download URL and title in Firestore
                    saveVideoToFirestore(videoTitle, uri.toString());
//                    fetchCurrentUserAndSendNotification(videoTitle);
                    Log.i("url uploaded one ",uri.toString());
                    fetchAllUsersAndSendNotification(videoTitle,uri.toString());
                    // Hide the progress bar and dismiss the progressDialog
                    hideProgressBar();
                }).addOnFailureListener(e -> {
                    // Handle failure to get download URL
                    hideProgressBar();
                    Toast.makeText(this, "Failed to get video download URL", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            Toast.makeText(this, "Enter a video title and select a video first", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToUser(UserModel user,String VideoTitle, String VideoURL ) {

            Log.i("UploadVideoActivity1", " ready");
            try {
                JSONObject notificationObj = new JSONObject();
                notificationObj.put("name", "e - learn");
                notificationObj.put("title", VideoTitle);
                notificationObj.put("image", "https://fastly.picsum.photos/id/4/5000/3333.jpg?hmac=ghf06FdmgiD0-G4c9DdNM8RnBIN7BO0-ZGEw47khHP4");

                JSONObject dataObject = new JSONObject();
                dataObject.put("userId", user.getUserId());
                dataObject.put("videoUrl",VideoURL);
                Log.i("vidoe", VideoURL);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("notification", notificationObj);
                jsonObject.put("data", dataObject);
                jsonObject.put("to", user.getFcmToken());
                Log.e("UploadVideoActivity", "json object is ready");
                callApi(jsonObject);

            } catch (Exception e) {
                Log.i("UploadVideoActivity", e.getMessage());
            }



    }


    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        Log.i("UploadVideoActivity3", " ready3");
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAID-47IE:APA91bEM0Rl_r-fP0RBer3dfMfXIgwzsmvTsBhKnhOZO6avbDJG0Ej7gBzt81DbsmLRZZP83N7Nk6ObL1qK45q4jVs5TULjgvVQnyO12EyEHi-9sBwUXERrueS2PzIxJatmbNW5bWoWc")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("UploadVideoActivity", " Failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("UploadVideoActivity4", " recieved");
            }
        });
    }


    private void cancelUpload() {
        isUploadCancelled = true;
        hideProgressBar();
        Toast.makeText(this, "Upload canceled", Toast.LENGTH_SHORT).show();
    }


    private void saveVideoToFirestore(String videoTitle, String downloadUrl) {
        // Here, we create a document with a unique ID for each video
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference videosCollection = db.collection("videos");

        // Create a new video document
        Map<String, Object> videoData = new HashMap<>();
        videoData.put("title", videoTitle);
        videoData.put("url", downloadUrl);
        videoData.put("thumbnail", null);
        // Add the video document to the "videos" collection
        videosCollection.add(videoData)
                .addOnSuccessListener(documentReference -> {
//                    fetchAllUsersAndSendNotification(videoTitle,downloadUrl);
                    Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                    // Clear the form or navigate to another screen if needed
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save video data to Firestore", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.setVisibility(View.VISIBLE);
            btnUploadVideo.setVisibility(View.VISIBLE);
            playPauseIcon.setVisibility(View.VISIBLE);
            videoView.pause();
        }
    }


    private void togglePlayPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
        }
        updatePlayPauseIcon();
    }

    private void updatePlayPauseIcon() {
        if (videoView.isPlaying()) {
            playPauseIcon.setImageResource(R.drawable.ic_pause); // Set pause icon
        } else {
            playPauseIcon.setImageResource(R.drawable.ic_play); // Set play icon
        }
    }

    private void showProgressBar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.progress_dialog_layout, null));
        builder.setCancelable(false); // Prevent dialog dismissal on outside touch or back press

        // Set up the cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle cancellation logic
            isUploadCancelled = true;
            if (uploadTask != null) {
                uploadTask.cancel();
            }
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog = builder.create();
        AlertDialog.show();
    }


    // Use this method to update the progress and show the percentage
    private void updateProgress(int progress) {
        if (AlertDialog != null && AlertDialog.isShowing()) {
            ProgressBar progressBar = AlertDialog.findViewById(R.id.progressBarId);
            TextView textViewProgress = AlertDialog.findViewById(R.id.textViewProgress);

            if (progressBar != null && textViewProgress != null) {
                progressBar.setProgress(progress);
                if (progress < 0)
                    textViewProgress.setText("Uploading... " + 0 + "%");
                else
                    textViewProgress.setText("Uploading... " + progress + "%");
            }
        }
    }

    private void hideProgressBar() {
        if (AlertDialog != null && AlertDialog.isShowing()) {
            AlertDialog.dismiss();
        }
    }


    private void fetchCurrentUserAndSendNotification(String VideoTitle,String VideoURL) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Fetch additional user data from Firestore using UID
            fetchUserData(currentUser.getEmail().toString(),VideoTitle,VideoURL);
        } else {
            AndroidUtil.showToast(getApplicationContext(), "No current user. Please log in.");
        }
    }

    private void fetchUserData(String userEmail, String VideoTitle,  String VideoURL) {
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
                        sendNotificationToUser(user, VideoTitle, VideoURL );

                    } else {
                        AndroidUtil.showToast(getApplicationContext(), "User data is null");
                    }
                } else {
                    AndroidUtil.showToast(getApplicationContext(), "No user found with the provided email");
                }
            } else {
                AndroidUtil.showToast(getApplicationContext(), "Failed to fetch user data: " + task.getException());
            }
        });

    }




    private void fetchAllUsersAndSendNotification(String videoTitle, String videoUrl) {
        // Get a reference to the "users" collection in Firestore
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Execute the query to get all users
        usersCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();

                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Loop through all documents and send notifications
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            sendNotificationToUser(user, videoTitle,videoUrl);
                        }
                    }
                } else {
                    AndroidUtil.showToast(getApplicationContext(), "No users found");
                }
            } else {
                AndroidUtil.showToast(getApplicationContext(), "Failed to fetch user data: " + task.getException());
            }
        });

    }


}
