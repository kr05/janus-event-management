package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.design.widget.Snackbar;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class OrganizerSellerDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerSellerDetails";
    private static final int ASSIGN_EVENT_REQUEST = 50;
    private static final int COBRAR_EVENTO_REQUEST = 51;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView nameTextView, addressTextView, phoneTextView, emailTextView;
    private ImageView sellerImageView;
    private String uid;

    private Query query;
    private FirestoreRecyclerOptions<SingleEvent> options;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_seller_details);

        Toolbar myToolbar = findViewById(R.id.organizerSellerDetailsToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        init();
        setupEventListeners(getIntent().getExtras());
    }

    private void setupRecyclerView(String uid) {
        query = db.collection("sellers/" + uid + "/events");

        // Configure recycler adapter options:
        options = new FirestoreRecyclerOptions.Builder<SingleEvent>()
                .setQuery(query, SingleEvent.class)
                .build();

        mRecyclerView = findViewById(R.id.organizerSellerDetailsEventsRecyclerView);

        mRecyclerView.setHasFixedSize(true);

        /// use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEvent, OrganizerSellerDetailsActivity.EventHolder>(options) {
            @Override
            public void onBindViewHolder(OrganizerSellerDetailsActivity.EventHolder holder, int position, SingleEvent model) {
                holder.title.setText(model.getTitle());
                holder.location.setText(model.getLocation());
                holder.date.setText(model.getDate());

                if (model.getAssigned() != 0) {
                    String stringAssigned = String.valueOf(model.getAssigned());
                    holder.assigned.setText("Boletos asignados: " + stringAssigned);
                } else {
                    holder.assigned.setText("SIN BOLETOS");
                }

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getApplicationContext())
                            .load(model.getImage())
                            .into(holder.image);
                }

                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add("cobrar").setOnMenuItemClickListener(item -> {
                            DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                            Log.d(TAG, "onMenuItemClick: on context menu cobrar click:" + model.getTitle());
                            Intent intent = new Intent(getApplicationContext(), OrganizerCobrarEventoActivity.class);
                            intent.putExtra("UID", uid);
                            intent.putExtra("EVENT_UID", doc.getId());
                            startActivityForResult(intent, COBRAR_EVENTO_REQUEST);
                            return true;
                        });
                    }
                });
            }

            @Override
            public OrganizerSellerDetailsActivity.EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_event_view, group, false);
                return new OrganizerSellerDetailsActivity.EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void init() {
        nameTextView = findViewById(R.id.organizerSellerDetailsName);
        addressTextView = findViewById(R.id.organizerSellerDetailsAddress);
        phoneTextView = findViewById(R.id.organizerSellerDetailsPhone);
        emailTextView = findViewById(R.id.organizerSellerDetailsEmail);
        sellerImageView = findViewById(R.id.organizerSellerDetailsImage);
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

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("UID");
        setupRecyclerView(uid);


        final DocumentReference docRef = db.document("sellers/" + uid);
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

    private void updateUI(Map<String, Object> data) {

        if (data.get("name") != null) {
            nameTextView.setText(data.get("name").toString());
        }

        if (data.get("address") != null) {
            addressTextView.setText(data.get("address").toString());
        }

        if (data.get("phone") != null) {
            phoneTextView.setText(data.get("phone").toString());
        }

        if (data.get("email") != null) {
            emailTextView.setText(data.get("email").toString());
        }

        if (data.get("image") != null) {
            Glide.with(getApplicationContext())
                    .load(data.get("image").toString())
                    .into(sellerImageView);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASSIGN_EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(R.id.organizerSellerDetailsContainer), "El evento a sido agregado.", Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == COBRAR_EVENTO_REQUEST) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(R.id.organizerSellerDetailsContainer), "El evento a sido cobrado.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void onOrganizerSellerDetailsFabClick(View view) {
        Intent intent = new Intent(this, SellerAssignEventActivity.class);
        intent.putExtra("uid", uid);
        startActivityForResult(intent, ASSIGN_EVENT_REQUEST);
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, assigned;
        ImageView image;

        public EventHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitleTextView);
            location = itemView.findViewById(R.id.eventLocationTextView);
            image = itemView.findViewById(R.id.eventImageView);
            date = itemView.findViewById(R.id.eventDateTextView);
            assigned = itemView.findViewById(R.id.eventDetailsTextView);
        }
    }
}
