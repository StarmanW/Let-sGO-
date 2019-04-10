package com.tarcrsd.letsgo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    // REQUEST CODE CONSTANTS = For determining the response
    // returned from a specific activity
    private static final int RC_SIGN_OUT = 0;
    private static final int RC_SIGN_IN = 1;
    private static final int RC_NEWUSER_REGISTER = 2;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseApp, Firestore and FirebaseAuth
        FirebaseApp.initializeApp(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // If user is not signed in
        if (mAuth.getCurrentUser() != null) {
            Intent mainActivityIntend = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntend);
            finish();
        }
        initGoogleSignIn();
    }

    /**
     * Initialize google sign in
     */
    private void initGoogleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        // Set sign-in button click event handler
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
    }

    /**
     * Sign in button click
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signInUser();
                break;
        }
    }

    /**
     * Method to display sign in form and perform sign in operation
     */
    private void signInUser() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Result returned after sign in activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("GOOGLE SIGN IN FAILED", "Google sign in failed", e);
            }
        } else if (requestCode == RC_NEWUSER_REGISTER) {
            Intent mainActivityIntend = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntend);
            finish();
        }
    }

    /**
     * Performs google account authentication with firebase
     *
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get the current logged un user ID to determine
                            // if current user is a new user by checking with
                            // the "users" collection in firestore.
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            db.collection("users/")
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (!task.getResult().exists()) {
                                                Intent registerNewUserIntend = new Intent(getApplicationContext(), RegisterActivity.class);
                                                startActivityForResult(registerNewUserIntend, RC_NEWUSER_REGISTER);
                                            } else {
                                                Intent mainActivityIntend = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(mainActivityIntend);
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
