package com.nrt.e_learning.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nrt.e_learning.R;

import java.util.HashMap;
import java.util.Map;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;

    private Uri videoUri;
    private VideoView videoView;
    private AppCompatButton btnChooseVideo, btnUploadVideo;

    private EditText editTextVideoTitle;
    private ImageView playPauseIcon;

    private AlertDialog AlertDialog ;
    private StorageReference storageReference;

    private boolean isUploadCancelled = false;
    private UploadTask  uploadTask ;

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
                updateProgress(progress-20);
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
        builder.setView(getLayoutInflater().inflate(R.layout.progress_dialog_layout,null));
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
                if(progress<0)
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

}
