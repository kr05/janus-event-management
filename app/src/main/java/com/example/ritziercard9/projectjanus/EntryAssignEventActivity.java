package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class EntryAssignEventActivity extends AppCompatActivity implements VerticalStepperForm{

    private static final String TAG = "EntryAssignEvent";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CustomVerticalStepperFormLayout verticalStepperForm;

    private String uid;
    private String selectedEvent;
    private Spinner eventChooserSpinner;
    private Button submitButton, cancelButton;
    private ProgressBar assigningEventProgressCircle;

    private QuerySnapshot currentSnapshots;
    private CollectionReference currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_assign_event);

        setupToolbar();
        extractBundleData(getIntent().getExtras());
        setupStepperForm();
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("entryUID", uid);
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    private void extractBundleData(Bundle extras) {
        if (extras == null) {
            return;
        }
        uid = extras.getString("uid");
        currentEntry = db.collection("accessControl/" + uid + "/events");
    }

    private void setupStepperForm() {
        String[] mySteps = {"Escoje el evento", "Confirmar detalles"};
        int accentColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        int primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        // Finding the view
        verticalStepperForm = findViewById(R.id.entry_assign_event_stepper);

        // Setting up and initializing the form
        CustomVerticalStepperFormLayout.CustomBuilder.newInstance(verticalStepperForm, mySteps, this, this)
                .showVerticalLineWhenStepsAreCollapsed(true)
                .materialDesignInDisabledSteps(true)
                .primaryColor(accentColor)
                .primaryDarkColor(primaryColor)
                .stepTitleTextColor(primaryColor)
                .displayBottomNavigation(false)
                .buttonPressedTextColor(accentColor)
                .confirmationStepEnabled(false)
                .finalStepNextButtonEnabled(false)
                .init();
    }

    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.entryAssignEventToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case 0:
                view = createChooseEventStep();
                break;
            case 1:
                view = createConfirmationStep();
                break;
        }
        return view;
    }

    @Override
    public void onStepOpening(int stepNumber) {
        switch (stepNumber) {
            case 0:
                verticalStepperForm.setStepAsCompleted(0);
                eventChooserSpinner = findViewById(R.id.eventChooserSpinner);
                eventChooserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedEvent = adapterView.getSelectedItem().toString();
                        Log.d(TAG, "onItemSelected: currently selected event:" + selectedEvent);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        return;
                    }
                });
                setupEventSpinnerListeners();
                break;
            case 1:
                verticalStepperForm.setStepAsCompleted(1);
                submitButton = findViewById(R.id.assignEventConfirmationSubmit);
                cancelButton = findViewById(R.id.assignEventConfirmationCancel);
                assigningEventProgressCircle = findViewById(R.id.assignEventProgressCircle);
                break;
        }
    }

    @Override
    public void sendData() {
        SingleEntryEvent singleEvent = null;
        String eventUid = null;
        for (DocumentSnapshot doc : currentSnapshots) {
            if (doc.get("title").equals(selectedEvent)) {
                singleEvent = doc.toObject(SingleEntryEvent.class);
                eventUid = doc.getId();
            }
        }

        if (singleEvent == null) {
            return;
        }

        singleEvent.setActive(true);

        currentEntry.document(eventUid).set(singleEvent).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: event added successfully");
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });
    }

    private void setupEventSpinnerListeners() {
        db.collection("events" ).orderBy("title").addSnapshotListener((documentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            updateTicketSpinner(documentSnapshots);
        });
    }

    private void updateTicketSpinner(QuerySnapshot snapshots) {
        currentSnapshots = snapshots;
        ArrayList<String> spinnerArray = new ArrayList<String>();

        for (DocumentSnapshot doc : snapshots) {
            if (doc.get("title") != null) {
                spinnerArray.add(doc.getString("title"));
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.selected_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventChooserSpinner.setAdapter(spinnerArrayAdapter);
    }


    private View createConfirmationStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.stepper_confirmation, null, false);

        return receiptLayoutContent;
    }

    private View createChooseEventStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.stepper_event_chooser, null, false);

        return receiptLayoutContent;
    }

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            assigningEventProgressCircle.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            assigningEventProgressCircle.setVisibility(View.INVISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    public void onEventChooserSubmit(View view) {
        toggleButtonVisibilityOnProcessing(true);
        sendData();
    }

    public void onEventChooserCancel(View view) {
        finish();
    }
}
