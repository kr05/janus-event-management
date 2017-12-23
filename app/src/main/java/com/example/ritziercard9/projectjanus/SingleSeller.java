package com.example.ritziercard9.projectjanus;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Created by ritziercard9 on 12/22/2017.
 */

public class SingleSeller {

    private String name, address, phone, email, image;
    private Date mTimestamp;

    public SingleSeller() {
    }

    public SingleSeller(String name, String address, String phone, String email, String image) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @ServerTimestamp
    public Date getTimestamp() { return mTimestamp; }

    public void setTimestamp(Date timestamp) { mTimestamp = timestamp; }
}
