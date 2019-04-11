package com.tarcrsd.letsgo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarcrsd.letsgo.Adapters.EventAdapter;
import com.tarcrsd.letsgo.Models.EventAttendees;
import com.tarcrsd.letsgo.Models.Events;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class PreviousEventActivity extends AppCompatActivity {

    // Firebase references
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI components & activity properties
    private RecyclerView mRecyclerView;
    private ArrayList<Events> mEventsData;
    private EventAdapter mAdapter;

    /**
     * onCreate method
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_event);

        // Get firebase instance
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initHistoryRecycleView();
        getEventAttendees();
    }

    /**
     * Initialize recycle view
     */
    private void initHistoryRecycleView() {
        setTitle("Previous Events");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mRecyclerView = findViewById(R.id.recycleViewHistory);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mEventsData = new ArrayList<>();
        mAdapter = new EventAdapter(this, mEventsData);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Get list of events the user has attended
     * before. NOTE: Compared using date.
     */
    private void getEventAttendees() {
        db.collection("eventAttendees")
                .whereEqualTo("userUID", db.document("/users/" + mAuth.getUid()))
                .whereEqualTo("status", 2)
                .whereLessThan("eventDate", new Date())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (value != null) {
                            getAttendedEvents(value);
                        }
                    }
                });
    }

    /**
     * Get the list of events associated with
     * each of the eventAttendees record
     *
     * @param value
     */
    private void getAttendedEvents(QuerySnapshot value) {
        for (QueryDocumentSnapshot document : value) {
            mEventsData.clear();
            document.toObject(EventAttendees.class)
                    .getEventID()
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                mEventsData.add(task.getResult().toObject(Events.class));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
