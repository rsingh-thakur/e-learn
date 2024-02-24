package com.nrt.e_learning.model;// PdfModel.java

import android.os.Parcelable;

import java.util.ArrayList;

public class PdfItem extends ArrayList<Parcelable> {

    private String fileName;
    private String category;
    private String filePath;
    private String price;
    private String downloadUrl;
    private String uploadedAt;

    // Required empty constructor for Firestore
    public PdfItem() {
    }

    public PdfItem(String fileName, String category, String filePath, String price, String downloadUrl, String uploadedAt) {
        this.fileName = fileName;
        this.category = category;
        this.filePath = filePath;
        this.price = price;
        this.downloadUrl = downloadUrl;
        this.uploadedAt = uploadedAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setDocumentId(String id) {
    }
}
