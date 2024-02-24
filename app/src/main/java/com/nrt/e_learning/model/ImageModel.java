package com.nrt.e_learning.model;

public class ImageModel {

    String title;
    String imageUrl;

    public ImageModel(String title, String imageUrl) {
            this.title = title;
            this.imageUrl = imageUrl;
        }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
