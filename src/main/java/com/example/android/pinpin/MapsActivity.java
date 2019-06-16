package com.example.android.pinpin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocClient;
    private LocationRequest mLocReq;
    private LocationCallback mLocCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.i(TAG, "locatom Result loc length: " + locationResult.getLocations().size());
            for (Location location : locationResult.getLocations()) {
                Log.i("MAPS", "Location: " + location.getLatitude()
                        + ", " + location.getLongitude());
                if (currLoc == null) {
                    currLoc = MyApplication.getMyApp().getDefaultLoc();
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currLoc).zoom(15).build();
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
            }
        }
    };
    private MyApplication app;
    private FragmentManager fragManager;
    private LatLng currLoc;
    private DrawerLayout drawerLayout;
    private ImageButton drawerIcon;
    private AppCompatTextView navHeaderName;
    private ImageView navHeaderIcon;
    private List<Job_Model> all_db_jobs;
    private List<Necessity_Model> all_db_necs;
    private static final int REQUEST_LOCATION_CODE = 99;
    private boolean canPin = true;
    private boolean pinAdded = false;
    private boolean canNotify = true;
    private long pinCooldown;
    private NotificationManagerCompat notificationManager;
    private AppCompatButton requestBtn;
    Set<Pin> dbCoords = new HashSet<>();
    Set<NotifiedPin> notifiedPins = new HashSet<>();
    Set<Polygon> validAreas = new HashSet<>();
    private static Circle currLocCircle;
    private static final double VALID_RADIUS_METERS = 21.0;
    private static final double PIN_VIEW_RAD_METERS = 500.0;
    private static final int PIN_TIMER_SEC = 60;
    private static final long PIN_NOTIFIED_DURATION_MIN = 120;
    private static final double MIN_CLICK_DIST = 0.03;
    private static final String CHANNEL_ID = "notification_id";
    private static final int NOTIFICATION_ID = 3000;
    private static final String TAG = "BaseMapActivity";
    private static final String BACK_STACK_ROOT_TAG = "Base_Map_Activity_Root";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        mFusedLocClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        app = (MyApplication) getApplication();
        app.setObserver(this);
        // Google Places
        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                currLoc = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17));
            }

            @Override
            public void onError(Status status) {
                System.out.println("GOOGLE PLACES ERROR");
            }
        });

        // Set to notification manager initialized in NotificationInitialize
        notificationManager = NotificationManagerCompat.from(this);

        // Read in coordinates from the database
       // timerHandler.postDelayed(timerRunnable, 0);
        fragManager = getSupportFragmentManager();

        drawerLayout = findViewById(R.id.base_map_drawer_layout);
        drawerIcon = findViewById(R.id.nav_button);
        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                displaySelectedScreen(menuItem.getItemId());
                return true;
            }
        });
        View.OnClickListener profileOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragManager.beginTransaction()
                        .replace(R.id.content_frame, new ProfileFragment())
                        .addToBackStack(BACK_STACK_ROOT_TAG)
                        .commit();
            }
        };
        View navHeaderView = navView.getHeaderView(0);
        navHeaderName = navHeaderView.findViewById(R.id.nav_header_name);
        // TODO: Set up users with names
        navHeaderName.setText(app.getUser().getName());
        navHeaderIcon = navHeaderView.findViewById(R.id.nav_header_profile_image);
        navHeaderName.setOnClickListener(profileOnClickListener);
        navHeaderIcon.setOnClickListener(profileOnClickListener);

        requestBtn = findViewById(R.id.request_button);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                String requestTypes[] = {"Job", "Necessity"};

                builder.setTitle("Request a: ");
                builder.setItems(requestTypes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,  int which) {
                        Fragment fragment = null;
                        switch(which) {
                            case 0:
                                Log.i(TAG, "REQ JOB CLICKED");
                                fragment = RequestJobFragment.newInstance(null, BACK_STACK_ROOT_TAG);
                                break;
                            case 1:
                                Log.i(TAG, "REQ NEC CLICKED");
                                fragment = RequestNecessityFragment.newInstance(null, BACK_STACK_ROOT_TAG);
                                break;
                        }
                        if (fragment != null) {
                            fragManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            fragManager.beginTransaction()
                                    .replace(R.id.content_frame, fragment, "requestFrag")
                                    .addToBackStack(BACK_STACK_ROOT_TAG)
                                    .commit();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void displaySelectedScreen(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.nav_jobs:
                Log.i(TAG, "Jobs selected");
                fragment = new YourJobsFragment();
                break;
            case R.id.nav_necs:
                Log.i(TAG, "Necs selected");
                fragment = new YourNecsFragment();
                break;
        }
        if (fragment != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
            fragManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(BACK_STACK_ROOT_TAG)
                    .commit();
        }
    }

    // Creates a notification and places in the device's System tray
    private void sendNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Pin Nearby")
                .setContentText("A pin has been detected nearby you. Open app to see location?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notification.setAutoCancel(true);

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    // If a pin that is notified passes a certain duration, remove it from the list of notified pins
    private void checkNotifiedPins() {
        List<NotifiedPin> oldPins = new ArrayList<>();
        long curT = System.currentTimeMillis();

        for (NotifiedPin p : notifiedPins) {
            long pinT = p.getTimeMilli() +
                    PIN_NOTIFIED_DURATION_MIN * 60 * 1000;
            if (System.currentTimeMillis() >= p.getTimeMilli() +
                    PIN_NOTIFIED_DURATION_MIN * 60 * 1000) {
                oldPins.add(p);
            }
        }

        notifiedPins.removeAll(oldPins);
    }

    // Adds all the markers from the database onto the map
    private void addMarkers() {
        for (Pin p : dbCoords) {
            if (currLoc != null) {
                // Only show Pins within a certain radius of user.
                if (PIN_VIEW_RAD_METERS >= getDistance(currLoc.latitude, currLoc.longitude, p.coords.latitude, p.coords.longitude)) {
                    MarkerOptions mo = new MarkerOptions();
                    mo.position(p.coords);

                    switch (p.need) {
                        case "Food":
                            mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.foodpin));
                            break;
                        case "Money":
                            mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.moneypin));
                            break;
                        case "FirstAid":
                            mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.firstaidpin));
                            break;
                        case "Ride":
                            mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.ridepin));
                            break;
                    }
                    mMap.addMarker(mo);

                    NotifiedPin temp = new NotifiedPin(p, System.currentTimeMillis());
                    if(!notifiedPins.contains(temp)) {
                        notifiedPins.add(temp);
                        pinAdded = true;
                    }
                }
            }
        }

        checkNotifiedPins();

        // Send a notification to the tray that a marker appeared nearby
        if (pinAdded) {
            sendNotification();
            pinAdded = false;
        }
    }

    // Highlight valid areas to place pins
    private void highlightAreas() {
        // Temporary values w/o database
        // Downtown SLO
        LatLng l1 = new LatLng(35.275774, -120.667078);
        LatLng l2 = new LatLng(35.281857, -120.664997);
        LatLng l3 = new LatLng(35.279882, -120.658361);

        // Google HQ
        LatLng l4 = new LatLng(37.423270, -122.084100);
        LatLng l5 = new LatLng(37.419606, -122.084347);
        LatLng l6 = new LatLng(37.422017, -122.087298);

        // Cal Poly
        LatLng l7 = new LatLng(35.303562, -120.667387);
        LatLng l8 = new LatLng(35.296452, -120.664426);
        LatLng l9 = new LatLng(35.299534, -120.655928);
        LatLng l10 = new LatLng(35.304122, -120.658932);

        // Foothill
        LatLng l11 = new LatLng(35.298238, -120.680862);
        LatLng l12 = new LatLng(35.290848, -120.678416);
        LatLng l13 = new LatLng(35.290462, -120.667645);
        LatLng l14 = new LatLng(35.295786, -120.668889);

        PolygonOptions highlight = new PolygonOptions();
        highlight.add(l1, l2, l3);
        highlight.strokeWidth(10);
        highlight.fillColor(Color.argb(35, 0, 0, 255));

        Polygon polygon = mMap.addPolygon(highlight);
        validAreas.add(polygon);

        PolygonOptions highlight2 = new PolygonOptions();
        highlight2.add(l4, l5, l6);
        highlight2.strokeWidth(10);
        highlight2.fillColor(Color.argb(35, 0, 0, 255));

        polygon = mMap.addPolygon(highlight2);
        validAreas.add(polygon);

        PolygonOptions highlight3 = new PolygonOptions();
        highlight3.add(l7, l8, l9, l10);
        highlight3.strokeWidth(10);
        highlight3.fillColor(Color.argb(35, 0, 0, 255));

        polygon = mMap.addPolygon(highlight3);
        validAreas.add(polygon);

        PolygonOptions highlight4 = new PolygonOptions();
        highlight4.add(l11, l12, l13, l14);
        highlight4.strokeWidth(10);
        highlight4.fillColor(Color.argb(35, 0, 0, 255));

        polygon = mMap.addPolygon(highlight4);
        validAreas.add(polygon);
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    // Gets distance between 2 coords in km
    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadiusKm * c;
    }

    // For handling permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }

                        mMap.setMyLocationEnabled(true);
                    }
                    //Permission denied
                    else {
                        Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    public Boolean checkValidPin(AlertDialog.Builder builder, AlertDialog alertDialog, final LatLng pin) {
        // Cooldown timer condition
        if (!canPin) {
            builder.setTitle("Recently placed pin");
            builder.setMessage("Must wait " + pinCooldown + " seconds before placing a new pin");
            builder.setNeutralButton("Clear", null);
            alertDialog = builder.create();
            alertDialog.show();
            return false;
        }

        /*
        // Distance from user current position condition
        if (getDistance(currLoc.latitude, currLoc.longitude, pin.latitude, pin.longitude) >= VALID_RADIUS_METERS / 1000) {
            builder.setTitle("Invalid location");
            builder.setMessage("Pins must be placed in a 0.04km radius");
            builder.setNeutralButton("Clear", null);
            alertDialog = builder.create();
            alertDialog.show();
            return false;
        }

        // If the pin is not located in a valid area, do not allow placing of pin
        boolean goodArea = false;

        for (Polygon a : validAreas) {
            if (PolyUtil.containsLocation(pin, a.getPoints(), false)) {
                goodArea = true;
            }
        }


        if (!goodArea) {
            builder.setTitle("Invalid location");
            builder.setMessage("Pins can only be placed within the blue highlighted areas");
            builder.setNeutralButton("Clear", null);
            alertDialog = builder.create();
            alertDialog.show();
            return false;
        }
        */

        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            Log.i(TAG, "Location Permission Not Granted");
        }
        setPins();
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    public void setPins() {
        mMap.clear();
        // SET JOB PINS
        for (Job_Model job : app.getAllJobs()) {
            if (!job.acceptor.equals("")) continue;
            Drawable drawable = getDrawable(R.drawable.marker_task);
            switch(job.type) {
                case "Errand":
                    drawable = getDrawable(R.drawable.marker_errand);
                    break;
                case "Manual Labor":
                    drawable = getDrawable(R.drawable.marker_manual);
                    break;
                case "Virtual":
                    drawable = getDrawable(R.drawable.marker_virtual);
                    break;
            }
            BitmapDescriptor icon = getMarkerIconFromDrawable(drawable);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(job.location)
                    .icon(icon));
            marker.setTag(job);
        }

        // SET NECESSITY PINS
        for (Necessity_Model nec : app.getAllNecs()) {
            if (!nec.acceptor.equals("")) continue;
            Drawable drawable = getDrawable(R.drawable.marker_money);
            switch (nec.type) {
                case "FirstAid":
                    drawable = getDrawable(R.drawable.marker_firstaid);
                    break;
                case "Ride":
                    drawable = getDrawable(R.drawable.marker_ride);
                    break;
                case "Food":
                    drawable = getDrawable(R.drawable.marker_food);
                    break;
            }
            BitmapDescriptor icon = getMarkerIconFromDrawable(drawable);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(nec.location)
                    .icon(icon));
            marker.setTag(nec);
           // mMap.addMarker(mo);
           // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pin, 17));
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = null;
                AppCompatTextView type;
                AppCompatTextView desc;
                if (marker.getTag() instanceof Job_Model) {
                    Job_Model j = (Job_Model) marker.getTag();
                    v = getLayoutInflater().inflate(R.layout.map_job_window, null);
                    type = v.findViewById(R.id.job_info_type);
                    AppCompatTextView pay = v.findViewById(R.id.job_info_pay);
                    desc = v.findViewById(R.id.job_info_desc);
                    type.setText(j.type);
                    pay.setText("$" + j.pay);
                    desc.setText(j.title);
                    Log.i(TAG, "JOB WINDOW: " + j.type + ", " + j.pay + ", " + j.title);

                } else if (marker.getTag() instanceof Necessity_Model) {
                    Necessity_Model n = (Necessity_Model) marker.getTag();
                    v = getLayoutInflater().inflate(R.layout.map_nec_window, null);
                    type = v.findViewById(R.id.nec_info_type);
                    desc = v.findViewById(R.id.nec_info_desc);
                    type.setText(n.type);
                    desc.setText(n.description);
                }
                return v;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Fragment fragment = null;
                String s = "";
                if (marker.getTag() instanceof Job_Model) {
                    Job_Model j = (Job_Model) marker.getTag();
                    fragment = JobDetailsFragment.newInstance(j, BACK_STACK_ROOT_TAG);
                    s = "jobDetailsFrag";
                } else if (marker.getTag() instanceof Necessity_Model) {
                    Necessity_Model n = (Necessity_Model) marker.getTag();
                    fragment = NecessityDetailsFragment.newInstance(n, BACK_STACK_ROOT_TAG);
                    s = "necDetailsFrag";
                }
                if (fragment != null) {
                    fragManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragManager.beginTransaction()
                            .replace(R.id.content_frame, fragment, s)
                            .addToBackStack(BACK_STACK_ROOT_TAG)
                            .commit();
                }
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        currLoc = new LatLng(location.getLatitude(), location.getLongitude());

        // Resolves issue with CameraUpdateFactory not being initialized
        try {
            MapsInitializer.initialize(this);
        }
        catch (Exception e) {
            Log.e("Location Error", "GoogleMaps Issue", e);
            return;
        }

        // Move map to current location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currLoc).zoom(10).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        Log.i(TAG, "Camera Position: " + mMap.getCameraPosition().target.latitude + ", "
                + mMap.getCameraPosition().target.longitude);
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 17));
        if (mFusedLocClient != null) {
            mFusedLocClient.removeLocationUpdates(mLocCallback);
        }
        /*
        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
        */
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocReq = new LocationRequest();
        mLocReq.setInterval(500);
        mLocReq.setFastestInterval(500);
        mLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        Intent intent = new Intent(this, MyLocationService.class);
        startService(intent);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> s = mFusedLocClient.getLastLocation();
            s.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "GET LAST LOC: " + e.getMessage());
                }
            });
            s.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.i(TAG, "cur loc: " + location);
                    if (location == null) return;
                    currLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currLoc).zoom(10).build();
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
            });
            mFusedLocClient.requestLocationUpdates(mLocReq, mLocCallback, Looper.myLooper());
        }

    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check if user has given permission previously and denied request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            // Ask user for permission
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
        }
    }

    public FragmentManager getFragManager() {
        return fragManager;
    }

    public FusedLocationProviderClient getmFusedLocClient() {
        return mFusedLocClient;
    }

    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public LatLng getCurrLoc() {
        return currLoc;
    }

    public void setJobList(List<Job_Model> jobList) {
        this.all_db_jobs = jobList;
    }

    public void setNecList(List<Necessity_Model> necList) {
        this.all_db_necs = necList;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
