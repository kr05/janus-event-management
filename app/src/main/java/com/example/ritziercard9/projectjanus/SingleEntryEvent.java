package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by kevin on 1/16/2018.
 */

public class SingleEntryEvent {
    private String title, location, date, image, details;
    private boolean isActive;
    private Date mTimestamp;

    public SingleEntryEvent() {
    }

    public SingleEntryEvent(String title, String location, String date, String image, String details, boolean isActive) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.image = image;
        this.isActive = isActive;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Date mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
