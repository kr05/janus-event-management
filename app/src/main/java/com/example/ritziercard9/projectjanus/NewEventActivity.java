package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NewEventActivity";
    private ActionBar ab;
    private Button sendButton, cancelButton;
    private ProgressBar creatingEventProgressCircle;
    private TextView bandTextView, cityTextView, date, time, capacityTextView, descriptionTextView;
    private CollectionReference eventsCollection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        eventsCollection = db.collection("events");

        Toolbar myToolbar = findViewById(R.id.newEventToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        init();
    }

    private void init() {
        sendButton = findViewById(R.id.newEventSendButton);
        cancelButton = findViewById(R.id.newEventCancelButton);

        creatingEventProgressCircle = findViewById(R.id.newEventProgressCircle);
    }

    private void initTextViews() {
        bandTextView = findViewById(R.id.newEventBandName);
        cityTextView = findViewById(R.id.newEventCity);
        date = findViewById(R.id.newEventDate);
        time = findViewById(R.id.newEventTime);
        capacityTextView = findViewById(R.id.newEventCapacity);
        descriptionTextView = findViewById(R.id.newEventDescription);
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "events");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    public void onNewEventCancel(View view) {
        finish();
    }

    public void onNewEventSend(View view) {
        toggleButtonVisibilityOnProcessing(true);
        initTextViews();

        String band = bandTextView.getText().toString();
        String location = cityTextView.getText().toString();
        String dateString = date.getText().toString();
        String timeString = time.getText().toString();
        String capacity = capacityTextView.getText().toString();
        String description = descriptionTextView.getText().toString();

        if (TextUtils.isEmpty(band)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese el nombre de la banda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(location)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la cuidad y el estado.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(dateString)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la fecha del evento.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(timeString)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la hora del evento.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(capacity)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la capacidad del evento. Un estimado es suficiente.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", band);
        data.put("location", location);
        data.put("date", dateString);
        data.put("time", timeString);
        data.put("capacity", Integer.parseInt(capacity));
        data.put("details", description);

        eventsCollection.add(data).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Receipt added successfully with ID:" + documentReference.getId());
            Intent intent = new Intent();
            intent.putExtra("title", band);
            intent.putExtra("date", dateString);
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });


    }

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            creatingEventProgressCircle.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            creatingEventProgressCircle.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }
}
