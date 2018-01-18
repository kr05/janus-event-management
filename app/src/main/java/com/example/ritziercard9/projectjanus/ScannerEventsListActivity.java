package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class ScannerEventsListActivity extends AppCompatActivity {

    private static final String TAG = "ScannerEventsList";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mAdapter;
    private String entryUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Starting ScannerEventsList onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_events_list);

        Toolbar myToolbar = findViewById(R.id.scannerEventsListToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        String queryString = null;

        if (getIntent().getExtras() != null) {
            entryUID = getIntent().getStringExtra("entryUID");
            queryString = "accessControl/" + entryUID + "/events";
        }

        Log.d(TAG, "query string:" + queryString);

        // TODO: 1/17/2018 protect against null query string
        Query query = db.collection(queryString).whereEqualTo("active", true).orderBy("title");

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<SingleEntryEvent> options = new FirestoreRecyclerOptions.Builder<SingleEntryEvent>()
                .setQuery(query, SingleEntryEvent.class)
                .build();

        mRecyclerView = findViewById(R.id.scannerEventsListRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEntryEvent, ScannerEventsListActivity.EventHolder>(options) {
            @Override
            public void onBindViewHolder(ScannerEventsListActivity.EventHolder holder, int position, SingleEntryEvent model) {
                holder.title.setText(model.getTitle());
                holder.location.setText(model.getLocation());
                holder.details.setText(model.getDetails());
                holder.date.setText(model.getDate());

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getApplicationContext())
                            .load(model.getImage())
                            .into(holder.image);
                }

                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                    Intent intent = new Intent(getApplicationContext(), ScannerEventDetailsActivity.class);
                    intent.putExtra("UID", doc.getId());
                    intent.putExtra("entryUID", entryUID);
                    startActivity(intent);
                });
            }

            @Override
            public ScannerEventsListActivity.EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_event_view, group, false);

                return new ScannerEventsListActivity.EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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

    public class EventHolder extends RecyclerView.ViewHolder {
        TextView title, location, details, date;
        ImageView image;

        public EventHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitleTextView);
            location = itemView.findViewById(R.id.eventLocationTextView);
            details = itemView.findViewById(R.id.eventDetailsTextView);
            image = itemView.findViewById(R.id.eventImageView);
            date = itemView.findViewById(R.id.eventDateTextView);
        }
    }
}
