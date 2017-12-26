package com.example.ritziercard9.projectjanus;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
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
        setContentView(R.layout.activity_validate_tickets);

        onCreateSetupNfc();

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

        Log.d(TAG, "onNewIntent: " + nfcId);

        if (nfcId.equals("04BDC982744080")) {
            Snackbar sb = Snackbar.make(findViewById(R.id.validateTicketsContainer), "Boleto no es valido!", Snackbar.LENGTH_INDEFINITE);
            sb.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            sb.show();
            return;
        }

        scanTicket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeSetupNfc();
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

        DocumentReference docRef = db.document("events/" + uid + "/counters/checkedIn");
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

    public void scanTicket() {
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
        .addOnSuccessListener(result -> {
            Log.d(TAG, "Transaction success:" + result);
            String scannerNotifText = "Boleto escaneado";
            if (!scannerTypeSwitch.isChecked()) {
                scannerNotifText += " para entrada: VALIDO";
            } else {
                scannerNotifText += " para salida: VALIDO";
            }
            Snackbar.make(findViewById(R.id.validateTicketsContainer), scannerNotifText, Snackbar.LENGTH_INDEFINITE).show();
        })
        .addOnFailureListener(e -> Log.w(TAG, "Transaction failure.", e));


    }
}
