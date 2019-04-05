package com.tarcrsd.letsgo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    // Firebase references
    private FirebaseAuth mAuth;

    // UI components
    private Button btnLogout;

    // Constructor
    public ProfileFragment() {
        // Get the current logged-in user
        mAuth = FirebaseAuth.getInstance();
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
        }
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
