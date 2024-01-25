package com.nrt.e_learning;
// Import necessary libraries

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.nrt.e_learning.util.AndroidUtil;


public class EditVideoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView thumbnailImageView;

    private Button saveButton, btnDeleteVideo;

    Uri selectedThumbnailUri;
    EditText title;
    String videoUrl;
    Uri imageUri;

    StorageReference storageReference;
    // Add other necessary variables...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        thumbnailImageView = findViewById(R.id.ThumbnailImageView);
        // Find other views...
        title = findViewById(R.id.editVideoTitle);
        btnDeleteVideo = findViewById(R.id.btnDeleteVideo);
        title.setText(getIntent().getStringExtra("videoTitle").trim());
        thumbnailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });


        saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });

        btnDeleteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoUrl = getIntent().getStringExtra("videoUrl");


               Log.i("url", videoUrl);
                if (videoUrl != null) {
                    deleteVideoFromStorage(videoUrl);
                    deleteVideoDocumentByUrl(videoUrl);
                } else {
                    AndroidUtil.showToast(getApplicationContext(), "video name is null");
                }

            }
        });


    }

    public void onSaveButtonClick(View view) {
        String newTitle = title.getText().toString();
        videoUrl = getIntent().getStringExtra("videoUrl");
        // Save the picture in the database
        savePictureToDatabase(newTitle, selectedThumbnailUri);
        // Update the video title
        updateVideoTitleByUrl(videoUrl, newTitle);

    }


    public void onThumbnailClick(View view) {
        // Handle logic when the image is clicked
        openImagePicker();
    }

    private void openImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedThumbnailUri = data.getData();
            thumbnailImageView.setImageURI(selectedThumbnailUri);
        }


    }


    private void updateThumbnailByUrl(String videoUrl, Uri newThumbnailUri) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // Query the Firestore to find the document with the matching video URL
        firestore.collection("videos")
                .whereEqualTo("url", videoUrl)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Found the document with the matching video URL
                            // Update the 'thumbnail' field with the new thumbnail URI
                            document.getReference().update("thumbnail", newThumbnailUri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        // Thumbnail update success
                                        Toast.makeText(EditVideoActivity.this, "Thumbnail updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Thumbnail update failed
                                        Toast.makeText(EditVideoActivity.this, "Failed to update thumbnail", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // No document found with the matching video URL
                        Toast.makeText(EditVideoActivity.this, "Video not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateVideoTitleByUrl(String videoUrl, String newTitle) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("videos")
                .whereEqualTo("url", videoUrl)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().update("title", newTitle)
                                    .addOnSuccessListener(aVoid -> {
                                        // Title update success
                                        Toast.makeText(EditVideoActivity.this, "Video title updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Title update failed
                                        Toast.makeText(EditVideoActivity.this, "Failed to update video title", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // No document found with the matching video URL
                        Toast.makeText(EditVideoActivity.this, "Video not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void savePictureToDatabase(String title, Uri thumbnailUri) {
        // Create a reference to the image in Firebase Storage
        StorageReference imageRef = storageReference.child("thumbnails/" + title);

        // Upload the image to Firebase Storage
        imageRef.putFile(thumbnailUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload success
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save image details to Firestore
//                        saveImageDetailsToFirestore(title, uri.toString());
                        updateThumbnailByUrl(videoUrl,uri);
                    });
                })
                .addOnFailureListener(e -> {
                    // Image upload failed
                    Toast.makeText(EditVideoActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }


//    private void saveImageDetailsToFirestore(String title, String imageUrl) {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        // Create a new document in the "images" collection with title and URL
//        ImageModel imageModel = new ImageModel(title, imageUrl);
//        updateThumbnailByUrl(videoUrl, Uri.parse(imageUrl));
//        firestore.collection("images")
//                .document(title)
//                .set(imageModel)
//                .addOnSuccessListener(aVoid -> {
//                    // Image details saved successfully
//                    Toast.makeText(EditVideoActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    // Image details save failed
//                    Toast.makeText(EditVideoActivity.this, "Failed to save image details", Toast.LENGTH_SHORT).show();
//                });
//    }

    public void deleteVideoFromStorage(String videoUrl) {
        // Create a StorageReference from the video URL
         StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);

        // Delete the file
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
               AndroidUtil.showToast(getApplicationContext(),"Video deleted successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Uh-oh, an error occurred!
                AndroidUtil.showToast(getApplicationContext(),"Failed to delete video");
            }
        });
    }



    private void deleteVideoDocumentByUrl(String videoUrl) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query Firestore to find the document with the matching video URL
        firestore.collection("videos")
                .whereEqualTo("url", videoUrl)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Found the document with the matching video URL

                            // Get the thumbnail URL from the document
                            String thumbnailUrl = document.getString("thumbnail");
                            // Delete the thumbnail in Firebase Storage
                            deleteThumbnail(thumbnailUrl);

                            // Delete the document in Firestore
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Document deletion success
                                        Toast.makeText(EditVideoActivity.this, "Video document deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Document deletion failed
                                        Toast.makeText(EditVideoActivity.this, "Failed to delete video document", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // No document found with the matching video URL
                        Toast.makeText(EditVideoActivity.this, "Video not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteThumbnail(String thumbnailUrl) {
        // Extract the thumbnail name from the URL
            // Create a StorageReference for the thumbnail file
        if(thumbnailUrl!= null) {
            StorageReference thumbnailRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);

            // Delete the thumbnail file in Firebase Storage
            thumbnailRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditVideoActivity.this, "Thumbnail deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditVideoActivity.this, "Failed to delete thumbnail", Toast.LENGTH_SHORT).show();
                    });
        }else {
            Toast.makeText(EditVideoActivity.this, "No thumbnail found to delete", Toast.LENGTH_SHORT).show();

        }
    }



}