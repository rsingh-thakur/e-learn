package com.nrt.e_learning.model;

import android.net.Uri;


public class VideoItem {
    private Uri videoUri;
    private String videoTitle;
    private Uri thumbnailUri;

    private String description;

    private String CategoryName;

    public VideoItem() {
    }

    public Uri getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }


    public Uri getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(Uri thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public VideoItem(Uri videoUri, String videoTitle, Uri thumbnailUri) {
        this.videoUri = videoUri;
        this.videoTitle = videoTitle;
        this.thumbnailUri = thumbnailUri;
    }
}


