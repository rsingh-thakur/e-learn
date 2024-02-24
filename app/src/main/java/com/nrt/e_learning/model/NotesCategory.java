package com.nrt.e_learning.model;

public class NotesCategory {
    private String categoryName;

    private String price;

    private String period;


    public NotesCategory() {
    }

    public NotesCategory(String categoryName, String price, String period) {
        this.categoryName = categoryName;
        this.price = price;
        this.period = period;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
