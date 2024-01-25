package com.nrt.e_learning;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.adapters.VideoListAdapter;
import com.nrt.e_learning.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity  implements VideoListAdapter.OnItemClickListener {

    private FirebaseFirestore firestore;
    private List<VideoItem> videoItemList;
    private RecyclerView recyclerViewVideos;
    private VideoListAdapter videoListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);
        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this)); // Use LinearLayoutManager or GridLayoutManager as needed

        firestore = FirebaseFirestore.getInstance();
        videoItemList = new ArrayList<>();
       // videoItemList =   generateSampleVideoItems();
        // Handle item click
        videoListAdapter = new VideoListAdapter(videoItemList, this, this);
        recyclerViewVideos.setAdapter(videoListAdapter);
        fetchVideosFromFirestore();
//        videoListAdapter.notifyDataSetChanged();
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
                        Toast.makeText(VideoListActivity.this, "Failed to fetch videos from Firestore", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("videoUrl", videoItem.getVideoUri().toString());
            intent.putExtra("videoTitle",videoItem.getVideoTitle());
            startActivity(intent);
        }
    }
}


