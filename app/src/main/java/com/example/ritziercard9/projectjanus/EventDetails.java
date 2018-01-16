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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

public class EventDetails extends AppCompatActivity {

    private static final String TAG = "EventDetails";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter mAdapter;
    private static DecimalFormat REAL_FORMATTER = new DecimalFormat("0.##");
    private Integer querySize;
    private TextView titleTextView;
    private TextView locationTextView;
    private TextView dateTextView;
    private TextView remainingTicketsTextView;
    private ImageView detailsImageView;
    private Spinner ticketSpinner, quantitySpinner;
    private Query query;
    private FirestoreRecyclerOptions<SingleTicket> options;
    private String uid;
    private String sellerUid;

    private static final int ACTIVATE_TICKETS_REQUEST = 1;
    private int remaining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Toolbar myToolbar = findViewById(R.id.eventDetailsToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        setupEventListeners(getIntent().getExtras());

        titleTextView = findViewById(R.id.eventDetailsTitle);
        locationTextView = findViewById(R.id.eventDetailsLocation);
        detailsImageView = findViewById(R.id.eventDetailsImage);
        ticketSpinner = findViewById(R.id.eventDetailsTicketSpinner);
        quantitySpinner = findViewById(R.id.eventDetailsQuantitySpinner);
        dateTextView = findViewById(R.id.eventDetailsDate);
        remainingTicketsTextView = findViewById(R.id.eventDetailsRemainingTickets);

        //Initializing value in order to prevent error if scan button is pressed before listeners finish updating.
        remaining = 0;

    }

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("UID");
        sellerUid = bundle.getString("sellerUID");

        setupTicketRecyclerView(uid);

        final DocumentReference docRef = db.document("sellers/" + sellerUid + "/events/" + uid);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVATE_TICKETS_REQUEST) {
            if (resultCode == RESULT_OK) {
                boolean hasReceipt = data.getBooleanExtra("hasReceipt", false);
                String ticketActivationToastText;
                if (hasReceipt) {
                    String name = data.getStringExtra("receiptName");
                    ticketActivationToastText = "La compra a sido exitosa con recibo para: " + name;
                } else {
                    ticketActivationToastText = "La compra a sido exitosa SIN recibo para el cliente.";
                }
                Snackbar.make(findViewById(R.id.eventDetailsContainer), ticketActivationToastText, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("sellerUID", sellerUid);
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    private void setupTicketRecyclerView(String uid) {
        query = db.collection("events/" + uid + "/tickets").orderBy("price");

        // Configure recycler adapter options:
        options = new FirestoreRecyclerOptions.Builder<SingleTicket>()
                .setQuery(query, SingleTicket.class)
                .build();

        mRecyclerView = findViewById(R.id.ticketsRecyclerView);

        // use a linear layout manager
        mLayoutManager = new CustomHorizontalLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleTicket, EventDetails.TicketHolder>(options) {
            @Override
            public void onBindViewHolder(EventDetails.TicketHolder holder, int position, SingleTicket model) {
                holder.title.setText(model.getTitle());
                holder.price.setText(REAL_FORMATTER.format(model.getPrice()));
                holder.setIsRecyclable(false);
            }

            @Override
            public EventDetails.TicketHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_ticket_view, group, false);
                int length = options.getSnapshots().size();
                querySize = length;
                view.getLayoutParams().width = group.getWidth() / length;
                return new EventDetails.TicketHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                updateTicketSpinner(options.getSnapshots());
                if (querySize == null) {
                    return;
                } else {
                    int newQuerySize = options.getSnapshots().size();
                    if (newQuerySize != querySize) {
                        querySize = newQuerySize;
                        notifyDataSetChanged();
                    }
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void updateTicketSpinner(ObservableSnapshotArray<SingleTicket> snapshots) {
        ArrayList<String> spinnerArray = new ArrayList<String>();
        Iterator<SingleTicket> iterator = snapshots.iterator();

        while (iterator.hasNext()) {
            SingleTicket curTicket = iterator.next();
            spinnerArray.add(curTicket.getTitle());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.selected_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ticketSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void updateUI(Map<String, Object> data) {
        titleTextView.setText(data.get("title").toString());
        locationTextView.setText(data.get("location").toString());
        dateTextView.setText(data.get("date").toString());

        if (data.get("assigned") != null) {
            int sold = 0;
            int assigned = ((Long) data.get("assigned")).intValue();
            if (data.get("sold") != null) {
                sold = ((Long) data.get("sold")).intValue();
            }

            remaining = assigned - sold;

            remainingTicketsTextView.setText(String.valueOf(remaining));
        }

        if (data.get("image") != null) {
            Glide.with(getApplicationContext())
                    .load(data.get("image").toString())
                    .into(detailsImageView);
        }
    }

    public void onEscanearClick(View view) {
        Intent intent = new Intent(this, ActivateTicketsActivity.class);

        if (ticketSpinner.getSelectedItem() == null || quantitySpinner.getSelectedItem() == null) {
            return;
        }

        if (remaining <= 0) {
            Snackbar.make(findViewById(R.id.eventDetailsContainer), "No hay boletos restantes.", Snackbar.LENGTH_LONG).show();
            return;
        }

        String ticketTitle = ticketSpinner.getSelectedItem().toString();
        String ticketQuantity = quantitySpinner.getSelectedItem().toString();
        Double ticketTotal = null;
        Double ticketPrice = null;
        String ticketUID = "";

        if (remaining < Integer.parseInt(ticketQuantity)) {
            Snackbar.make(findViewById(R.id.eventDetailsContainer), "No hay boletos suficientes para la compra.", Snackbar.LENGTH_LONG).show();
            return;
        }

        ListIterator<SingleTicket> iterator = options.getSnapshots().listIterator();

        while (iterator.hasNext() && ticketTotal == null) {
            SingleTicket curTicket = iterator.next();
            if ((curTicket.getTitle()).equals(ticketTitle)) {
                ticketPrice = curTicket.getPrice();
                ticketTotal = ticketPrice * Integer.parseInt(ticketQuantity);
                ticketUID = options.getSnapshots().getSnapshot(iterator.previousIndex()).getId();

            }
        }


        Bundle bundle = new Bundle();
        bundle.putString("ticketTitle", ticketTitle);
        bundle.putString("ticketQuantity", ticketQuantity);
        bundle.putString("ticketTotal", REAL_FORMATTER.format(ticketTotal));
        bundle.putDouble("ticketPrice", ticketPrice);
        bundle.putString("uid", uid);
        bundle.putString("ticketUID", ticketUID);
        bundle.putString("sellerUID", sellerUid);
        intent.putExtras(bundle);

        Log.d(TAG, "onEscanearClick: BUNDLE:" + bundle);

        startActivityForResult(intent, ACTIVATE_TICKETS_REQUEST);
    }

    public class TicketHolder extends RecyclerView.ViewHolder {
        TextView title, price;

        public TicketHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.ticketTitleTextView);
            price = itemView.findViewById(R.id.ticketPriceTextView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}
