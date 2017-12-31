package com.example.ritziercard9.projectjanus;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class ActivateTicketsActivity extends AppCompatActivity implements VerticalStepperForm {
    private static final String TICKET_TITLE_KEY = "ticketTitle";
    private static final String TICKET_QUANTITY_KEY = "ticketQuantity";
    private static final String TICKET_TOTAL_KEY = "ticketTotal";
    private static final String TAG = "ActivateTicketsActivity";
    private static final String UID = "uid";
    private static final String TICKET_UID = "ticketUID";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference receiptsCollection;

    private Button receiptButton, noReceiptButton;
    private ProgressBar activatingTicketsProgressBar;
    private List<String> scannedTickets;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    private CustomVerticalStepperFormLayout verticalStepperForm;
    private String summaryTicketTitle, summaryTicketQuantity, summaryTicketTotal, ticketUID, uid, sellerUID;
    private int summaryTicketQuantityInt;
    private PendingIntent pendingIntent;
    private NfcAdapter mAdapter;

    //Custom method to convert bytes to String.
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_tickets);

        onCreateSetupNfc();

        scannedTickets = new ArrayList<>();

        Toolbar myToolbar = findViewById(R.id.activateTicketsToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        extractBundleData(getIntent().getExtras());

        receiptsCollection = db.collection("sellers/" + sellerUID + "/events/" + uid + "/receipts");

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

    private void onCreateSetupNfc() {
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter != null && mAdapter.isEnabled()) {
        } else {
            finish();
        }
    }

    private void onResumeSetupNfc() {
        IntentFilter[] intentFiltersArray = new IntentFilter[]{};
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        final String nfcId = bytesToHex(tagFromIntent.getId());

        //Check if ticket has already been scanned (local)
        //Check if expected ticket quantity matches scanned tickets
        //Otherwise, add to ticket array
        if (scannedTickets.contains(nfcId)) {
            Snackbar sb = Snackbar.make(findViewById(R.id.activateTicketsContainer), "Boleto ya a sido escaneado!", Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            sb.show();
            return;
        }

        if (scannedTickets.size() >= summaryTicketQuantityInt) {
            Snackbar sb = Snackbar.make(findViewById(R.id.activateTicketsContainer), "Todos los boletos requeridos han sido escaneados!", Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            sb.show();
            return;
        }


        DocumentReference ticketDocRef = db.document("events/" + uid + "/ticketList/" + nfcId);
        ticketDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Task has been successful");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Snackbar sb = Snackbar.make(findViewById(R.id.activateTicketsContainer), "El boleto ya a sido comprado! Por favor usar otro boleto y contacte a su organizador.", Snackbar.LENGTH_LONG);
                        sb.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                        sb.show();
                    } else {
                        Log.d(TAG, "nfcId passed checks: " + nfcId);
                        Snackbar.make(findViewById(R.id.activateTicketsContainer), "Boleto escaneado:" + nfcId, Snackbar.LENGTH_LONG).show();
                        addCounter(nfcId);
                    }
                } else {
                    Log.d(TAG, "onComplete: Task has NOT been successful");
                    Snackbar sb = Snackbar.make(findViewById(R.id.activateTicketsContainer), "Hubo un error; cheque su coneccion e intente de nuevo.", Snackbar.LENGTH_LONG);
                    sb.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                    sb.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeSetupNfc();
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("UID", uid);
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("sellerUID", sellerUID);
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
        sellerUID = extras.getString("sellerUID");
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
                updateScanningRemainingTicketsTextView(scannedTickets.size());
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
        if (scannedTickets.size() >= summaryTicketQuantityInt) {
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
        data.put("scannedTickets", scannedTickets);

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
        data.put("scannedTickets", scannedTickets);

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

    public void addCounter(String nfcId) {
        scannedTickets.add(nfcId);
        updateScanningRemainingTicketsTextView(scannedTickets.size());
        checkScannedTickets();
    }

    private void updateScanningRemainingTicketsTextView(int scannedTicketsSize) {
        TextView remainingTicketsTextView = findViewById(R.id.remainingTicketsTextView);
        int remainingTickets = summaryTicketQuantityInt - scannedTicketsSize;

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
