package com.nrt.e_learning;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.nrt.e_learning.adapters.YoutubeAdapter;
import com.nrt.e_learning.model.VideoItem;
import com.nrt.e_learning.util.AndroidUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Youtube_player_activity extends AppCompatActivity  implements YoutubeAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
   private List<VideoItem> videoItemList;
    private YoutubeAdapter youtubeAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        recyclerView = findViewById(R.id.youtubeVideosList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch video IDs from YouTube API asynchronously
        new FetchVideoIdsTask().execute();
        videoItemList = new ArrayList<>();
        // Initialize adapter with an empty list (it will be updated when video IDs are fetched)
        youtubeAdapter = new YoutubeAdapter(videoItemList, this, this);
        recyclerView.setAdapter(youtubeAdapter);

    }

    @Override
    public void onItemClick(int position) {
        handleVideoItemClick(position);
    }


    private void handleVideoItemClick(int position) {
        Log.i("handleVideoItemClick", String.valueOf(position));
        if (youtubeAdapter != null) {
            if (videoItemList.size() != 0) {
                VideoItem videoItem = videoItemList.get(position);
                Log.i("click", videoItem.getVideoUri().toString());
                // Launch YoutubePlayer with the video URL

//                Intent intent = new Intent(this, PlayerActivity.class);
//                intent.putExtra("videoUrl", videoItem.getVideoUri().toString());
//                intent.putExtra("videoTitle", videoItem.getVideoTitle());
//                startActivity(intent);
                String videoId = videoItem.getVideoUri().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                startActivity(intent);


            } else
                AndroidUtil.showToast(this, "no video found");
        }
    }

    public void onBackButtonClicked(View view) {
        finish();
    }

    private class FetchVideoIdsTask extends AsyncTask<String, Void, List<VideoItem>> {

        @Override
        protected List<VideoItem> doInBackground(String... pageTokens) {
            try {
                String apiUrl = "https://youtube.googleapis.com/youtube/v3/playlistItems?playlistId=PLBV9mwz9xpe1z0Kjwu_WxtWDI4x4Kh9yE&key=AIzaSyCFc1UccT89eayyWQ6h-8nuarnZp13ReQM&order=date&part=id,snippet&maxResults=40";

                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";


                List<VideoItem> videoIds = parseVideoIds(response);
                return videoIds;
            } catch (IOException e) {
                Log.e("FetchVideoIdsTask", "Error fetching video IDs", e);
            }

            return null;
        }

       @Override
       protected void onPostExecute(List<VideoItem> videoIds) {
           super.onPostExecute(videoIds);

           if (videoIds != null) {
               // Update the adapter with the new data
               youtubeAdapter.setVideoItems(videoIds);
               videoItemList = videoIds;
               // Notify the adapter that the data set has changed
               youtubeAdapter.notifyDataSetChanged();
           } else {
               Log.e("youtube", "Video IDs are null");
               // Handle the case where video retrieval has failed
           }
       }



        private List<VideoItem> parseVideoIds(String jsonResponse) {
            List<VideoItem> videoIds = new ArrayList<VideoItem>();


            try {
                JSONObject json = new JSONObject(jsonResponse);
                JSONArray items = json.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject snippet = item.getJSONObject("snippet");
                    JSONObject resourceId = snippet.getJSONObject("resourceId");
                    JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                    JSONObject highThumbnail = thumbnails.getJSONObject("high");

                    Uri highQualityThumbnailUrl = Uri.parse(highThumbnail.getString("url"));
                    Uri videoUrl = Uri.parse(resourceId.getString("videoId"));
                    String VideoTitle = snippet.getString("title");

                    VideoItem newViewItem = new VideoItem(videoUrl,VideoTitle,highQualityThumbnailUrl);

                    videoIds.add(newViewItem);
                    Log.d("Thumbnail URL", highQualityThumbnailUrl.toString());
                    Log.d("Video", videoUrl.toString());
                }
            } catch (JSONException e) {
                Log.e("FetchVideoIdsTask", "Error parsing JSON", e);
            }
            return videoIds ;
        }
    }

}
