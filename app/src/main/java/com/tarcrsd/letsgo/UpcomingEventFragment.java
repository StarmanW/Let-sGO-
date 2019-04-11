package com.tarcrsd.letsgo;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tarcrsd.letsgo.Adapters.EventAdapter;
import com.tarcrsd.letsgo.Models.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingEventFragment extends Fragment implements LocationListener {

    // Firebase references
    private FirebaseFirestore db;

    private RecyclerView mRecyclerView;
    private ArrayList<Events> mEventsData;
    private EventAdapter mAdapter;
    private LocationManager locationManager;

    private Geocoder geocoder;

    public UpcomingEventFragment() {
        db = FirebaseFirestore.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upcoming_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        initEventRecycleView();
        initUserLocation();
    }

    /**
     * Initialize event recycle view
     */
    private void initEventRecycleView() {
        // Initialize the RecyclerView.
        mRecyclerView = getView().findViewById(R.id.recycleViewUpcoming);

        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        // Initialize the ArrayList that will contain the data.
        mEventsData = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new EventAdapter(getActivity(), mEventsData);
        mRecyclerView.setAdapter(mAdapter);
    }


    /**
     * Initialize user location
     */
    @SuppressLint("MissingPermission")
    private void initUserLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Request Location access permission
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 200, this);
        }
    }


    /**
     * Fired when permission is granted.
     * Usually happens during first time
     * launching the app.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isLocationPermissionGranted()) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 200, this);
            }
        }
    }

    /**
     * Determine if ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
     * permission is provided.
     *
     * @return
     */
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * LocationListener methods implementation
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() != 0) {
                displayEventList(addresses.get(0).getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayEventList(String locality) {
        if (locality != null) {
            db.collection("events")
                    .whereEqualTo("locality", locality)
                    .whereGreaterThanOrEqualTo("date", new Date())
                    .orderBy("date", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                            if (value != null) {
                                mEventsData.clear();
                                for (QueryDocumentSnapshot document : value) {
                                    mEventsData.add(document.toObject(Events.class));
                                }
                                // Notify the adapter of the change.
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        } else {
            db.collection("events")
                    .whereGreaterThanOrEqualTo("date", new Date())
                    .orderBy("date", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                            if (value != null) {
                                mEventsData.clear();
                                for (QueryDocumentSnapshot document : value) {
                                    mEventsData.add(document.toObject(Events.class));
                                }
                                // Notify the adapter of the change.
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
