package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class SellerAssignEventActivity extends AppCompatActivity implements VerticalStepperForm {

    private static final String TAG = "SellerAssignEvent";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CustomVerticalStepperFormLayout verticalStepperForm;

    private Button submitButton, cancelButton;
    private ProgressBar assigningEventProgressCircle;
    private EditText quantityEditText;
    private Spinner eventChooserSpinner;
    private String currentQuantity;
    private String selectedEvent;
    private String uid;
    private QuerySnapshot currentSnapshots;
    private CollectionReference currentSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_assign_event);

        Toolbar myToolbar = findViewById(R.id.assignEventToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        extractBundleData(getIntent().getExtras());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        String[] mySteps = {"Escoje el evento", "Ingresar numero de boletos", "Confirmar detalles"};
        int accentColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        int primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        // Finding the view
        verticalStepperForm = findViewById(R.id.asssign_event_stepper);

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

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("UID", uid);
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
        currentSeller = db.collection("sellers/" + uid + "/events");
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case 0:
                view = createChooseEventStep();
                break;
            case 1:
                view = createEnterQuantityStep();
                break;
            case 2:
                view = createConfirmationStep();
                break;
        }
        return view;
    }

    private View createConfirmationStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.stepper_confirmation, null, false);

        return receiptLayoutContent;
    }

    private View createEnterQuantityStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.stepper_quantity_chooser, null, false);

        return receiptLayoutContent;
    }

    private View createChooseEventStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.stepper_event_chooser, null, false);

        return receiptLayoutContent;
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
                quantityEditText = findViewById(R.id.quantityChooserEditText);
                quantityEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        return;
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        return;
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        checkQuantity();
                        currentQuantity = editable.toString();
                        Log.d(TAG, "afterTextChanged: " + currentQuantity);
                    }
                });
                checkQuantity();
                break;
            case 2:
                verticalStepperForm.setStepAsCompleted(2);
                submitButton = findViewById(R.id.assignEventConfirmationSubmit);
                cancelButton = findViewById(R.id.assignEventConfirmationCancel);
                assigningEventProgressCircle = findViewById(R.id.assignEventProgressCircle);
                break;
        }
    }

    private void checkQuantity() {
        if (!TextUtils.isEmpty(quantityEditText.getText().toString())) {
            verticalStepperForm.setStepAsCompleted(1);
        }
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

    @Override
    public void sendData() {
        SingleEvent singleEvent = null;
        String eventUid = null;
        for (DocumentSnapshot doc : currentSnapshots) {
            if (doc.get("title").equals(selectedEvent)) {
                singleEvent = doc.toObject(SingleEvent.class);
                eventUid = doc.getId();
            }
        }

        if (singleEvent == null) {
            return;
        }

        singleEvent.setAssigned(Integer.parseInt(currentQuantity));

        currentSeller.document(eventUid).set(singleEvent).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: event added successfully");
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });

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
