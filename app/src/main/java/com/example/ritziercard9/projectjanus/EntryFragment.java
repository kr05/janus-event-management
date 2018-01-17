package com.example.ritziercard9.projectjanus;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntryFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter mAdapter;

    public EntryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static EntryFragment newInstance() {
        EntryFragment fragment = new EntryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_entry, container, false);

        Query query = db.collection("accessControl").orderBy("name");

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<SingleEntry> options = new FirestoreRecyclerOptions.Builder<SingleEntry>()
                .setQuery(query, SingleEntry.class)
                .build();

        mRecyclerView = v.findViewById(R.id.fragmentEntryRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEntry, EntryFragment.EntryHolder>(options) {
            @Override
            public void onBindViewHolder(EntryFragment.EntryHolder holder, int position, SingleEntry model) {
                holder.name.setText(model.getName());
                holder.email.setText(model.getEmail());

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getActivity())
                            .load(model.getImage())
                            .into(holder.image);
                }

                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                    Intent intent = new Intent(getActivity(), OrganizerSingleEntryDetailsActivity.class);
                    intent.putExtra("entryUID", doc.getId());
                    startActivity(intent);
                });
            }

            @Override
            public EntryFragment.EntryHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_entry_view, group, false);

                return new EntryFragment.EntryHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        return v;
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

    public class EntryHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView image;

        public EntryHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entryNameTextView);
            email = itemView.findViewById(R.id.entryEmailTextView);
            image = itemView.findViewById(R.id.entryImageView);

        }
    }

}
