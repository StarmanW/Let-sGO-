package com.tarcrsd.letsgo;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tarcrsd.letsgo.Models.User;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Components
    private ImageView profileImg;
    private EditText txtName;
    private EditText txtContact;
    private EditText txtAddress;
    private TextView txtErrName;
    private TextView txtErrContact;
    private TextView txtErrAddress;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    // Profile image upload
    private Uri fileUri;
    private String profileImgPath;

    // CONSTANT
    private static final int PROFILE_IMG_REQUEST = 12;
    private static final String PROFILE_IMG_STORAGE_PATH = "profileImg/";

    /**
     * On Create method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        initUI();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
        setTitle("Register New Profile");
        txtName = findViewById(R.id.txtName);
        txtContact = findViewById(R.id.txtContact);
        txtAddress = findViewById(R.id.txtAddress);
        txtErrName = findViewById(R.id.txtErrName);
        txtErrContact = findViewById(R.id.txtErrContact);
        txtErrAddress = findViewById(R.id.txtErrAddress);
        profileImg = findViewById(R.id.profileImg);

        // Set default image for image view
        profileImg.setImageResource(R.drawable.upload_img_placeholder);
    }

    /**
     * Click event handler
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profileImg) {
            selectProfileImage();
        } else if (v.getId() == R.id.btnSaveProfile) {
            registerNewUser();
        }
    }

    /**
     * Performs upload image operation
     */
    private void selectProfileImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivityForResult(Intent.createChooser(photoPickerIntent, "Please select an image"), PROFILE_IMG_REQUEST);
        } else {
            startActivityForResult(photoPickerIntent, PROFILE_IMG_REQUEST);
        }
    }

    /**
     * Performs registration of new user
     */
    private void registerNewUser() {
        if (isValidData()) {
            String name = txtName.getText().toString();
            String contact = txtContact.getText().toString();
            String address = txtAddress.getText().toString();

            // Create new user object
            User newUser = new User(mAuth.getUid(), name, contact, address, profileImgPath);

            // Save new user to firebase
            db.document("users/" + newUser.getUserUID())
                    .set(newUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    });
        }
    }

    /**
     * Data field validation
     *
     * @return
     */
    private boolean isValidData() {
        String name = txtName.getText().toString();
        String contact = txtContact.getText().toString();
        String address = txtAddress.getText().toString();
        boolean isValidData = true;

        if (!name.matches("^[A-z\\-\\/ ]+$")) {
            txtErrName.setVisibility(View.VISIBLE);
            txtErrName.setText(getString(R.string.txtErrName));
            isValidData = false;
        } else {
            txtErrName.setVisibility(View.GONE);
            txtErrName.setText("");
        }

        if (!contact.matches("^[0-9\\-+]+$")) {
            txtErrContact.setVisibility(View.VISIBLE);
            txtErrContact.setText(getString(R.string.txtErrContact));
            isValidData = false;
        } else {
            txtErrContact.setVisibility(View.GONE);
            txtErrContact.setText("");
        }

        if (!address.matches("^[A-z0-9@\\-,.;' ]+$")) {
            txtErrAddress.setVisibility(View.VISIBLE);
            txtErrAddress.setText(getString(R.string.txtErrAddress));
            isValidData = false;
        } else {
            txtErrAddress.setVisibility(View.GONE);
            txtErrAddress.setText("");
        }

        return isValidData;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if any image is selected
        if (requestCode == PROFILE_IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the image
            fileUri = data.getData();
            try {
                // Convert selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);

                // Setting up bitmap selected image into ImageView.
                profileImg.setImageBitmap(bitmap);

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
    public void uploadImageToFirebaseStorage() {
        // Checking whether fileUri is empty or not.
        if (fileUri != null) {
            final FrameLayout uploadImgOveray = findViewById(R.id.progressBarHolder);
            final StorageReference profileImgRef = mStorageRef.child(PROFILE_IMG_STORAGE_PATH + System.currentTimeMillis() + "." + getFileExtension(fileUri));

            // Adding addOnSuccessListener to second StorageReference.
            profileImgRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Remove loading screen
                            uploadImgOveray.setVisibility(View.INVISIBLE);

                            // Get the firebase image path
                            profileImgPath = profileImgRef.getPath();
                            Toast.makeText(RegisterActivity.this, "Profile image has been successfully uploaded", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(RegisterActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
}
