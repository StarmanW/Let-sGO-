package com.tarcrsd.letsgo;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarcrsd.letsgo.Adapters.EventAdapter;
import com.tarcrsd.letsgo.Models.Events;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttendingFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView mRecycleView;
    private ArrayList<Events> mEventsData;
    private EventAdapter mAdapter;

    public AttendingFragment() { db = FirebaseFirestore.getInstance();}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attending, container, false);



    }

    private void initEventRecycleView() {

        mRecycleView= getView().findViewById(R.id.eventRecycleView);

        mRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        mEventsData = new ArrayList<>();

        mAdapter = new EventAdapter(getActivity(), mEventsData);
        mRecycleView.setAdapter(mAdapter);
    }

    private void displayAttendingEvent(){
        db.collection("eventAttendees")
                .whereEqualTo("userId", mAuth.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>(){
                @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e){
                    mEventsData.clear();
                    for(QueryDocumentSnapshot document : value){
                        mEventsData.add(document.toObject(Events.class));

                    }
                    mAdapter.notifyDataSetChanged();
                }
        });

    }


}
