package com.tarcrsd.letsgo;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tarcrsd.letsgo.Models.User;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String EVENT_IMG_STORAGE_PATH = "profileImg/";

    // Event image upload
    private Uri fileUri;
    private static final int EVENT_IMG_REQUEST = 1;
    private String profileImgPath;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    // UI components
    private Button btnLogout;
    private TextView txtName;
    private TextView txtContact;
    private TextView txtAddress;
    private ImageView profileImageView;

    // Variables
    private boolean isEditing;
    private final User[] user = {null};

    // Constructor
    public ProfileFragment() {
        // Get the current logged-in user
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * onViewCreated event handler
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {

        //get current user
        db.collection("users")
            .document(mAuth.getUid())
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    user[0] = task.getResult().toObject(User.class);

                    //set text fields with user details
                    txtName = getActivity().findViewById(R.id.txtName);
                    txtName.setText(user[0].getName());

                    txtContact = getActivity().findViewById(R.id.txtContact);
                    txtContact.setText(user[0].getContact());

                    txtAddress = getActivity().findViewById(R.id.txtAddress);
                    txtAddress.setText(user[0].getAddress());

                    profileImgPath = user[0].getProfileImg();
                }
            });

        btnLogout = getActivity().findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
    }

    /**
     * Buttons onClick event handler
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout:
                logout(v);
                break;
            case R.id.btnEditProfile:
                updateDetails();
                break;
            case R.id.profileImg:
                updateImage();
                break;
            case R.id.btnPreviousEvents:
//                Intent previousEventIntent = new Intent(getContext(), PreviousEvent.class);
//                startActivity(previousEventIntent);
                break;
        }
    }

    private void updateDetails() {
        if (!isEditing) {
            //if not editing
            txtAddress.setEnabled(true);
            txtContact.setEnabled(true);
            txtName.setEnabled(true);
        } else {
            User updatedUser = new User(
                    mAuth.getUid(), txtName.getText().toString(), txtContact.getText().toString(),
                    txtAddress.getText().toString(), profileImgPath
            );

            //set updated user details
            db.document("/users/" + mAuth.getUid())
                    .set(updatedUser, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isEditing = false;
                            txtAddress.setEnabled(false);
                            txtContact.setEnabled(false);
                            txtName.setEnabled(false);

                            Toast.makeText(getContext(), "User details updated!", Toast.LENGTH_LONG).show();
                        }
                    });
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
     * Called once user has completed
     * select image activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if any image is selected
        if (requestCode == EVENT_IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the image
            fileUri = data.getData();
            try {
                // Convert selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), fileUri);

                // Setting up bitmap selected image into ImageView.
                profileImageView.setImageBitmap(bitmap);

                // Upload image to firebase storage
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload image to firebase storage
     */
    private void uploadImageToFirebaseStorage() {
        // Checking whether fileUri is empty or not.
        if (fileUri != null) {
            final FrameLayout uploadImgOverlay = getActivity().findViewById(R.id.progressBarHolder);
            final StorageReference profileImgRef = mStorageRef.child(EVENT_IMG_STORAGE_PATH + System.currentTimeMillis() + "." + getFileExtension(fileUri));
            // Adding addOnSuccessListener to second StorageReference.
            profileImgRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Remove loading screen
                            uploadImgOverlay.setVisibility(View.INVISIBLE);

                            // Get the firebase image path
                            profileImgPath = profileImgRef.getPath();
                            db.document("/events/" + user[0].getUserUID())
                                    .update("image", profileImageView)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @SuppressLint("RestrictedApi")
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "Profile image updated!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
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
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Logout user
     * @param view
     */
    public void logout(View view) {
        mAuth.signOut();
        getActivity().finish();
    }
}
