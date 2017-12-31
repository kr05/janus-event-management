package com.example.ritziercard9.projectjanus;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by kevin on 12/30/2017.
 */

public class EventTicket {
    private String eventUID, receiptUID, sellerUID, ticketUID, status;
    private boolean isActivated;
    private Date mTimestamp;

    public EventTicket(String eventUID, String receiptUID, String sellerUID, String ticketUID, boolean isActivated, String status) {
        this.eventUID = eventUID;
        this.receiptUID = receiptUID;
        this.sellerUID = sellerUID;
        this.ticketUID = ticketUID;
        this.isActivated = isActivated;
        this.status = status;
    }

    public EventTicket() {
    }

    public String getEventUID() {
        return eventUID;
    }

    public void setEventUID(String eventUID) {
        this.eventUID = eventUID;
    }

    public String getReceiptUID() {
        return receiptUID;
    }

    public void setReceiptUID(String receiptUID) {
        this.receiptUID = receiptUID;
    }

    public String getSellerUID() {
        return sellerUID;
    }

    public void setSellerUID(String sellerUID) {
        this.sellerUID = sellerUID;
    }

    public String getTicketUID() {
        return ticketUID;
    }

    public void setTicketUID(String ticketUID) {
        this.ticketUID = ticketUID;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.mTimestamp = timestamp;
    }
}
