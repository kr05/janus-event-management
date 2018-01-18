package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ScannerEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ScannerEventDetails";
    private TextView titleTextView, locationTextView, detailsTextView, totalCapacityTextView, soldTicketsTextView, checkedInTextView, dateTextView;
    private ImageView detailsImageView;
    private String uid, entryUID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_event_details);

        Toolbar myToolbar = findViewById(R.id.scannerEventDetailsToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        titleTextView = findViewById(R.id.scannerEventDetailsTitle);
        locationTextView = findViewById(R.id.scannerEventDetailsLocation);
        detailsTextView = findViewById(R.id.scannerEventDetailsExtraInfo);
        detailsImageView = findViewById(R.id.scannerEventDetailsImage);
        totalCapacityTextView = findViewById(R.id.scannerEventDetailsTotalCapacity);
        soldTicketsTextView = findViewById(R.id.scannerEventDetailsSoldTickets);
        checkedInTextView = findViewById(R.id.scannerEventDetailsCheckedIn);
        dateTextView = findViewById(R.id.scannerEventDetailsDate);

        setupEventListeners(getIntent().getExtras());

    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("entryUID", entryUID);
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    public void onScannerEventDetailsFabClick(View view) {
        Intent intent = new Intent(this, ValidateTicketsActivity.class);
        intent.putExtra("UID", uid);
        startActivity(intent);
    }

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("UID");
        entryUID = bundle.getString("entryUID");

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

        final DocumentReference checkedInRef = db.document("events/" + uid + "/counters/checkedIn");
        checkedInRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                updateCheckedInCounter(snapshot);
            } else {
                Log.d(TAG, "Current data: null");
                Map<String, Object> defaultVal = new HashMap<>();
                defaultVal.put("value", 0);
                checkedInRef.set(defaultVal);
            }
        });
    }

    private void updateCheckedInCounter(DocumentSnapshot snapshot) {
        if (snapshot.get("value") != null) {
            checkedInTextView.setText(String.valueOf(snapshot.getDouble("value").intValue()));
        }
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

        if (data.get("details") != null) {
            detailsTextView.setText(data.get("details").toString());
        }

        if (data.get("capacity") != null) {
            totalCapacityTextView.setText(data.get("capacity").toString());
        }

        if (data.get("image") != null) {
            Glide.with(getApplicationContext())
                    .load(data.get("image").toString())
                    .into(detailsImageView);
        }
    }

}
