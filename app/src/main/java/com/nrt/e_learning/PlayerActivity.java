package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.net.Uri;

import android.os.Environment;
import android.view.View;

import android.widget.ImageButton;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;


import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import com.nrt.e_learning.model.VideoItem;
import com.nrt.e_learning.util.AndroidUtil;



public class PlayerActivity extends AppCompatActivity {

    private static final String CACHE_DIR ="video_cache";
    private static final int FULL_SCREEN_REQUEST_CODE = 100;
    private StyledPlayerView styledPlayerView;
    private ExoPlayer exoPlayer;

    long currentPosition =0L;
    private String videoUrl;
    private boolean isFullScreen = false;
    AppCompatButton downloadButton;
    String videoTitle;
    TextView textView ;

    ImageButton back_button;

    ImageButton fullScreenButton ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get the video URL from the intent
        videoUrl = getIntent().getStringExtra("videoUrl");
        fullScreenButton = findViewById(R.id.btnFullScreen);

        styledPlayerView = findViewById(R.id.videoViews);
        textView = findViewById(R.id.textViewPlayerTitle);
        back_button = findViewById(R.id.back_button);
        videoTitle  = getIntent().getStringExtra("videoTitle").trim();
        textView.setText(videoTitle);

        initializePlayer();

        styledPlayerView.setControllerVisibilityListener(new StyledPlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                // Toggle visibility of the full-screen button based on player controls visibility
                fullScreenButton.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
            }
        });

        downloadButton = findViewById(R.id.btnDownload);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
            }
        });



        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); releasePlayer();}
        });



        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFullScreenPlayer();
            }
        });
    }

    private void openFullScreenPlayer() {

            // Launch PlayerActivity with the video URL
            Intent intent = new Intent(this, FullScreenPlayerActivity.class);
            intent.putExtra("videoUrl", videoUrl);
            intent.putExtra("videoTitle",videoTitle);
            intent.putExtra("currentDuration",String.valueOf(exoPlayer.getCurrentPosition()));
            startActivityForResult(intent, FULL_SCREEN_REQUEST_CODE);
            exoPlayer.stop();

    }


    private void initializePlayer() {
        // Create a new instance of SimpleExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();

        // Set the player to the StyledPlayerView
        styledPlayerView.setPlayer(exoPlayer);
        styledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        // Set media source
        Uri videoUri = Uri.parse(videoUrl);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.seekTo(currentPosition);
        // Prepare the player
        exoPlayer.prepare();

        exoPlayer.play();
    }

    private void downloadVideo() {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(videoUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        // Set title and description for the download
        request.setTitle(videoTitle);
        request.setDescription("Downloading..."+videoTitle);

        // Set the destination directory for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Elearning/" + videoTitle + ".mp4");
        // Enqueue the download
        AndroidUtil.showToast(getApplicationContext(),"Video is Downloading...");
        downloadManager.enqueue(request);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FULL_SCREEN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            currentPosition = data.getLongExtra("currentPosition", 1000);
//            exoPlayer.seekTo(currentPosition);
//        }
//    }


}
