package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class OrganizerCobrarEventoActivity extends AppCompatActivity {

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

        setupRecyclerView(uid);
    }

    private void setupRecyclerView(String uid) {
        query = db.collection("sellers/" + uid + "/receiptsSummary").orderBy("totalPrice");

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
                String quantityString = model.getTotalTickets() + " x " + model.getPrice();

                holder.title.setText(model.getTitle());
                holder.quantity.setText(quantityString);
                holder.totalPrice.setText(model.getTotalPrice());
            }

            @Override
            public OrganizerCobrarEventoActivity.ReceiptSummaryHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_receipt_summary_view, group, false);
                return new OrganizerCobrarEventoActivity.ReceiptSummaryHolder(view);
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
