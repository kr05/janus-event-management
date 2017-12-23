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
 * {@link SellersFragment.OnSellersFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SellersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SellersFragment extends Fragment {
    private OnSellersFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mAdapter;

    public SellersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SellersFragment.
     */
    public static SellersFragment newInstance() {
        SellersFragment fragment = new SellersFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
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
        View v = inflater.inflate(R.layout.fragment_sellers, container, false);

        Query query = db.collection("sellers").orderBy("name");

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<SingleSeller> options = new FirestoreRecyclerOptions.Builder<SingleSeller>()
                .setQuery(query, SingleSeller.class)
                .build();

        mRecyclerView = v.findViewById(R.id.fragmentSellersRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // specify an adapter
        mAdapter = new FirestoreRecyclerAdapter<SingleSeller, SellersFragment.SellerHolder>(options) {
            @Override
            public void onBindViewHolder(SellersFragment.SellerHolder holder, int position, SingleSeller model) {
                holder.name.setText(model.getName());
                holder.address.setText(model.getAddress());

                if (!TextUtils.isEmpty(model.getImage())) {
                    Glide.with(getActivity())
                            .load(model.getImage())
                            .into(holder.image);
                }

                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot doc = getSnapshots().getSnapshot(position);
                    Intent intent = new Intent(getActivity(), OrganizerSellerDetailsActivity.class);
                    intent.putExtra("UID", doc.getId());
                    startActivity(intent);
                });
            }

            @Override
            public SellersFragment.SellerHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_seller_view, group, false);

                return new SellersFragment.SellerHolder(view);
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
            mListener.onSellersFragmentInteraction(uri);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSellersFragmentInteractionListener) {
            mListener = (OnSellersFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnSellersFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSellersFragmentInteraction(Uri uri);
    }

    public class SellerHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        ImageView image;

        public SellerHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.sellerNameTextView);
            address = itemView.findViewById(R.id.sellerAddressTextView);
            image = itemView.findViewById(R.id.sellerImageView);

        }
    }
}
