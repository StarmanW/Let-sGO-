package com.tarcrsd.letsgo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarcrsd.letsgo.Adapters.AttendanceAdapter;
import com.tarcrsd.letsgo.Adapters.EventAdapter;
import com.tarcrsd.letsgo.Models.EventAttendees;
import com.tarcrsd.letsgo.Models.Events;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class AttendanceActivity extends AppCompatActivity {

    // Firebase Reference
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView mRecycleView;
    private ArrayList<EventAttendees> eventAttendees;
    private AttendanceAdapter mAdapter;

    // Event Data
    private String eventID;

    /**
     * onCreate method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        eventID = getIntent().getStringExtra("eventID");
        db = FirebaseFirestore.getInstance();
        initUI();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
        db.document("events/" + eventID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Events event = task.getResult().toObject(Events.class);
                        setTitle("Participants for " + event.getName());
                    }
                });

        // UI & variable setup
        eventAttendees = new ArrayList<>();
        mAdapter = new AttendanceAdapter(getApplicationContext(), eventAttendees);

        // Initialize UI components
        mRecycleView = findViewById(R.id.recycleViewAttendees);
        mRecycleView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        mRecycleView.setAdapter(mAdapter);

        // Fetch data
        displayAttendees();
    }

    /**
     * Display the list of attendees for
     * the selected event.
     */
    private void displayAttendees() {
        db.collection("eventAttendees")
                .whereEqualTo("eventID", db.document("events/" + eventID))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (value != null && !value.isEmpty()) {
                            eventAttendees.clear();
                            for (QueryDocumentSnapshot document : value) {
                                eventAttendees.add(document.toObject(EventAttendees.class));
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("eventID", eventID);
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
