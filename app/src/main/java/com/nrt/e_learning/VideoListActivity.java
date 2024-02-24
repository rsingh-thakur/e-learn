package com.nrt.e_learning;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.adapters.VideoListAdapter;
import com.nrt.e_learning.model.VideoItem;
import com.nrt.e_learning.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity  implements VideoListAdapter.OnItemClickListener {

    private FirebaseFirestore firestore;
    private List<VideoItem> videoItemList;
    private RecyclerView recyclerViewVideos;
    private VideoListAdapter videoListAdapter;

    private EditText searchVideoTitleText ;
    private ImageButton video_search_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        searchVideoTitleText = findViewById(R.id.search_bar);
        video_search_btn = findViewById(R.id.video_search_btn);


        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);
        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this)); // Use LinearLayoutManager or GridLayoutManager as needed

        firestore = FirebaseFirestore.getInstance();
        videoItemList = new ArrayList<>();

        videoListAdapter = new VideoListAdapter(videoItemList, this, this);
        recyclerViewVideos.setAdapter(videoListAdapter);

        fetchVideosFromFirestore();

        video_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedText = searchVideoTitleText.getText().toString().trim();
                AndroidUtil.fetchVideosFromFirestoreBySearchText(getApplicationContext(),searchedText,firestore,videoItemList,videoListAdapter);
                if(videoItemList.size()==0)
                    AndroidUtil.showToast(getApplicationContext(),"No Result Found");
            }
        });

    }



    private void fetchVideosFromFirestore() {
        firestore.collection("videos")
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

                            Log.i("datalog", videoUrl);
                            Log.i("datalog", videoTitle);
                           // Log.i("datalog", thumbnailUrl);

                            // Create Uri objects for video and thumbnail
                            Uri videoUri = Uri.parse(videoUrl);
                            Uri thumbnailUri = null;

                            if (thumbnailUrl!=null)
                                thumbnailUri = Uri.parse(thumbnailUrl);

                            // Create a VideoItem object with both video and thumbnail URIs
                            VideoItem videoItem = new VideoItem(videoUri, videoTitle,thumbnailUri);
                            videoItem.setThumbnailUri(thumbnailUri);

                            // Add the VideoItem to the list
                            videoItemList.add(videoItem);
                        }

                        // Now, you can update your RecyclerView adapter with the new data
                        videoListAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                     AndroidUtil.showToast(getApplicationContext(),"Failed to fetch videos from Firestore");
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
        if (videoListAdapter != null) {
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


