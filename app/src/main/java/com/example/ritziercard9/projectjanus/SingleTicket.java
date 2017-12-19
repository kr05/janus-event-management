package com.example.ritziercard9.projectjanus;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by ritziercard9 on 12/2/2017.
 */

public class SingleTicket {
    private String title;
    private double price;
    private Date mTimestamp;

    public SingleTicket() {}

    public SingleTicket(String title, double price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.mTimestamp = timestamp;
    }
}
