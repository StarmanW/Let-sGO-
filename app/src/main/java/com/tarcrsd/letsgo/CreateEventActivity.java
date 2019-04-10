package com.tarcrsd.letsgo;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tarcrsd.letsgo.Models.EventOrganizer;
import com.tarcrsd.letsgo.Models.Events;
import com.tarcrsd.letsgo.Models.User;
import com.tarcrsd.letsgo.Module.DateFormatterModule;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener{

    // POJO object
    private User user;
    private Events newEvent = new Events();

    // CONSTANT
    private static final int EVENT_IMG_REQUEST = 10;
    private static final int PLACE_AUTOCOMPLETE_REQUEST = 11;
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
    private LinearLayout editNameLayout;
    private EditText txtEventName;
    private EditText txtLocation;
    private EditText txtDate;
    private EditText txtTime;
    private EditText txtDescription;
    private Button btnOne;
    private String api_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        InitUI();
    }

    private void InitUI() {
        // Initializes views
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        eventImgView = findViewById(R.id.eventImgView);
        editNameLayout = findViewById(R.id.editNameLayout);
        txtEventName = findViewById(R.id.txtEventName);
        txtLocation = findViewById(R.id.txtLocation);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtDescription = findViewById(R.id.txtDescription);
        btnOne = findViewById(R.id.btnOne);
    }

    /**
     * Retrieve user data
     * Links from eventID > eventOrganizer table >
     * userID > users table
     */
    private void getUserData() {
        db.collection("eventOrganizer")
                .whereEqualTo("eventID", db.document("/events/" + newEvent.getEventID()))
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
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Buttons onClick event handler
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOne:
                handleBtnOneClick();
                break;
            case R.id.eventImgView:
                updateImage();
                break;
            case R.id.txtLocation:
                updateLocation();
                break;
        }
    }

    private void handleBtnOneClick() {
            updateEventDetails();
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
     * Update event details
     */
    private void updateEventDetails() {
        try {
                    newEvent.setEventID(db.collection("events").document().getId());
                    newEvent.setName(txtEventName.getText().toString());
                    newEvent.setDescription(txtDescription.getText().toString());
                    newEvent.setImage(eventImgPath);
                    newEvent.setDate(DateFormatterModule.getDate(txtDate.getText().toString()));
                    newEvent.setTime(DateFormatterModule.getTime(txtTime.getText().toString()));
                    newEvent.setLocation(txtLocation.getText().toString());

            db.document("/events/" + newEvent.getEventID())
                    .set(newEvent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            collapsingToolbar.setTitle(txtEventName.getText());
                            Toast.makeText(getApplicationContext(), "New event added!", Toast.LENGTH_LONG).show();

                            Intent toEventDetail = new Intent(getApplicationContext(), EventDetailsActivity.class);
                            toEventDetail.putExtra("eventID", newEvent.getEventID());
                            startActivity(toEventDetail);
                        }
                    });
        } catch (ParseException ex) {
            Log.i("ERR add new Event", ex.getMessage());
        }
    }

    private void updateLocation() {
            final String api_key = getResources().getString(R.string.google_maps_key);

            /**
             * Initialize Places. For simplicity, the API key is hard-coded. In a production
             * environment we recommend using a secure mechanism to manage API keys.
             */
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), api_key);
            }

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG))
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST);

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
                    newEvent.setLocation(place.getAddress());
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                        if (addresses.size() > 0) {
                            newEvent.setLocality(addresses.get(0).getLocality());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, status.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Upload image to firebase storage
     */
    public void uploadImageToFirebaseStorage() {
        // Checking whether fileUri is empty or not.
        if (fileUri != null) {
            final FrameLayout uploadImgOverlay = findViewById(R.id.progressBarHolder);
            final StorageReference eventImgRef = mStorageRef.child(EVENT_IMG_STORAGE_PATH + System.currentTimeMillis() + "." + getFileExtension(fileUri));

            // Adding addOnSuccessListener to second StorageReference.
            eventImgRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Remove loading screen
                            uploadImgOverlay.setVisibility(View.INVISIBLE);

                            // Get the firebase image path
                            eventImgPath = eventImgRef.getPath();
                            db.document("/events/" + newEvent.getEventID())
                                    .update("image", eventImgPath)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CreateEventActivity.this, "Event image added!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(CreateEventActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Display loading screen
                            uploadImgOverlay.setVisibility(View.VISIBLE);
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
}
