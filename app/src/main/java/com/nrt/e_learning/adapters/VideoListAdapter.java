package com.nrt.e_learning.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.nrt.e_learning.R;
import com.nrt.e_learning.model.VideoItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private List<VideoItem> videoItems;
    private Context context;
    private OnItemClickListener onItemClickListener;


    private int currentPlayingPosition = -1;

    public VideoListAdapter(List<VideoItem> videoItems, OnItemClickListener onItemClickListener, Context context) {
        this.videoItems = videoItems;
        this.onItemClickListener = onItemClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_video_item, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem videoItem = videoItems.get(position);
        holder.bind(videoItem, position);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewVideoThumbnail;
        TextView textViewVideoTitle;

        ImageButton btnEdit;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewVideoThumbnail = itemView.findViewById(R.id.imageViewVideoThumbnail);
            textViewVideoTitle = itemView.findViewById(R.id.textViewVideoTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            // Set click listener for the thumbnail
            imageViewVideoThumbnail.setOnClickListener(v -> {
                onItemClickListener.onItemClick(getAdapterPosition());
            });

            btnEdit.setOnClickListener(v -> {
                onItemClickListener.onEditClick(getAdapterPosition());
            });
        }

        void bind(VideoItem videoItem, int currentPosition) {
            textViewVideoTitle.setText(videoItem.getVideoTitle());

            if (videoItem.getThumbnailUri() != null) {
                // If the thumbnail URI is available, load it from Firebase Storage using Glide
                Glide.with(context)
                        .load(videoItem.getThumbnailUri().toString())
                        .placeholder(R.drawable.ic_video) // Placeholder image while loading
                        .into(imageViewVideoThumbnail);
            } else {
                // If the thumbnail URI is null, set a default placeholder
                imageViewVideoThumbnail.setImageResource(R.drawable.ic_video);
            }
        }
    }

        public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
    }


}
