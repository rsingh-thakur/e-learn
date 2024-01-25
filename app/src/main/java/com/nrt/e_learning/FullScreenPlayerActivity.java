package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;



public class FullScreenPlayerActivity extends AppCompatActivity {
   private long currentDuration=0L;

    private StyledPlayerView styledPlayerView;
    private ExoPlayer exoPlayer;
    private String videoUrl;

    ImageButton back_button;

       ImageButton fullscreen_exit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_player);

        // Get the video URL from the intent
        videoUrl = getIntent().getStringExtra("videoUrl");
        currentDuration = Long.parseLong(getIntent().getStringExtra("currentDuration"));

        styledPlayerView = findViewById(R.id.fullvideoViews);
        back_button = findViewById(R.id.back_buttonf);
        fullscreen_exit = findViewById(R.id.btnExitFullScreen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setFullScreenLayout();
        initializePlayer();


        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish(); }
        });



        fullscreen_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish(); }
        });
    }

    private void initializePlayer() {
        // Create a new instance of SimpleExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();

        // Set the player to the StyledPlayerView
        styledPlayerView.setPlayer(exoPlayer);
        // Set media source
        Uri videoUri = Uri.parse(videoUrl);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.seekTo(currentDuration);
        // Prepare the player
        exoPlayer.prepare();

        // Start playback
        exoPlayer.setPlayWhenReady(true);
    }



    @Override
    protected void onDestroy() {
        // Send back the current player position to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("currentPosition", exoPlayer.getCurrentPosition());
        setResult(RESULT_OK, resultIntent);
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

    private void setFullScreenLayout() {
        // Set the layout parameters for full-screen mode
        styledPlayerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

}
