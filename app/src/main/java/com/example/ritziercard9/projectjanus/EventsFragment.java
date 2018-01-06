package com.example.ritziercard9.projectjanus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
 * Activities that contain this fragment must implement the
 * {@link EventsFragment.OnEventsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment {
    private OnEventsFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mAdapter;

    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
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
        View v = inflater.inflate(R.layout.fragment_events, container, false);

        Query query = db.collection("events").orderBy("title");

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<SingleEvent> options = new FirestoreRecyclerOptions.Builder<SingleEvent>()
                .setQuery(query, SingleEvent.class)
                .build();

        mRecyclerView = v.findViewById(R.id.fragmentEventsRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleEvent, EventsFragment.EventHolder>(options) {
            @Override
            public void onBindViewHolder(EventsFragment.EventHolder holder, int position, SingleEvent model) {
                holder.title.setText(model.getTitle());
                holder.location.setText(model.getLocation());
                holder.details.setText(model.getDetails());
                holder.date.setText(model.getDate());

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getActivity())
                            .load(model.getImage())
                            .into(holder.image);
                }



                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                    Intent intent = new Intent(getActivity(), OrganizerEventDetailsActivity.class);
                    intent.putExtra("UID", doc.getId());
                    startActivity(intent);
                });
            }

            @Override
            public EventsFragment.EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_event_view, group, false);

                return new EventsFragment.EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onEventsFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventsFragmentInteractionListener) {
            mListener = (OnEventsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        TextView title, location, details, date, capacity, sold;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnEventsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onEventsFragmentInteraction(Uri uri);
    }
}
