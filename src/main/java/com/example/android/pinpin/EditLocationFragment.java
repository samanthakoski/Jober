package com.example.android.pinpin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class EditLocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "EditLocationFragment";
    private final int PERM_REQ_ACC_FINE_LOC = 1;
    private String BACK_STACK_TAG;
    private MyApplication app;
    private MapsActivity activity;
    private RequestFragment frag;
    private Necessity_Model necessity_model;
    private Job_Model job_model;
    private boolean locPermGranted;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocProviderClient;
    private Location lastKnownLoc;
    private CameraPosition camPos;
    private ImageButton backBtn;
    private AppCompatButton selectLocBtn;
    private LatLng curLoc;

    public static EditLocationFragment newInstance(Job_Model job, String BACK_STACK_TAG) {
        EditLocationFragment fragment = new EditLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("JOB", job);
        bundle.putSerializable("BACKTAG", BACK_STACK_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static EditLocationFragment newInstance(Necessity_Model nec, String BACK_STACK_TAG) {
        EditLocationFragment fragment = new EditLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("NEC", nec);
        bundle.putSerializable("BACKTAG", BACK_STACK_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        activity = (MapsActivity) getActivity();
        fusedLocProviderClient = activity.getmFusedLocClient();
        app = MyApplication.getMyApp();
        BACK_STACK_TAG = (String) getArguments().getSerializable("BACKTAG");
        activity = (MapsActivity) getActivity();
        curLoc = app.getDefaultLoc();
        frag = (RequestFragment) activity.getFragManager().findFragmentByTag("requestFrag");
        necessity_model = (Necessity_Model) getArguments().getSerializable("NEC");
        if (necessity_model == null) {
            job_model = (Job_Model) getArguments().getSerializable("JOB");
        }
        if (savedInstanceState != null) {
            lastKnownLoc = savedInstanceState.getParcelable(app.KEY_LOCATION);
            camPos = savedInstanceState.getParcelable(app.KEY_CAMERA_POSITION);
        }
        return inflater.inflate(
                R.layout.set_location,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.set_loc_map);
        mapFragment.getMapAsync(this);

        backBtn = view.findViewById(R.id.set_loc_back_button);
        selectLocBtn = view.findViewById(R.id.select_loc);
        curLoc = (necessity_model == null) ? job_model.location : necessity_model.location;
        Log.i(TAG, "curLoc before reset: " + curLoc.latitude + ", " + curLoc.longitude);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        selectLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Set pin and pass info back to request job fragment
                frag.setLocation(curLoc);
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "drag started");
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onMarkerDragEnd: "+arg0.getPosition().latitude+", "+arg0.getPosition().longitude);
                curLoc = arg0.getPosition();
                map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.i(TAG, "marker dragged");
            }
        });
        setPin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        locPermGranted = false;
        switch (requestCode) {
            case PERM_REQ_ACC_FINE_LOC: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location permission granted");
                    locPermGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat
                .checkSelfPermission(
                        app.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location permission granted");
            locPermGranted = true;
        } else {
            Log.i(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERM_REQ_ACC_FINE_LOC);
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locPermGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                Log.i(TAG, "in update loc uI cam pos: " + map.getCameraPosition());
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                locPermGranted = false;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    private void getDeviceLocation() {
        try {
            if (locPermGranted) {
                Task locResult = fusedLocProviderClient.getLastLocation();
                locResult.addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLoc = task.getResult();
                            Log.i(TAG, "lastKnownLoc: " + task.getResult());
                            if (lastKnownLoc == null) {
                                setDefaultLoc();
                                return;
                            }
                            /*
                            app.setLastKnownLoc(lastKnownLoc);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLoc.getLatitude(),
                                            lastKnownLoc.getLongitude()), app.getDefaultZoom()));
                                            */
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    app.getDefaultLoc(), app.getDefaultZoom()));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            setDefaultLoc();
                        }
                    }});
                // TODO: remove below
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(app.getDefaultLoc()).zoom(17).build();
                map.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: %s" + e.getMessage());
        }
    }

    private void setDefaultLoc() {
        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(app.getDefaultLoc(), app.getDefaultZoom()));
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void setPin() {
        Drawable drawable = null;
        switch (frag.getType()) {
            case "Manual Labor":
                drawable = activity.getDrawable(R.drawable.marker_manual);
                break;
            case "Virtual":
                drawable = activity.getDrawable(R.drawable.marker_virtual);
                break;
            case "Task":
                drawable = activity.getDrawable(R.drawable.marker_task);
                break;
            case "Errand":
                drawable = activity.getDrawable(R.drawable.marker_errand);
                break;
            case "Food":
                drawable = activity.getDrawable(R.drawable.marker_food);
                break;
            case "FirstAid":
                drawable = activity.getDrawable(R.drawable.marker_firstaid);
                break;
            case "Money":
                drawable = activity.getDrawable(R.drawable.marker_money);
                break;
            case "Ride":
                drawable = activity.getDrawable(R.drawable.marker_ride);
                break;
        }
        if (drawable != null) {
            BitmapDescriptor icon = activity.getMarkerIconFromDrawable(drawable);
            map.addMarker(new MarkerOptions()
                    .position(curLoc)
                    .icon(icon)).setDraggable(true);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(app.getDefaultLoc()).zoom(17).build();
            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            Log.i(TAG, "After moving camera after setting pin: " + map.getCameraPosition());
        }
    }

}
