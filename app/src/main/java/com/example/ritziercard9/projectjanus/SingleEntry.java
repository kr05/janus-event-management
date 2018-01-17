package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by kevin on 1/16/2018.
 */

public class SingleEntry {

    private String name, email, phone, image;
    private Date mTimestamp;

    public SingleEntry() {
    }

    public SingleEntry(String name, String email, String phone, String image, Date mTimestamp) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.mTimestamp = mTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Date mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
