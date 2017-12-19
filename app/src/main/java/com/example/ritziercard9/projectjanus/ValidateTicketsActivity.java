package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class ValidateTicketsActivity extends AppCompatActivity {

    private static final String TAG = "ValidateTicketsActivity";
    private String uid;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView nfcImageView;
    private TextView counterTextView;
    private Switch scannerTypeSwitch;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_tickets);

        Toolbar myToolbar = findViewById(R.id.validateTicketsToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        counterTextView = findViewById(R.id.validateTicketsCounter);
        nfcImageView = findViewById(R.id.validateTicketsNfcIcon);
        scannerTypeSwitch = findViewById(R.id.validateTicketsSwitch);

        setUpSwitch();

        setupEventListeners(getIntent().getExtras());
    }

    private void setUpSwitch() {

        nfcImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.entryGreen));
        ab.setTitle("Entrada");

        scannerTypeSwitch.setChecked(false);

        scannerTypeSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(isChecked){
                // If the switch button is on
                nfcImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                ab.setTitle("Salida");
            }
            else {
                // If the switch button is off
                ab.setTitle("Entrada");
                nfcImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.entryGreen));
            }
        });
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("UID", uid);
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("UID");

        final DocumentReference docRef = db.document("events/" + uid + "/counters/checkedIn");
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                updateUI(snapshot);
            } else {
                Log.d(TAG, "Current data: null");
                Map<String, Object> defaultVal = new HashMap<>();
                defaultVal.put("value", 0);
                docRef.set(defaultVal);
            }
        });
    }

    private void updateUI(DocumentSnapshot snapshot) {
        if (snapshot.get("value") != null) {
            counterTextView.setText(String.valueOf(snapshot.getDouble("value").intValue()));
        }
    }

    public void onValidateImageClick(View view) {
        Log.d(TAG, "onValidateImageClick: CLICK");
        final DocumentReference checkedInDocRef = db.document("events/" + uid + "/counters/checkedIn");

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(checkedInDocRef);
            double updatedCheckedIn;
            if (!scannerTypeSwitch.isChecked()) {
                updatedCheckedIn = snapshot.getDouble("value") + 1;
            } else {
                updatedCheckedIn = snapshot.getDouble("value") - 1;
            }

            if (updatedCheckedIn < 0) {
                throw new FirebaseFirestoreException("Checked-in counter less than zero",
                        FirebaseFirestoreException.Code.ABORTED);
            } else {
                transaction.update(checkedInDocRef, "value", updatedCheckedIn);
                // Success
                return updatedCheckedIn;
            }

        })
        .addOnSuccessListener(result -> Log.d(TAG, "Transaction success:" + result))
        .addOnFailureListener(e -> Log.w(TAG, "Transaction failure.", e));


    }
}
