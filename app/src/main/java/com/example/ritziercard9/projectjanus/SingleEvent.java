package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

/**
 * Created by ritziercard9 on 11/30/2017.
 */

public class SingleEvent {

    private String image, title, location, details, date, status;
    private int capacity, assigned, sold;
    private Date mTimestamp;

    public SingleEvent() {}

    public SingleEvent(String image, String title, String location, String details, String date, int capacity, int assigned, int sold, String status) {
        this.image = image;
        this.title = title;
        this.location = location;
        this.details = details;
        this.date = date;
        this.capacity = capacity;
        this.assigned = assigned;
        this.status = status;
        this.sold = sold;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @ServerTimestamp
    public Date getTimestamp() { return mTimestamp; }

    public void setTimestamp(Date timestamp) { mTimestamp = timestamp; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAssigned() {
        return assigned;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }
}
