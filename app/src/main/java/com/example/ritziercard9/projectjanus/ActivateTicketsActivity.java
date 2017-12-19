package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class ActivateTicketsActivity extends AppCompatActivity implements VerticalStepperForm {
    private static final String TICKET_TITLE_KEY = "ticketTitle";
    private static final String TICKET_QUANTITY_KEY = "ticketQuantity";
    private static final String TICKET_TOTAL_KEY = "ticketTotal";
    private static final String TAG = "ActivateTicketsActivity";
    private static final String UID = "uid";
    private static final String TICKET_UID = "ticketUID";
    private int counter = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference receiptsCollection;

    private Button receiptButton, noReceiptButton;
    private ProgressBar activatingTicketsProgressBar;


    private CustomVerticalStepperFormLayout verticalStepperForm;
    private String summaryTicketTitle, summaryTicketQuantity, summaryTicketTotal, ticketUID, uid;
    private int summaryTicketQuantityInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_tickets);

        Toolbar myToolbar = findViewById(R.id.activateTicketsToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        extractBundleData(getIntent().getExtras());

        receiptsCollection = db.collection("events/" + uid + "/receipts");

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        String[] mySteps = {"Escanear boletos", "Confirmar transaccion", "Enviar recibo"};
        int accentColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        int primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        // Finding the view
        verticalStepperForm = findViewById(R.id.activate_tickets_stepper);

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

        summaryTicketTitle = extras.getString(TICKET_TITLE_KEY);
        summaryTicketQuantity = extras.getString(TICKET_QUANTITY_KEY);
        summaryTicketQuantityInt = Integer.parseInt(summaryTicketQuantity);
        summaryTicketTotal = extras.getString(TICKET_TOTAL_KEY);
        uid = extras.getString(UID);
        ticketUID = extras.getString(TICKET_UID);
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case 0:
                view = createScanningStep();
                break;
            case 1:
                view = createConfirmStep();
                break;
            case 2:
                view = createReceiptStep();
                break;
        }
        return view;
    }

    private View createReceiptStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout receiptLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.receipt_stepper_view, null, false);

        return receiptLayoutContent;
    }

    private View createConfirmStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout summaryLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.summary_stepper_view, null, false);
        return summaryLayoutContent;
    }

    private View createScanningStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        ConstraintLayout scanLayoutContent = (ConstraintLayout) inflater.inflate(R.layout.scanning_stepper_view, null, false);
        return scanLayoutContent;
    }

    @Override
    public void onStepOpening(int stepNumber) {
        switch (stepNumber) {
            case 0:
                ImageView image = findViewById(R.id.scanningStepperImageView);
                image.setImageResource(R.drawable.ic_nfc);
                updateScanningRemainingTicketsTextView(counter);
                checkScannedTickets();
                break;
            case 1:
                verticalStepperForm.setStepAsCompleted(1);
                TextView summaryTicketTitleTextView = findViewById(R.id.summaryTicketTitleTextView);
                TextView summaryTicketQuantityTextView = findViewById(R.id.summaryTicketQuantityTextView);
                TextView summaryTicketTotalTextView = findViewById(R.id.summaryTicketTotalTextView);

                summaryTicketTitleTextView.setText(summaryTicketTitle);
                summaryTicketQuantityTextView.setText(summaryTicketQuantity);
                summaryTicketTotalTextView.setText(summaryTicketTotal);

                break;
            case 2:
                verticalStepperForm.setStepAsCompleted(2);
                receiptButton = findViewById(R.id.summaryReceiptButton);
                noReceiptButton = findViewById(R.id.summaryNoReceiptButton);
                activatingTicketsProgressBar = findViewById(R.id.summaryReceiptProgressBar);
                break;
        }

    }

    private void checkScannedTickets() {
        if (counter >= summaryTicketQuantityInt) {
            verticalStepperForm.setActiveStepAsCompleted();
        } else {
            String errorMessage = "Please scan all tickets.";
            verticalStepperForm.setActiveStepAsUncompleted(errorMessage);
        }
    }

    @Override
    public void sendData() {
        Log.d(TAG, "sendData: SENDING DATA");

        TextView nameTextView = findViewById(R.id.summaryNameTextView);
        TextView phoneTextView = findViewById(R.id.summaryPhoneTextView);
        TextView emailTextView = findViewById(R.id.summaryEmailTextView);

        String name = nameTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String email = emailTextView.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(findViewById(R.id.activateTicketsContainer), "Por favor ingrese el nombre del cliente antes de terminar.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
            Snackbar.make(findViewById(R.id.activateTicketsContainer), "Por favor ingrese el correo electronico y/o el telefono antes de continuar.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("email", email);
        data.put("ticketUID", ticketUID);
        data.put("ticketQuantity", summaryTicketQuantityInt);
        data.put("ticketTotal", Double.parseDouble(summaryTicketTotal));
        data.put("customerReceiptSent", true);

        receiptsCollection.add(data).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Receipt added successfully with ID:" + documentReference.getId());
            Intent intent = new Intent();
            intent.putExtra("hasReceipt", true);
            intent.putExtra("receiptName", name);
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });


    }

    public void sendDataWithoutReceipt() {
        Log.d(TAG, "sendData: SENDING DATA");

        Map<String, Object> data = new HashMap<>();
        data.put("ticketUID", ticketUID);
        data.put("ticketQuantity", summaryTicketQuantityInt);
        data.put("ticketTotal", Double.parseDouble(summaryTicketTotal));
        data.put("customerReceiptSent", false);

        receiptsCollection.add(data).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Receipt added successfully with ID:" + documentReference.getId());
            Intent intent = new Intent();
            intent.putExtra("hasReceipt", false);
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });


    }

    public void addCounter(View view) {
        counter++;
        updateScanningRemainingTicketsTextView(counter);
        checkScannedTickets();
    }

    private void updateScanningRemainingTicketsTextView(int counter) {
        TextView remainingTicketsTextView = findViewById(R.id.remainingTicketsTextView);
        int remainingTickets = summaryTicketQuantityInt - counter;

        if (remainingTickets < 0) {
            remainingTickets = 0;
        }

        remainingTicketsTextView.setText("Boletos restantes: " + remainingTickets);
    }

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            activatingTicketsProgressBar.setVisibility(View.VISIBLE);
            receiptButton.setVisibility(View.INVISIBLE);
            noReceiptButton.setVisibility(View.INVISIBLE);
        } else {
            activatingTicketsProgressBar.setVisibility(View.INVISIBLE);
            receiptButton.setVisibility(View.VISIBLE);
            noReceiptButton.setVisibility(View.VISIBLE);
        }
    }

    public void onSendReceiptClick(View view) {
        Log.d(TAG, "onSendReceiptClick: Click!");
        toggleButtonVisibilityOnProcessing(true);
        sendData();
    }

    public void onFinishWithoutReceipt(View view) {
        toggleButtonVisibilityOnProcessing(true);
        sendDataWithoutReceipt();
    }
}
