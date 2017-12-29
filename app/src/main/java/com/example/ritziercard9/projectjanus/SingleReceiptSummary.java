package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by kevin on 12/28/2017.
 */

public class SingleReceiptSummary {
    private String title, price, totalPrice, totalTickets;
    private Date mTimestamp;

    public SingleReceiptSummary(String title, String price, String totalPrice, String totalTickets) {
        this.title = title;
        this.price = price;
        this.totalPrice = totalPrice;
        this.totalTickets = totalTickets;
    }

    public SingleReceiptSummary() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(String totalTickets) {
        this.totalTickets = totalTickets;
    }

    @ServerTimestamp
    public Date getTimestamp() { return mTimestamp; }

    public void setTimestamp(Date timestamp) { mTimestamp = timestamp; }
}
