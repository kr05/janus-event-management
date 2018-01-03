package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by kevin on 12/28/2017.
 */

public class SingleReceiptSummary {
    private String title;
    private Double price, totalPrice, totalTickets;
    private Date mTimestamp;

    public SingleReceiptSummary(String title, Double price, Double totalPrice, Double totalTickets) {
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Double totalTickets) {
        this.totalTickets = totalTickets;
    }

    @ServerTimestamp
    public Date getTimestamp() { return mTimestamp; }

    public void setTimestamp(Date timestamp) { mTimestamp = timestamp; }
}
