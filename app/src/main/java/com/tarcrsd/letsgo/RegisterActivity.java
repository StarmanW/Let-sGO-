package com.tarcrsd.letsgo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tarcrsd.letsgo.Models.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,OnSuccessListener<Void> {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        initUI();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
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
     * Save Profile button click event handler
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (isValidData()) {
            String name = txtName.getText().toString();
            String contact = txtContact.getText().toString();
            String address = txtAddress.getText().toString();

            // Create new user object
            User newUser = new User(mAuth.getUid(), name, contact, address, "/something");

            // Save new user to firebase
            db.collection("users")
                    .document(newUser.getUserUID())
                    .set(newUser)
                    .addOnSuccessListener(this);
        }
    }

    /**
     * Data field validation
     * @return
     */
    private boolean isValidData() {
        String name = txtName.getText().toString();
        String contact = txtContact.getText().toString();
        String address = txtAddress.getText().toString();
        boolean isValidData = true;

        if (!name.matches("^[A-z\\-\\/ ]+$")) {
            txtErrName.setText(getString(R.string.txtErrName));
            isValidData = false;
        } else {
            txtErrName.setText("");
        }

        if (!contact.matches("^[0-9\\-\\+]+$")) {
            txtErrContact.setText(getString(R.string.txtErrContact));
            isValidData = false;
        } else {
            txtErrName.setText("");
        }

        if (!address.matches("^[A-z0-9\\@\\-\\,\\.\\;\\']+$")) {
            txtErrAddress.setText(getString(R.string.txtErrAddress));
            isValidData = false;
        } else {
            txtErrName.setText("");
        }

        return isValidData;
    }

    @Override
    public void onSuccess(Void aVoid) {
        finish();
    }
}
