package com.tarcrsd.letsgo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.android.libraries.places.internal.e;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarcrsd.letsgo.Models.EventAttendees;

import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.EventLog;
import android.view.View;

import com.tarcrsd.letsgo.Adapters.EventAdapter;
import com.tarcrsd.letsgo.Models.Events;

import java.util.ArrayList;


public class EventHistory extends AppCompatActivity {
    private String eventID;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private ArrayList<Events> mEventsData;
    private EventAdapter mAdapter;

    public EventHistory(){db = FirebaseFirestore.getInstance();}

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHistoryRecycleView();
        getEventAttendees();
    }

    private void initHistoryRecycleView() {
        mRecyclerView = findViewById(R.id.recycleViewHistory);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext() , 1));
        mEventsData = new ArrayList<>();
        mAdapter = new EventAdapter(getApplicationContext() ,mEventsData);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getEventAttendees() {
        db.collection("eventAttendees")
                .whereEqualTo("userUID", db.document("/users/" + mAuth.getUid()))
                .whereEqualTo("status", 2)
                .addSnapshotListener(new EventListener<QuerySnapshot>(){
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        getAttendedEvents(value);

                    }
        });

    }

    private void getAttendedEvents(QuerySnapshot value) {
        for(QueryDocumentSnapshot document : value) {
            mEventsData.clear();
            document.toObject(EventAttendees.class)
                    .getEventID()
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException e) {
                            mEventsData.add(value.toObject(Events.class ));
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }

    }



}
