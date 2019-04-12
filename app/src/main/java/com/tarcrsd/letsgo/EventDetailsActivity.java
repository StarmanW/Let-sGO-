package com.tarcrsd.letsgo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tarcrsd.letsgo.Models.EventAttendees;
import com.tarcrsd.letsgo.Models.EventOrganizer;
import com.tarcrsd.letsgo.Models.Events;
import com.tarcrsd.letsgo.Models.User;
import com.tarcrsd.letsgo.Module.DateFormatterModule;
import com.tarcrsd.letsgo.Module.DatePickerFragment;
import com.tarcrsd.letsgo.Module.GlideApp;
import com.tarcrsd.letsgo.Module.TimePickerFragment;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class EventDetailsActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // POJO object
    private Events event;
    private User user;

    // Boolean
    private boolean isOrganizer;
    private boolean isEditing = false;

    // CONSTANT
    private static final int EVENT_IMG_REQUEST = 10;
    private static final int PLACE_AUTOCOMPLETE_REQUEST = 11;
    private static final int ATTENDANCE_REQUEST = 12;
    private static final String EVENT_IMG_STORAGE_PATH = "eventsImg/";

    // Event image upload
    private Uri fileUri;
    private String eventImgPath;

    // Firebase references
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
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

    private TextView lblName;
    private TextView lblEventNameErr;
    private TextView lblLocationErr;
    private TextView lblDateErr;
    private TextView lblTimeErr;
    private TextView lblDescriptionErr;
    private Button btnOne;
    private Button btnTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize event ID
        event = new Events();
        if (getIntent().getExtras().getString("eventID") == null) {
            finish();
        }
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
        getSupportActionBar().setHomeButtonEnabled(true);

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

        lblName = findViewById(R.id.lblName);
        lblEventNameErr = findViewById(R.id.lblEventNameErr);
        lblLocationErr = findViewById(R.id.lblLocationErr);
        lblDateErr = findViewById(R.id.lblDateErr);
        lblTimeErr = findViewById(R.id.lblTimeErr);
        lblDescriptionErr = findViewById(R.id.lblDescriptionErr);
        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);

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
        try {
            // Load image into image view
            GlideApp.with(getApplicationContext())
                    .load(mStorageRef.child(event.getImage()))
                    .into(eventImgView);
        } catch (Exception ex) {
            eventImgView.setImageResource(R.drawable.event_bg);
        }

        // Set event data
        collapsingToolbar.setTitle(event.getName());
        eventImgPath = event.getImage();
        txtEventName.setText(event.getName());
        txtLocation.setText(event.getLocation());
        txtDate.setText(DateFormatterModule.formatDate(event.getDate()));
        txtTime.setText(DateFormatterModule.formatTime(event.getTime()));
        txtDescription.setText(event.getDescription());
        txtContact.setText(user.getContact());
        txtOrganizedBy.setText(user.getName());

        // Set button text dynamically
        if (mAuth.getCurrentUser().getUid().equals(user.getUserUID())) {
            btnOne.setText(getString(R.string.btnEditEventDetails));
            btnTwo.setText(getString(R.string.btnAttendance));
            isOrganizer = true;
        } else {
            isOrganizer = false;
            btnTwo.setVisibility(View.GONE);
            db.collection("eventAttendees/")
                    .whereEqualTo("userUID", db.document("users/" + mAuth.getUid()))
                    .whereEqualTo("eventID", db.document("events/" + event.getEventID()))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot document = task.getResult();
                                List<EventAttendees> eventAttendees = document.toObjects(EventAttendees.class);
                                if (eventAttendees.size() != 0) {
                                    if (new Date().after(eventAttendees.get(0).getEventDate())) {
                                        btnOne.setVisibility(View.GONE);
                                    }
                                    btnOne.setText(getString(R.string.btn_unattend_event));
                                } else {
                                    btnOne.setText(getString(R.string.btn_attend_event));
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOne:
                handleBtnOneClick();
                break;
            case R.id.btnTwo:
                handleBtnTwoClick();
                break;
            case R.id.eventImgView:
                updateImage();
                break;
            case R.id.txtLocation:
                updateLocation();
                break;
            case R.id.txtDate:
                showDatePicker(txtDate);
                break;
            case R.id.txtTime:
                showDatePicker(txtTime);
                break;
        }
    }

    private void handleBtnOneClick() {
        if (isOrganizer) {
            if (!isEditing) {
                lblName.setVisibility(View.VISIBLE);
                txtEventName.setVisibility(View.VISIBLE);
                txtEventName.setEnabled(true);
                txtLocation.setEnabled(true);
                txtDate.setEnabled(true);
                txtTime.setEnabled(true);
                txtDescription.setEnabled(true);
                btnOne.setText(getString(R.string.btnUpdateEventDetails));
                txtEventName.requestFocus();
                isEditing = true;
            } else {
                updateEventDetails();
            }
        } else {
            updateEventAttendees();
        }
    }

    private void handleBtnTwoClick() {
        if (isOrganizer) {
            Intent attendanceActivityIntend = new Intent(getApplicationContext(), AttendanceActivity.class);
            attendanceActivityIntend.putExtra("eventID", event.getEventID());
            startActivityForResult(attendanceActivityIntend, ATTENDANCE_REQUEST);
        }
    }

    /**
     * Handles the button click to create a new date picker fragment and
     * show it.
     *
     * @param view View that was clicked
     */
    public void showDatePicker(View view) {
        if (view.getId() == R.id.txtDate) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(),
                    getString(R.string.datepicker));
        } else if (view.getId() == R.id.txtTime) {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getSupportFragmentManager(), getString(R.string.timepicker));
        }
    }

    private void updateImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivityForResult(Intent.createChooser(photoPickerIntent, "Please select an image"), EVENT_IMG_REQUEST);
        } else {
            startActivityForResult(photoPickerIntent, EVENT_IMG_REQUEST);
        }
    }

    /**
     * Update event attendees
     */
    private void updateEventAttendees() {
        if (btnOne.getText().equals(getString(R.string.btn_attend_event))) {
            DocumentReference ref = db.collection("eventAttendees").document();
            EventAttendees eventAttendees = new EventAttendees(
                    ref.getId(),
                    db.document("/users/" + mAuth.getUid()),
                    db.document("/events/" + event.getEventID()),
                    event.getDate(),
                    1);

            ref.set(eventAttendees)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            btnOne.setText(getString(R.string.btn_unattend_event));
                            Toast.makeText(EventDetailsActivity.this, "You are attending this event.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (btnOne.getText().equals(getString(R.string.btn_unattend_event))) {
            db.collection("eventAttendees")
                    .whereEqualTo("userUID", db.document("users/" + mAuth.getUid()))
                    .whereEqualTo("eventID", db.document("events/" + event.getEventID()))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                List<EventAttendees> eventAttendees = querySnapshot.toObjects(EventAttendees.class);
                                db.document("eventAttendees/" + eventAttendees.get(0).getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                btnOne.setText(getString(R.string.btn_attend_event));
                                                Toast.makeText(EventDetailsActivity.this, "You have unattented this event.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    /**
     * Update event details
     */
    private void updateEventDetails() {
        try {
            if (isValidData()) {
                final Events updatedEvent = new Events(event.getEventID(),
                        txtEventName.getText().toString(),
                        txtDescription.getText().toString(),
                        eventImgPath,
                        DateFormatterModule.getDate(txtDate.getText().toString()),
                        DateFormatterModule.getTime(txtTime.getText().toString()),
                        txtLocation.getText().toString(),
                        event.getLocality());

                Log.i("EVENT ID", event.getEventID());
                Log.i("Eaaa", "1234");

                db.document("/events/" + event.getEventID())
                        .set(updatedEvent, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                collapsingToolbar.setTitle(txtEventName.getText());
                                lblName.setVisibility(View.GONE);
                                txtEventName.setVisibility(View.GONE);
                                txtLocation.setEnabled(false);
                                txtDate.setEnabled(false);
                                txtTime.setEnabled(false);
                                txtDescription.setEnabled(false);
                                btnOne.setText(getString(R.string.btnEditEventDetails));
                                isEditing = false;
                                Toast.makeText(getApplicationContext(), "Event details updated!", Toast.LENGTH_LONG).show();
                            }
                        });

                db.collection("eventAttendees")
                        .whereEqualTo("eventID", db.document("/events/" + updatedEvent.getEventID()))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<EventAttendees> eventAttendees = task.getResult().toObjects(EventAttendees.class);
                                    for (EventAttendees eventAttendee : eventAttendees) {
                                        eventAttendee.setEventDate(updatedEvent.getDate());
                                        db.document("eventAttendees/" + eventAttendee.getId())
                                                .set(eventAttendee, SetOptions.merge());
                                    }
                                }
                            }
                        });
            }
        } catch (ParseException ex) {
            Log.i("ERR Update Event", ex.getMessage());
        }
    }

    private void updateLocation() {
        if (isEditing) {
            final String API_KEY = getResources().getString(R.string.google_maps_key);

            /**
             * Initialize Places. For simplicity, the API key is hard-coded. In a production
             * environment we recommend using a secure mechanism to manage API keys.
             */
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), API_KEY);
            }

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG))
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST);
        }
    }

    /**
     * Called once user has completed
     * select image activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if any image is selected
        if (requestCode == EVENT_IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the image
            fileUri = data.getData();
            try {
                // Convert selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);

                // Setting up bitmap selected image into ImageView.
                eventImgView.setImageBitmap(bitmap);

                // Upload image to firebase storage
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place != null) {
                    // Update text view location
                    txtLocation.setText(place.getAddress());

                    // Update location & locality for event object
                    event.setLocation(place.getAddress());
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                        if (addresses.size() > 0) {
                            event.setLocality(addresses.get(0).getLocality());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, status.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ATTENDANCE_REQUEST) {
            if (resultCode == RESULT_OK) {
                event.setEventID(data.getStringExtra("eventID"));
            }
        }
    }

    /**
     * Upload image to firebase storage
     */
    public void uploadImageToFirebaseStorage() {
        // Checking whether fileUri is empty or not.
        if (fileUri != null) {
            final FrameLayout uploadImgOveray = findViewById(R.id.progressBarHolder);
            final StorageReference eventImgRef = mStorageRef.child(EVENT_IMG_STORAGE_PATH + System.currentTimeMillis() + "." + getFileExtension(fileUri));

            // Adding addOnSuccessListener to second StorageReference.
            eventImgRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Remove loading screen
                            uploadImgOveray.setVisibility(View.INVISIBLE);

                            // Get the firebase image path
                            eventImgPath = eventImgRef.getPath();
                            db.document("/events/" + event.getEventID())
                                    .update("image", eventImgPath)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EventDetailsActivity.this, "Event image updated!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(EventDetailsActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Display loading screen
                            uploadImgOveray.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    /**
     * Get image file extension
     *
     * @param uri
     * @return
     */
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    /**
     * Data field validation
     *
     * @return
     */
    private boolean isValidData() {
        String eventName = txtEventName.getText().toString();
        String description = txtDescription.getText().toString();
        String location = txtLocation.getText().toString();
        String time = txtTime.getText().toString();
        String date = txtDate.getText().toString();
        boolean isValidData = true;

        if (!eventName.matches("^[\\S\\s\\D\\d]+$")) {
            lblEventNameErr.setVisibility(View.VISIBLE);
            isValidData = false;
        } else {
            lblEventNameErr.setVisibility(View.GONE);
        }

        if (!description.matches("^[\\S\\s\\D\\d]+$")) {
            lblDescriptionErr.setVisibility(View.VISIBLE);
            isValidData = false;
        } else {
            lblDescriptionErr.setVisibility(View.GONE);
        }

        if (!location.matches("^[\\S\\s\\D\\d]+$")) {
            lblLocationErr.setVisibility(View.VISIBLE);
            isValidData = false;
        } else {
            lblLocationErr.setVisibility(View.GONE);
        }

        if (!date.matches("^[\\S\\s\\D\\d]+$")) {
            lblDateErr.setVisibility(View.VISIBLE);
            isValidData = false;
        } else {
            lblDateErr.setVisibility(View.GONE);
        }

        if (!time.matches("^[\\S\\s\\D\\d]+$")) {
            lblTimeErr.setVisibility(View.VISIBLE);
            isValidData = false;
        } else {
            lblTimeErr.setVisibility(View.GONE);
        }

        return isValidData;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        String month_string = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);

        txtDate.setText(day_string +
                "-" + month_string +
                "-" + year_string +
                " " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        txtTime.setText(String.format("%02d:%02d %s", cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), DateFormatterModule.getAMOrPM(cal.get(Calendar.AM_PM))));
    }
}
