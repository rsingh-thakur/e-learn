package com.nrt.e_learning.adapters;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.nrt.e_learning.R;
import com.nrt.e_learning.model.Videomodel;


public class VideoAdapter extends FirestoreRecyclerAdapter<Videomodel, VideoAdapter.myviewholder> {

    public VideoAdapter(@NonNull FirestoreRecyclerOptions<Videomodel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull Videomodel model) {
        holder.setdata(model);
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        return new myviewholder(view);
    }

    static class myviewholder extends RecyclerView.ViewHolder {
        VideoView videoView;
        TextView title, desc;
        ProgressBar pbar;

        ImageButton playButton;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            title = itemView.findViewById(R.id.textVideoTitle);
            desc = itemView.findViewById(R.id.textVideoDescription);
            pbar = itemView.findViewById(R.id.videoProgressBar);
            playButton = itemView.findViewById(R.id.playButton);
            videoView.start();

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Video playback started, hide the progress bar
                    videoView.start();
                    pbar.setVisibility(View.GONE);
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePlayPauseShort();
                }
            });

        }

        void setdata(Videomodel obj) {
            videoView.setVideoPath(obj.getUrl());
            title.setText(obj.getTitle());
            desc.setText(obj.getDesc());

            Log.i("short",title +" "+desc);
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePlayPauseShort();
                }
            });

        }



         void  togglePlayPauseShort() {
             if (videoView.isPlaying()) {
                 pauseVideo();
             } else {
                 playVideo();
             }
        }
       private void playVideo() {
        videoView.start();
        pbar.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
      }

        private void pauseVideo() {
          videoView.pause();
          playButton.setVisibility(View.VISIBLE);

        }
    }
}
