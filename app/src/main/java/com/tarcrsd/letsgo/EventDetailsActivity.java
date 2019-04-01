package com.tarcrsd.letsgo;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tarcrsd.letsgo.Models.EventOrganizer;
import com.tarcrsd.letsgo.Models.Events;
import com.tarcrsd.letsgo.Models.User;
import com.tarcrsd.letsgo.Module.DateFormatterModule;
import com.tarcrsd.letsgo.Module.GlideApp;

import javax.annotation.Nullable;

public class EventDetailsActivity extends AppCompatActivity {

    // Firebase references
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    // UI Components
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView eventImgView;
    private EditText txtEventName;
    private EditText txtLocation;
    private EditText txtDate;
    private EditText txtTime;
    private EditText txtDescription;
    private EditText txtContact;
    private EditText txtOrganizedBy;
    private Button btnEditEventDetail;
    private Button btnViewAttendance;

    // POJO object
    private Events event;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize event ID
        event = new Events();
        event.setEventID(getIntent().getExtras().getString("eventID"));

        // Init UI
        initUI();
    }

    /**
     * Initialization of UI components
     */
    private void initUI() {
        // Replace the default toolbar with Collapsible Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializes views
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        eventImgView = findViewById(R.id.eventImgView);
        txtEventName = findViewById(R.id.txtEventName);
        txtLocation = findViewById(R.id.txtLocation);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtDescription = findViewById(R.id.txtDescription);
        txtContact = findViewById(R.id.txtContact);
        txtOrganizedBy = findViewById(R.id.txtOrganizedBy);
        btnEditEventDetail = findViewById(R.id.btnEditDetails);
        btnViewAttendance = findViewById(R.id.btnAttendance);

        // Get Event data
        getEventData();
    }

    /**
     * Retrieve event data based on ID
     */
    private void getEventData() {
        db.document("events/" + event.getEventID())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException e) {
                        event = value.toObject(Events.class);
                        getUserData();
                    }
                });
    }

    /**
     * Retrieve user data
     * Links from eventID > eventOrganizer table >
     * userID > users table
     */
    private void getUserData() {
        db.collection("eventOrganizer")
                .whereEqualTo("eventID", db.document("/events/" + event.getEventID()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        for (QueryDocumentSnapshot document : value) {
                            document.toObject(EventOrganizer.class)
                                    .getUserID()
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                            user = documentSnapshot.toObject(User.class);
                                            initEventData();
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Initialize views with data retrieved
     */
    private void initEventData() {
        GlideApp.with(getApplicationContext())
                .load(mStorageRef.child(event.getImage()))
                .into(eventImgView);

        collapsingToolbar.setTitle(event.getName());
        txtEventName.setText(event.getName());
        txtLocation.setText(event.getLocation());
        txtDate.setText(DateFormatterModule.getDate(event.getDate()));
        txtTime.setText(DateFormatterModule.getTime(event.getTime()));
        txtDescription.setText(event.getDescription());
        txtContact.setText(user.getContact());
        txtOrganizedBy.setText(user.getName());
    }
}
