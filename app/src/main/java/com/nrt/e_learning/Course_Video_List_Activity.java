package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.adapters.CourseVideoListAdapter;
import com.nrt.e_learning.adapters.VideoListAdapter;
import com.nrt.e_learning.model.VideoItem;
import com.nrt.e_learning.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class Course_Video_List_Activity extends AppCompatActivity  implements CourseVideoListAdapter.OnItemClickListener  {

    private FirebaseFirestore firestore;
    private List<VideoItem> videoItemList;
    private RecyclerView recyclerViewVideos;
    private CourseVideoListAdapter  courseVideoListAdapter;
    private EditText searchVideoTitleText ;
    private ImageButton video_search_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);
        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this)); // Use LinearLayoutManager or GridLayoutManager as needed

        firestore = FirebaseFirestore.getInstance();
        videoItemList = new ArrayList<>();

        courseVideoListAdapter = new CourseVideoListAdapter(videoItemList, this, this);
        recyclerViewVideos.setAdapter(courseVideoListAdapter);

        String cateName = getIntent().getStringExtra("categoryName");
        fetchVideosFromFirestore(cateName);

        searchVideoTitleText = findViewById(R.id.search_bar);
        video_search_btn = findViewById(R.id.video_search_btn);
        video_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedText = searchVideoTitleText.getText().toString().trim();
                fetchVideosFromFirestoreBySearchText(getApplicationContext(),searchedText,firestore,videoItemList,courseVideoListAdapter,cateName);
                if(videoItemList.size()==0)
                    AndroidUtil.showToast(getApplicationContext(),"No Result Found");
            }

        });
    }



    private void fetchVideosFromFirestore(String categoryName) {
        firestore.collection("videos")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the existing videoItemList
                        videoItemList.clear();

                        // Iterate through the documents and retrieve video data
                        for (DocumentSnapshot document : task.getResult()) {
                            String videoTitle = document.getString("title");
                            String videoUrl = document.getString("url");
                            String thumbnailUrl = document.getString("thumbnail");
                            String   Name = document.getString("categoryName");

                            // Create Uri objects for video and thumbnail
                            Uri videoUri = Uri.parse(videoUrl);
                            Uri thumbnailUri = null;

                            if (thumbnailUrl != null)
                                thumbnailUri = Uri.parse(thumbnailUrl);

                            // Create a VideoItem object with both video and thumbnail URIs
                            VideoItem videoItem = new VideoItem(videoUri, videoTitle, thumbnailUri);
                            videoItem.setThumbnailUri(thumbnailUri);

                            // Add the VideoItem to the list
                            videoItemList.add(videoItem);
                        }

                        // Now, you can update your RecyclerView adapter with the new data
                        courseVideoListAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(Course_Video_List_Activity.this, "Failed to fetch videos from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void fetchVideosFromFirestoreBySearchText(Context context, String searchText, FirebaseFirestore firestore, List<VideoItem> videoItemList, CourseVideoListAdapter videoListAdapter,String categoryName) {
        firestore.collection("videos")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the existing videoItemList
                        videoItemList.clear();

                        // Iterate through the documents and retrieve video data
                        for (DocumentSnapshot document : task.getResult()) {
                            String videoTitle = document.getString("title");
                            String videoUrl = document.getString("url");
                            String thumbnailUrl = document.getString("thumbnail");

                            if (videoTitle.toLowerCase().contains(searchText.toLowerCase())) {
                                // Create Uri objects for video and thumbnail
                                Uri videoUri = Uri.parse(videoUrl);
                                Uri thumbnailUri = null;

                                if (thumbnailUrl != null)
                                    thumbnailUri = Uri.parse(thumbnailUrl);

                                // Create a VideoItem object with both video and thumbnail URIs
                                VideoItem videoItem = new VideoItem(videoUri, videoTitle, thumbnailUri);
                                videoItem.setThumbnailUri(thumbnailUri);

                                // Add the VideoItem to the list
                                videoItemList.add(videoItem);
                            }
                        }

                        // Now, you can update your RecyclerView adapter with the new data
                        videoListAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(context, "Failed to fetch videos from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        handleVideoItemClick(position);
    }


    @Override
    public void onEditClick(int position) {
        handleEditClick(position);
    }

    private void handleEditClick(int position) {
        // Redirect to EditVideoActivity
        Intent intent = new Intent(this, EditVideoActivity.class);

        // Pass video information to the EditVideoActivity
        VideoItem videoItem = videoItemList.get(position);
        intent.putExtra("videoUrl", videoItem.getVideoUri().toString());
        intent.putExtra("videoTitle", videoItem.getVideoTitle());
        if(videoItem.getThumbnailUri()!=null)
            intent.putExtra("thumbnailUrl", videoItem.getThumbnailUri().toString());
        else{
            intent.putExtra("thumbnailUrl", "");
        }
        startActivity(intent);
    }



    private void handleVideoItemClick(int position) {
        if (courseVideoListAdapter != null) {
            VideoItem videoItem = videoItemList.get(position);

            // Launch PlayerActivity with the video URL
            Intent intent = new Intent(this,PlayerActivity.class);
            intent.putExtra("videoUrl", videoItem.getVideoUri().toString());
            intent.putExtra("videoTitle",videoItem.getVideoTitle());
            startActivity(intent);
        }
    }

    public void onBackButtonClicked(View view) {
        finish();
    }


}


