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
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class OrganizerSingleEntryDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerEntryDetails";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int ENTRY_ASSIGN_EVENT_REQUEST = 53;

    private TextView nameTextView, phoneTextView, emailTextView;
    private ImageView imageView;
    private String uid;

    private Query query;
    private FirestoreRecyclerOptions<SingleEntryEvent> options;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter mAdapter;
    private TextView emptyEventsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_single_entry_details);

        Toolbar myToolbar = findViewById(R.id.organizerEntryDetailsToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        init();
        setupEventListeners(getIntent().getExtras());
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "entry");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }


    private void setupRecyclerView(String uid) {
        query = db.collection("accessControl/" + uid + "/events");

        setupEventsListener(query);

        // Configure recycler adapter options:
        options = new FirestoreRecyclerOptions.Builder<SingleEntryEvent>()
                .setQuery(query, SingleEntryEvent.class)
                .build();



        mRecyclerView = findViewById(R.id.organizerEntryRecyclerView);

        mRecyclerView.setHasFixedSize(true);

        /// use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEntryEvent, OrganizerSingleEntryDetailsActivity.EventHolder>(options) {
            @Override
            public void onBindViewHolder(OrganizerSingleEntryDetailsActivity.EventHolder holder, int position, SingleEntryEvent model) {
                holder.title.setText(model.getTitle());
                holder.location.setText(model.getAddress());
                holder.date.setText(model.getDate());
                holder.activeSwitch.setChecked(!model.isActive());

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getApplicationContext())
                            .load(model.getImage())
                            .into(holder.image);
                }
            }

            @Override
            public OrganizerSingleEntryDetailsActivity.EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_entry_event_view, group, false);
                return new OrganizerSingleEntryDetailsActivity.EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupEventsListener(Query query) {
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    emptyEventsTextView.setVisibility(View.VISIBLE);
                    return;
                }

                if (snapshot != null && !snapshot.isEmpty()) {
                    Log.d(TAG, "Current data: " + snapshot.size());
                    emptyEventsTextView.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "Current data: null");
                    emptyEventsTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void init() {
        nameTextView = findViewById(R.id.organizerEntryNameTextView);
        phoneTextView = findViewById(R.id.organizerEntryPhoneTextView);
        emailTextView = findViewById(R.id.organizerEntryEmailTextView);
        imageView = findViewById(R.id.organizerEntryDetailsImage);

        emptyEventsTextView = findViewById(R.id.organizerEntryDetailsEmptyText);
    }

    private void setupEventListeners(Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "setEventDetails: Bundle is null!");
            return;
        }

        uid = bundle.getString("entryUID");
        setupRecyclerView(uid);


        final DocumentReference docRef = db.document("accessControl/" + uid);
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

        if (data.get("phone") != null) {
            phoneTextView.setText(data.get("phone").toString());
        }

        if (data.get("email") != null) {
            emailTextView.setText(data.get("email").toString());
        }

        if (data.get("image") != null) {
            Glide.with(getApplicationContext())
                    .load(data.get("image").toString())
                    .into(imageView);
        }


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

    public void onAssignEventToEntryClick(View view) {
    }


    public class EventHolder extends RecyclerView.ViewHolder {
        TextView title, location, date;
        ImageView image;
        Switch activeSwitch;

        public EventHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.singleEntryEventTitle);
            location = itemView.findViewById(R.id.singleEntryEventAddress);
            image = itemView.findViewById(R.id.singleEntryEventImage);
            date = itemView.findViewById(R.id.singleEntryEventDate);
            activeSwitch = itemView.findViewById(R.id.singleEntryEventSwitch);
        }
    }
}
