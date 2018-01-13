package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerEventDetails";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView titleTextView, locationTextView, dateTextView, capacityTextView;
    private Button soldTicketsCounter;
    private ImageView detailsImageView;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_details);

        Toolbar myToolbar = findViewById(R.id.organizerEventDetailsToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        titleTextView = findViewById(R.id.organizerEventDetailsTitle);
        locationTextView = findViewById(R.id.organizerEventDetailsLocation);
        dateTextView = findViewById(R.id.organizerEventDetailsDate);
        detailsImageView = findViewById(R.id.organizerEventDetailsImage);
        capacityTextView = findViewById(R.id.organizerEventDetailsCapacity);
        soldTicketsCounter = findViewById(R.id.organizerEventDetailsTicketCounter);

        setupEventListeners(getIntent().getExtras());
    }

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("UID");

        final DocumentReference docRef = db.document("events/" + uid);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                updateUI(snapshot.getData());
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "events");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    private void updateUI(Map<String, Object> data) {

        if (data.get("title") != null) {
            titleTextView.setText(data.get("title").toString());
        }

        if (data.get("location") != null) {
            locationTextView.setText(data.get("location").toString());
        }

        if (data.get("date") != null) {
            dateTextView.setText(data.get("date").toString());
        }

        if (data.get("capacity") != null) {
            capacityTextView.setText(data.get("capacity").toString());
        }

        if (data.get("image") != null) {
            Glide.with(getApplicationContext())
                    .load(data.get("image").toString())
                    .into(detailsImageView);
        }


    }

    public void onActivarConteoClick(View view) {

        final CollectionReference ticketsCollection = db.collection("events/" + uid + "/ticketList");
        ticketsCollection.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Snackbar sb = Snackbar.make(findViewById(R.id.organizerEventDetailsContainer), "No se pudo contar boletos, intente de nuevo:" + e.getMessage(), Snackbar.LENGTH_INDEFINITE);
                sb.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                sb.show();
                return;
            }

            if (snapshot != null && !snapshot.isEmpty()) {
                Log.d(TAG, "Current number of ticketrs: " + snapshot.size());
                updateTicketCounter(snapshot.size());
            } else {
                Log.d(TAG, "no tickets found");
                updateTicketCounter(0);
            }
        });

    }

    private void updateTicketCounter(int size) {
        soldTicketsCounter.setText(Integer.toString(size));
    }
}
