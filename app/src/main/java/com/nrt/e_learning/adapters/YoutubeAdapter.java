package com.nrt.e_learning.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nrt.e_learning.R;
import com.nrt.e_learning.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.VideoViewHolder> {

    private List<VideoItem> videoItems;
    private OnItemClickListener onItemClickListener;
    private Context context;

    public YoutubeAdapter(List<VideoItem> videoItems, OnItemClickListener onItemClickListener, Context context) {
        this.videoItems =videoItems;
        this.onItemClickListener = onItemClickListener;
        this.context = context;
    }

    public void setVideoItems(List<VideoItem> videoItems) {
        this.videoItems = videoItems ;
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
        WebView webViewimage;

        ImageButton btnEdit,playButton;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewVideoThumbnail = itemView.findViewById(R.id.imageViewVideoThumbnail);
            imageViewVideoThumbnail.setVisibility(View.GONE);
            textViewVideoTitle = itemView.findViewById(R.id.textViewVideoTitle);
            playButton = itemView.findViewById(R.id.playButton);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnEdit.setVisibility(View.GONE);
            webViewimage = itemView.findViewById(R.id.webView);
            playButton.setOnClickListener(v -> {
                onItemClickListener.onItemClick(getAdapterPosition());
            });
        }

        void bind(VideoItem videoItem, int currentPosition) {
            textViewVideoTitle.setText(videoItem.getVideoTitle());
            String thumbnailUrl = String.valueOf(videoItem.getThumbnailUri());
            loadThumbnail(thumbnailUrl);
        }

        public void loadThumbnail(String thumbnailUrl) {
            String htmlCode = "<html><body><img src=' "+thumbnailUrl+"' style='width:100%; height:100%;'></body></html>";
            webViewimage.loadData(htmlCode, "text/html", "utf-8");
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }



}
