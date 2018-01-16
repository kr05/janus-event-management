package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class OrganizerCobrarEventoActivity extends AppCompatActivity {

    private static final String TAG = "CobrarEvento";
    private Query query;
    private FirestoreRecyclerOptions<SingleReceiptSummary> options;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter mAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String EVENT_UID = "EVENT_UID";
    private static final String SELLER_UID = "UID";

    private String uid;
    private String eventUID;
    private TextView totalTickets, totalPrice, emptyDataTextView;
    private double quantity, total;
    private ProgressBar cobrandoEventoProgressCircle;
    private Button cancelButton, confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_cobrar_evento);

        setupToolbar();
        init(getIntent().getExtras());
    }

    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.organizerCobrarEventoToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void init(Bundle extras) {
        if (extras == null) {
            return;
        }
        uid = extras.getString(SELLER_UID);
        eventUID = extras.getString(EVENT_UID);

        totalTickets = findViewById(R.id.organizerCobrarEventoTotalTickets);
        totalPrice = findViewById(R.id.organizerCobrarEventoTotal);

        cobrandoEventoProgressCircle = findViewById(R.id.organizerCobrarEventoProgressCircle);
        cancelButton = findViewById(R.id.organizerCobrarEventoCancel);
        confirmButton = findViewById(R.id.organizerCobrarEventoConfirm);

        emptyDataTextView = findViewById(R.id.organizerCobrarEventoEmptyTextView);

        setupRecyclerView(uid);
        setupEventListeners();
    }

    private void setupEventListeners() {
        final CollectionReference receiptSummariesRef = db.collection("sellers/" + uid + "/events/" + eventUID + "/receiptSummaries");
        receiptSummariesRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            total = 0;
            quantity = 0;

            for (DocumentSnapshot doc : snapshot) {
                if (doc.get("totalPrice") != null && doc.get("totalTickets") != null) {
                    total += doc.getDouble("totalPrice");
                    quantity += doc.getDouble("totalTickets");
                }
            }
            updateUI(total, (int) quantity);
        });
    }

    private void updateUI(double total, int quantity) {
        totalPrice.setText("$" + String.valueOf(total));
        totalTickets.setText(String.valueOf(quantity));

        if (quantity <= 0) {
            emptyDataTextView.setVisibility(View.VISIBLE);
        } else {
            emptyDataTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void setupRecyclerView(String uid) {
        query = db.collection("sellers/" + uid + "/events/" + eventUID + "/receiptSummaries").orderBy("price");

        // Configure recycler adapter options:
        options = new FirestoreRecyclerOptions.Builder<SingleReceiptSummary>()
                .setQuery(query, SingleReceiptSummary.class)
                .build();

        mRecyclerView = findViewById(R.id.organizerCobrarEventoRecyclerView);

        mRecyclerView.setHasFixedSize(true);

        /// use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleReceiptSummary, OrganizerCobrarEventoActivity.ReceiptSummaryHolder>(options) {

            @Override
            public void onBindViewHolder(OrganizerCobrarEventoActivity.ReceiptSummaryHolder holder, int position, SingleReceiptSummary model) {
                String quantityString = model.getTotalTickets().intValue() + " x " + model.getPrice();

                holder.title.setText(model.getTitle());
                holder.quantity.setText(quantityString);
                holder.totalPrice.setText(Double.toString(model.getTotalPrice()));
            }

            @Override
            public OrganizerCobrarEventoActivity.ReceiptSummaryHolder onCreateViewHolder(ViewGroup group, int viewType) {
                View v = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_receipt_summary_view, group, false);
                return new OrganizerCobrarEventoActivity.ReceiptSummaryHolder(v);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("UID", uid);
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    public void onCobrarEventoConfirm(View view) {
        Log.d(TAG, "COBRANDO EVENTO");

        toggleButtonVisibilityOnProcessing(true);

        if (quantity <= 0) {
            Snackbar.make(findViewById(R.id.organizerCobrarEventoContainer), "No hay boletos vendidos.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("sold", quantity);
        nestedData.put("totalAmount", total);
        nestedData.put("executorUID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        nestedData.put("finishedAt", FieldValue.serverTimestamp());

        CollectionReference paymentReceiptsCollection = db.collection("sellers/" + uid + "/events/" + eventUID + "/paymentReceipts");
        paymentReceiptsCollection.add(nestedData).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Payment receipt pushed successfully");
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding payment receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });
    }

    public void onCobrarEventoCancel(View view) {
        finish();
    }

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            cobrandoEventoProgressCircle.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            cobrandoEventoProgressCircle.setVisibility(View.INVISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    public class ReceiptSummaryHolder extends RecyclerView.ViewHolder {
        TextView title, quantity, totalPrice;

        public ReceiptSummaryHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.receiptSummaryTitle);
            quantity = itemView.findViewById(R.id.receiptSummaryQuantity);
            totalPrice = itemView.findViewById(R.id.receiptSummaryTotalPrice);
        }
    }
}
