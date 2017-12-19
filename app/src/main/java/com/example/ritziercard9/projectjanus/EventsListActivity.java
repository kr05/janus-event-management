package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class EventsListActivity extends AppCompatActivity {

    private static final String TAG = "EventsListActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        Toolbar myToolbar = findViewById(R.id.eventsListToolbar);

        //myToolbar.setTitle("Detalles del evento");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Query query = db.collection("events").orderBy("title");

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<SingleEvent> options = new FirestoreRecyclerOptions.Builder<SingleEvent>()
                .setQuery(query, SingleEvent.class)
                .build();

        mRecyclerView = findViewById(R.id.eventsListRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEvent, EventHolder>(options) {
            @Override
            public void onBindViewHolder(EventHolder holder, int position, SingleEvent model) {
                holder.title.setText(model.getTitle());
                holder.location.setText(model.getLocation());
                holder.details.setText(model.getDetails());
                holder.date.setText(model.getDate());
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .into(holder.image);

                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                    Intent intent = new Intent(getApplicationContext(), EventDetails.class);
                    intent.putExtra("UID", doc.getId());
                    startActivity(intent);
                });
            }

            @Override
            public EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_event_view, group, false);

                return new EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
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
