package com.lhester.polendey.drivers;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lhester.polendey.drivers.Schedules.MySchedules;
import com.lhester.polendey.drivers.Schedules.ViewSched;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


public class DriverMapActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, PermissionsListener {


    public static boolean play_pickup = false, play_drop = false;
    Location mLastLocation;

    private DatabaseReference ListenToNewSchedule;
    // variables for adding location layer
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    public static NavigationMapRoute navigationMapRoute;

    private MapboxMap mapboxMap;
    private MapView mapView;
    private Marker passengerMarker, destinationMarkerr;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 1;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    public static Button btndrop;
    private Button btncancel, btnGoOnline, btn_start_travel, btn_navigate;
    private int status = 0;
    public static String customerId = "", destination, pickup_text;
    public static com.mapbox.mapboxsdk.geometry.LatLng destinationLatLng;
    private Boolean isLoggingOut = false, isOnline = false;
    private SupportMapFragment mapFragment;
    private RelativeLayout mCustomerInfo;
    private CircleImageView mCustomerProfileImage;
    private TextView txtfare;
    public static String final_fare = "";
    private TextView mCustomerName, mCustomerPhone, mCustomerDestination, mCustomerPickup;
    private ImageView imgMsg, imgCall;
    private List<Polyline> polylines;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    public static Toolbar toolbar;
    NavigationView navigationView;
    public static TextView txtmsgcount;

    private CheckBox chk;
    private BottomSheetBehavior mBottomSheetBehavior;
    private LinearLayout bottomSheet;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getBaseContext(),
                MyService.class));
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_driver_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        mAuth = FirebaseAuth.getInstance();
        ListenToNewSchedule = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Schedules");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        initViews();
        getUserInformation();
        txtmsgcount = findViewById(R.id.msgcount);
        polylines = new ArrayList<>();
        naviations();

        btnGoOnline = findViewById(R.id.go_online);
        btn_navigate = findViewById(R.id.openways);
        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        // .shouldSimulateRoute(simulateRoute)
                        .build();
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(DriverMapActivity.this, options);

            }
        });
        btn_start_travel = findViewById(R.id.start_travel);
        imgMsg = findViewById(R.id.idmsg);

        imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, Msgs.class);
                startActivity(intent);
                txtmsgcount.setVisibility(View.GONE);
                txtmsgcount.setText("");
                msgcount = 0;
            }
        });

        btn_start_travel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                onboard_passenger = true;
                onboard();
                driver_Arrived = 2;
                // playNotification();
                driver_action = 2;
                btn_start_travel.setVisibility(View.GONE);
                btn_start_travel.setEnabled(false);
                btncancel.setVisibility(View.VISIBLE);
                com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                        .include(new com.mapbox.mapboxsdk.geometry.LatLng(destinationLatLng.getLatitude(), destinationLatLng.getLongitude()))
                        .include(new com.mapbox.mapboxsdk.geometry.LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 48));

                if (animate_arrive) {
                    animate_arrive = false;
                    // mBottomSheetBehavior.setState(mBottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });


        //something automatic find current location upon intent
     /*   if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity
                    .this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapView.getMapAsync(this);
        } */

        progressBarCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRide();
                accepted = true;
                countDownTimer.cancel();
                timeCountInMilliSeconds = 21000;
                btnGoOnline.setVisibility(View.GONE);
                progressBarCircle.setVisibility(View.GONE);
                btncancel.setVisibility(View.VISIBLE);
                driver_action = 1;
                MediaPlayer mediaPlayer1;
                mediaPlayer1 = MediaPlayer.create(DriverMapActivity.this, R.raw.notification);
                mediaPlayer1.start();

            }
        });
        mCustomerInfo = findViewById(R.id.rel2);
        btndrop = findViewById(R.id.btndrop);
        btncancel = findViewById(R.id.btnCancel);
        mCustomerProfileImage = findViewById(R.id.profile_image_customer);
        mCustomerName = findViewById(R.id.customer_name);
        mCustomerPhone = findViewById(R.id.customer_phone);
        mCustomerDestination = findViewById(R.id.destination_text);
        mCustomerPickup = findViewById(R.id.pickup_text);
        txtfare = findViewById(R.id.fare);
        final int expand = getResources().getDimensionPixelSize(R.dimen.expand_peek_height);
        final int collapsed = getResources().getDimensionPixelSize(R.dimen.collapsed_peek_height);
        bottomSheet = findViewById(R.id.bottomsheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == 4) {
                    mBottomSheetBehavior.setPeekHeight(collapsed);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        });

        mBottomSheetBehavior.setPeekHeight(expand);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        btnGoOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline) {
                    final SweetAlertDialog dialog = new SweetAlertDialog(DriverMapActivity.this, SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText("Are you sure?")
                            .setContentText("You are going online!")
                            .setConfirmText("Confirm")
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {

                                    isOnline = true;
                                    btnGoOnline.setBackgroundResource(R.drawable.custom_btn_red);
                                    btnGoOnline.setText("Go Offline");
                                    mBottomSheetBehavior.setPeekHeight(collapsed);
                                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    driver_online();
                                    if (mediaPlayer != null) {
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                    }
                                    mediaPlayer = MediaPlayer.create(DriverMapActivity.this, R.raw.online);
                                    mediaPlayer.start();


                                    sDialog
                                            .setTitleText("Online!")
                                            .setContentText("You are online now!")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    dialog.findViewById(R.id.cancel_button).setVisibility(View.GONE);

                                }
                            });

                    dialog.show();

                } else {
                    final SweetAlertDialog offdialog = new SweetAlertDialog(DriverMapActivity.this, SweetAlertDialog.WARNING_TYPE);
                    offdialog.setTitleText("Are you sure?")
                            .setContentText("You are going to offline!")
                            .setConfirmText("Confirm")
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    isOnline = false;
                                    btnGoOnline.setBackgroundResource(R.drawable.custom_button);
                                    btnGoOnline.setText("Go Online");
                                    //mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    driver_offline();
                                    if (mediaPlayer != null) {
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                    }
                                    mediaPlayer = MediaPlayer.create(DriverMapActivity.this, R.raw.offline);
                                    mediaPlayer.start();

                                    sDialog
                                            .setTitleText("OFFLINE!")
                                            .setContentText("You are offline now!")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    offdialog.findViewById(R.id.cancel_button).setVisibility(View.GONE);
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }

                                }
                            });

                    offdialog.show();

                }

            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(DriverMapActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Cancel Ride")
                        .setContentText("Ride Will be Canceled")
                        .setConfirmText("Confirm")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                btncancel.setVisibility(View.GONE);
                                btnGoOnline.setVisibility(View.VISIBLE);
                                countDownTimer.cancel();
                                DatabaseReference current_driver = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                                        .child(customerId).child("Current Driver");
                                current_driver.removeValue();
                                if (!onboard_passenger) {
                                    cancelRide2();
                                } else {
                                    cancelRide3();
                                    onboard_passenger = false;
                                }

                                play_drop = false;
                                play_pickup = false;
                                driver_action = 3;
                                toolbar.setTitle("₱" + final_fare);
                                btndrop.setVisibility(View.GONE);
                                if (navigationMapRoute != null) {
                                    navigationMapRoute.removeRoute();
                                }
                                btn_start_travel.setVisibility(View.GONE);
                                driver_Arrived = 0;
                                pickupLatLng = null;
                                destinationLatLng = null;
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        });

        btndrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverMapActivity.this, Submit_rating.class));
                // playNotification();
             /*   dropoff();
                play_drop = false;
                play_pickup = false;
                driver_action = 3;
                toolbar.setTitle("₱" + final_fare);
                btndrop.setVisibility(View.GONE);
                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                }

                driver_Arrived = 0;
                pickupLatLng = null;
                destinationLatLng = null;
                //  customerId="";
                //acceptRide(); */
            }
        });

        getAssignedCustomer();
        checkMsgs();
        chk = findViewById(R.id.chk_map_open);
        chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chk.isChecked()) {
                    mapView.setVisibility(View.VISIBLE);
                } else {
                    mapView.setVisibility(View.INVISIBLE);
                }
            }
        });
        //  updateCustomer();
        checkNewSchedule();
    }

    private void onboard() {
        DatabaseReference onboardref = FirebaseDatabase.getInstance().getReference("Transactions").child(currentDateandTime);
        // refWorking.
        HashMap userMap = new HashMap();
        userMap.put("Type", "Onboard");
        onboardref.updateChildren(userMap);
    }

    private String getAddress(com.mapbox.mapboxsdk.geometry.LatLng location) {

        String ad = "";
        Geocoder geocoder = new Geocoder(DriverMapActivity.this, Locale.getDefault());
        //    String city="";
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            ad = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ad;
    }

    private String date_today() {
        String cdate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        cdate = sdf.format(new Date());
        return cdate;
    }

    private String time_today() {
        String mytime;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        mytime = sdf.format(new Date());
        return mytime;
    }

    private void cancelRide() {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        // currentDateandTime = sdf.format(new Date());
        DatabaseReference refTransaction = FirebaseDatabase.getInstance().getReference("Transactions").push();
        currentDateandTime = refTransaction.getKey();
        // refWorking.
        HashMap userMap = new HashMap();
        userMap.put("CID", customerId);
        userMap.put("DID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        userMap.put("pickupLat", pickupLatLng.getLatitude());
        userMap.put("pickupLong", pickupLatLng.getLongitude());
        userMap.put("pickupAddress", getAddress(pickupLatLng));
        userMap.put("destinationAddress", getAddress(destinationLatLng));
        userMap.put("destinationLat", destinationLatLng.getLatitude());
        userMap.put("destinationLong", destinationLatLng.getLongitude());
        userMap.put("Type", "Canceled By the Driver");
        userMap.put("Reason", "Unable to Accept");
        userMap.put("Fare", "0");
        userMap.put("Date", date_today());
        userMap.put("Time", time_today());
        userMap.put("tid", currentDateandTime);
        userMap.put("fd", date_today() + " _ " + time_today());
        refTransaction.setValue(userMap);

        btncancel.setVisibility(View.GONE);
        btndrop.setVisibility(View.GONE);
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cancelref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverId).child("Customer Request");
        cancelref.removeValue();
        customerId = "";
    }

    private void cancelRide2() {
        DatabaseReference refTransaction = FirebaseDatabase.getInstance().getReference("Transactions").child(currentDateandTime);
        // refWorking.
        HashMap userMap = new HashMap();
        userMap.put("Type", "Accepted, Canceled By the Driver");
        userMap.put("Reason", "Unable to get the passenger");

        refTransaction.updateChildren(userMap);
        btncancel.setVisibility(View.GONE);
        btndrop.setVisibility(View.GONE);
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cancelref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverId).child("Customer Request");
        cancelref.removeValue();
        customerId = "";
    }

    private boolean onboard_passenger = false;

    private void cancelRide3() {
        DatabaseReference refTransaction = FirebaseDatabase.getInstance().getReference("Transactions").child(currentDateandTime);
        // refWorking.
        HashMap userMap = new HashMap();
        userMap.put("Type", "Onboard, Canceled By the Driver");
        userMap.put("Reason", "Unable to continue the ride");

        refTransaction.updateChildren(userMap);
        btncancel.setVisibility(View.GONE);
        btndrop.setVisibility(View.GONE);
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cancelref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverId).child("Customer Request");
        cancelref.removeValue();
        customerId = "";
    }

    private void acceptRide() {
        btn_start_travel.setEnabled(true);
        btncancel.setVisibility(View.GONE);
        btndrop.setVisibility(View.GONE);
        timerText.setVisibility(View.GONE);
        //mCustomerInfo.setVisibility(View.GONE);
        DatabaseReference acceptRef = FirebaseDatabase.getInstance().getReference("Transactions").push();
        currentDateandTime = acceptRef.getKey();
        acceptRef.child(currentDateandTime);
        // refWorking.
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("CID", customerId);
        userMap.put("DID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        userMap.put("pickupLat", pickupLatLng.getLatitude());
        userMap.put("pickupLong", pickupLatLng.getLongitude());
        userMap.put("destinationLat", destinationLatLng.getLatitude());
        userMap.put("destinationLong", destinationLatLng.getLongitude());
        userMap.put("pickupAddress", getAddress(pickupLatLng));
        userMap.put("destinationAddress", getAddress(destinationLatLng));
        userMap.put("Type", "Accepted");
        userMap.put("Date", date_today());
        userMap.put("Time", time_today());
        userMap.put("Fare", "0");
        userMap.put("tid", currentDateandTime);
        userMap.put("fd", date_today() + " _ " + time_today());
        acceptRef.setValue(userMap);
        updateCustomer();
        btnGoOnline.setVisibility(View.VISIBLE);
    }

    private void naviations() {
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        txtfullname = header.findViewById(R.id.fullname);
        userImage = header.findViewById(R.id.userImage);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.sched:
                        startActivity(new Intent(DriverMapActivity.this, MySchedules.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.profile:
                        startActivity(new Intent(DriverMapActivity.this, SettingsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.histories:
                        startActivity(new Intent(DriverMapActivity.this, Transaction_Histories.class));
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.announcement:
                        startActivity(new Intent(DriverMapActivity.this, Announcement.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:

                        new SweetAlertDialog(DriverMapActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Log out")
                                .setContentText("You sure you want to log out?")
                                .setConfirmText("Logout")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        isOnline = false;
                                        driver_offline();
                                        finish();
                                        FirebaseAuth.getInstance().signOut();
                                        drawerLayout.closeDrawers();
                                        startActivity(new Intent(DriverMapActivity.this, LogInDriver.class));
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();

                  /*          AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
                            builder.setTitle("Confirm");
                            builder.setMessage("Are you sure?");
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                }
                            });

                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show(); */
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    public static String currentDateandTime = "";
    private DatabaseReference update_customer;

    private void updateCustomer() {
        update_customer = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerId).child("Current Driver");
        HashMap map = new HashMap();
        map.put("DID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("CID", customerId);
        map.put("TID", currentDateandTime);
        update_customer.updateChildren(map);
    }

    private void getAssignedCustomer() {
        btn_start_travel.setEnabled(false);
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("Customer Request").child("CustomerRideID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    Common_Var.customerID = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
                } else {
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static com.mapbox.mapboxsdk.geometry.LatLng pickupLatLng;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(locationLat, locationLng);

                    IconFactory iconFactory = IconFactory.getInstance(DriverMapActivity.this);
                    Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(DriverMapActivity.this.getResources(), R.drawable.pickupmarkers));
                    Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                    com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions2 = new MarkerOptions();
                    markerOptions2.position(pickupLatLng);
                    markerOptions2.icon(newIcon);
                    markerOptions2.title("Passenger Pickup Location");
                    markerOptions2.setSnippet("Pickup Location");
                    passengerMarker = mapboxMap.addMarker(markerOptions2);

                    com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                            .include(new com.mapbox.mapboxsdk.geometry.LatLng(pickupLatLng.getLatitude() - 0.001f, pickupLatLng.getLongitude() - 0.001f))
                            .include(new com.mapbox.mapboxsdk.geometry.LatLng(mLastLocation.getLatitude() + 0.001f, mLastLocation.getLongitude() + 0.001f))
                            .build();

                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 48));

                    Point origin = Point.fromLngLat(pickupLatLng.getLongitude(), pickupLatLng.getLatitude());
                    Point mapdestination = Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                    GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                    if (source != null) {
                        source.setGeoJson(Feature.fromGeometry(mapdestination));
                    }
                    getRoute(origin, mapdestination);
                    driver_Arrived = 1;
                    playNotification();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    MediaPlayer notificationPlayer;
    MediaPlayer notificationPlayer2;
    private void playNotification() {
        if (notificationPlayer == null) {
            notificationPlayer = MediaPlayer.create(this, R.raw.notification3);
            notificationPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });
        }
        notificationPlayer.start();
    }

    private void stopPlayer() {
        if (notificationPlayer != null) {
            notificationPlayer.release();
            notificationPlayer = null;
            //Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }

    private void playNotification2() {
        if (notificationPlayer2 == null) {
            notificationPlayer2 = MediaPlayer.create(this, R.raw.message_notification);
            notificationPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }
        notificationPlayer2.start();
    }

    private void stopPlayer2() {
        if (notificationPlayer2 != null) {
            notificationPlayer2.release();
            notificationPlayer2 = null;
            //Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }


    public static int driver_Arrived = 0;
    private float distance, distance2, distance3, current_fare;

    private DatabaseReference driver_location_ref;
    private ValueEventListener driver_location_listener_ref;

    private Boolean animate_arrive = true, animate_dropping = true, animate_request = true;

    private void driver_location() {

        driver_location_ref = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("l");
        driver_location_listener_ref = driver_location_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    getFare();
                    //   play_pickup=false;
                    // play_drop=false;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    Location loc1 = new Location("");
                    if (pickupLatLng != null) {
                        loc1.setLatitude(pickupLatLng.getLatitude());
                        loc1.setLongitude(pickupLatLng.getLongitude());
                    }
                    Location loc2 = new Location("");
                    if (driverLatLng != null) {

                        loc2.setLatitude(driverLatLng.latitude);
                        loc2.setLongitude(driverLatLng.longitude);
                    }
                    Location loc3 = new Location("");
                    if (destinationLatLng != null) {
                        loc3.setLatitude(destinationLatLng.getLatitude());
                        loc3.setLongitude(destinationLatLng.getLongitude());
                    }
                    Location current_location = new Location("");
                    current_location.setLatitude(mLastLocation.getLatitude());
                    current_location.setLongitude(mLastLocation.getLongitude());


                    distance = loc1.distanceTo(loc2); //  driver to pick up location
                    distance2 = loc2.distanceTo(loc3); // driver to destination
                    distance3 = loc1.distanceTo(loc3); // pick up  to destination
                    current_fare = loc1.distanceTo(current_location); // current fare
                    float fare;
                    if (driver_Arrived == 2) {

                        fare = mf + ((current_fare / 1000) - 1) * af;
                        if (fare < mf) {
                            fare = mf;
                        }
                        String strfare = String.format("%.2f", fare);
                        txtfare.setText(strfare);
                        final_fare = strfare;
                    }

                    if (distance < 100 && driver_Arrived <= 1) {
                        if (!play_pickup) {
                            playNotification();
                            play_pickup = true;
                            play_drop = false;
                            Log.e("Play", String.valueOf(play_pickup));
                            btn_start_travel.setVisibility(View.VISIBLE);
                            mBottomSheetBehavior.setState(mBottomSheetBehavior.STATE_EXPANDED);
                            if (passengerMarker != null) {
                                passengerMarker.remove();
                            }
                            Point mapdestination = Point.fromLngLat(destinationLatLng.getLongitude(), destinationLatLng.getLatitude());
                            Point origin = Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                            if (source != null) {
                                source.setGeoJson(Feature.fromGeometry(mapdestination));
                            }
                            getRoute(origin, mapdestination);
                        }


                    } else if (distance2 < 100 && driver_Arrived >= 2) {


                        if (!play_drop) {
                            mBottomSheetBehavior.setState(mBottomSheetBehavior.STATE_EXPANDED);
                            playNotification();
                            play_drop = true;
                            play_pickup = false;
                            btncancel.setVisibility(View.GONE);
                            btndrop.setVisibility(View.VISIBLE);
                            btnGoOnline.setVisibility(View.VISIBLE);
                            locationComponent.setCameraMode(CameraMode.TRACKING);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("Customer Request");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("destination") != null) {
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: " + destination);
                    } else {
                        mCustomerDestination.setText("Destination: --");
                    }
                    if (map.get("pickup") != null) {
                        pickup_text = map.get("pickup").toString();
                        mCustomerPickup.setText("Pickup: " + pickup_text);
                    } else {
                        mCustomerPickup.setText("Pickup: --");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(destinationLat, destinationLng);

                    }
                    if (passengerMarker != null) {
                        passengerMarker.remove();
                    }
                    IconFactory iconFactory = IconFactory.getInstance(DriverMapActivity.this);
                    Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(DriverMapActivity.this.getResources(), R.drawable.destination2));
                    Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                    com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions2 = new MarkerOptions();
                    markerOptions2.position(destinationLatLng);
                    markerOptions2.icon(newIcon);
                    markerOptions2.title("Passenger Destinationn");
                    markerOptions2.setSnippet("Destination");
                    destinationMarkerr = mapboxMap.addMarker(markerOptions2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private ProgressBar progressBarCircle;
    private CountDownTimer countDownTimer;
    private long timeCountInMilliSeconds = 21000;
    private MediaPlayer mediaPlayer;
    private TextView timerText;
    private Boolean accepted = false;

    private void initViews() {
        progressBarCircle = findViewById(R.id.progressBarCircle);
        timerText = findViewById(R.id.timerText);
        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);

    }

    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000) - 1);
                // playSound();
                if (((millisUntilFinished / 1000) - 1) >= 0) {
                    timerText.setText(String.valueOf((millisUntilFinished / 1000) - 1));
                    Log.e("counting", customerId);
                }
            }

            @Override
            public void onFinish() {
                Log.e("finish", customerId);
                progressBarCircle.setVisibility(View.GONE);
                timerText.setVisibility(View.GONE);
                playSoundend();
                cancelRide();
                accepted = false;
            }

        }.start();
    }

    private void playSoundend() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(DriverMapActivity.this, R.raw.fail);
        mediaPlayer.start();
    }

    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        //  mRideStatus.setVisibility(View.VISIBLE);
        //  btncancel.setVisibility(View.VISIBLE);
        progressBarCircle.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.VISIBLE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        startCountDownTimer();
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    String full_name = "";
                    if (map.get("last_name") != null) {
                        full_name = map.get("last_name").toString();
                    }
                    if (map.get("first_name") != null) {
                        full_name += "," + map.get("first_name").toString();
                    }
                    mCustomerName.setText(full_name);
                    if (map.get("phone") != null) {
                        mCustomerPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("image") != null) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.driver)
                                .into(mCustomerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void endRide() {
        String userId = "";
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Intent intent = new Intent(DriverMapActivity.this, LogInDriver.class);
            startActivity(intent);
            finish();
        }
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("Customer Request");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Customer Requests");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        customerId = "";
        if (assignedCustomerPickupLocationRefListener != null) {
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        btncancel.setVisibility(View.GONE);
        btndrop.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        txtfare.setText("0.00");
        //  mCustomerDestination.setText("Destination: --");
        mCustomerProfileImage.setImageResource(R.drawable.ic_person_black_24dp);
    }


    public static double driver_action = 0;

    private void driver_online() {
        if (isOnline) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("Drivers Available");

            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("Drivers Working");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (customerId) {
                case "":
                    geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            btnGoOnline.setVisibility(View.VISIBLE);
                            btncancel.setVisibility(View.GONE);
                            btn_start_travel.setVisibility(View.GONE);
                            btndrop.setVisibility(View.GONE);
                            mBottomSheetBehavior.setState(mBottomSheetBehavior.STATE_EXPANDED);


                            //  progressBarCircle.setVisibility(View.GONE);
                            //  timerText.setVisibility(View.GONE);

                        }
                    });
                    geoFireAvailable.setLocation(userId, new GeoLocation(locationComponent.getLastKnownLocation().getLatitude(),
                            locationComponent.getLastKnownLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                    break;

                default:

                    geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
               /*   geoFireWorking.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()), new GeoFire.CompletionListener() {

                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    }); */
                    GeoHash geoHash = new GeoHash(new GeoLocation(locationComponent.getLastKnownLocation().getLatitude(),
                            locationComponent.getLastKnownLocation().getLongitude()));
                    Map<String, Object> updates = new HashMap<>();
                    //updates.put(userId+"action", "requested");
                    updates.put(userId + "/g", geoHash.getGeoHashString());
                    updates.put(userId + "/l", Arrays.asList(locationComponent.getLastKnownLocation().getLatitude(),
                            locationComponent.getLastKnownLocation().getLongitude(), driver_action, mLastLocation.getBearing()));
                    refWorking.updateChildren(updates);

                    break;
            }
        }
    }

    private void disconnectDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers Available");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    private void driver_offline() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers Available");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapView.getMapAsync(this);

                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut) {
            //    disconnectDriver();
            mapView.onStop();
        }
    }

    private float mf = 0, af = 0;

    private void getFare() {
        DatabaseReference databaseReferenceFare = FirebaseDatabase.getInstance().getReference().child("Fare").child("current_fare");
        databaseReferenceFare.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("minimum_fare") != null) {
                        mf = Float.parseFloat(map.get("minimum_fare").toString());
                    }
                    if (map.get("additional_fare") != null) {
                        af = Float.parseFloat(map.get("additional_fare").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static int msgcount = 0;

    private void checkMsgs() {

        DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                // .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
                .child("Messages");
        databaseReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        if (postSnapShot.getKey().equals("CID")) {
                            String value1 = dataSnapshot.child("CID").getValue().toString();
                            if (value1.equals(customerId)) {
                                String seen = "";
                                String messageD = "";

                                if (dataSnapshot.child("messageC").exists()) {
                                    messageD = dataSnapshot.child("messageC").getValue().toString();
                                    if (dataSnapshot.child("seen").exists()) {
                                        seen = dataSnapshot.child("seen").getValue().toString();
                                        if (seen.equals("False") && !messageD.equals("")) {
                                            //  Intent intent = new Intent(CustomerMapActivity.this, Msgs.class);
                                            //  startActivity(intent);
                                            msgcount++;
                                            if (msgcount > 0) {
                                                txtmsgcount.setVisibility(View.VISIBLE);
                                                txtmsgcount.setText(String.valueOf(msgcount));
                                                msgplaynotif();
                                            } else {
                                                txtmsgcount.setVisibility(View.GONE);
                                            }

                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    MediaPlayer msgnotif;

    private void msgplaynotif() {

        if (msgnotif == null) {
            msgnotif = MediaPlayer.create(this, R.raw.msgtone);
            msgnotif.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    msgStop();

                }
            });
        }
        msgnotif.start();
    }

    private void msgStop() {
        if (msgnotif != null) {
            msgnotif.release();
            msgnotif = null;
            //Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && !customerId.equals("")) {
            // Toast.makeText(CustomerMapActivity.this,"Please Cancel The Ride First",Toast.LENGTH_LONG);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_HOME && !customerId.equals("")) {
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_APP_SWITCH && !customerId.equals("")) {
            return false;
        } else {
            isOnline = false;
            driver_offline();
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(DriverMapActivity.this, LogInDriver.class));

        }
        return super.onKeyDown(keyCode, event);
    }


    private CircleImageView userImage;
    private TextView txtfullname;

    private void getUserInformation() {
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            String ln, fn, mn;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    if (dataSnapshot.hasChild("firstname")) {
                        fn = dataSnapshot.child("firstname").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("lastname")) {
                        ln = dataSnapshot.child("lastname").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("middlename")) {
                        mn = dataSnapshot.child("middlename").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(userImage);
                    }
                    txtfullname.setText(ln + ", " + fn + " " + mn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        //   addDestinationIconSymbolLayer(style);
                    }
                });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<DriverMapActivity> activityWeakReference;

        MainActivityLocationCallback(DriverMapActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            DriverMapActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();
                mLastLocation = location;

                if (location == null) {
                    return;
                }

// Create a Toast which displays the new location's coordinates
                if (getApplicationContext() != null) {
                    mLastLocation = location;
                    com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude(), location.getLongitude());
                    driver_online();
                    if (!customerId.equals("")) {
                        //   if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //        return;
                        //    }
                        driver_location();

                        //    if (animate_request && pickupLatLng != null) {
                        //    com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder builder = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder();
                        //     builder.include(pickupLatLng);
                        //     builder.include(latLng);
                        //     animate_request = false;
                        //  }
                    } else {
                        animate_request = true;
                        animate_arrive = true;
                        animate_dropping = true;
                        driver_action = 0;
                        driver_Arrived = 0;
                        locationComponent.setCameraMode(CameraMode.TRACKING);

                        btnGoOnline.setVisibility(View.VISIBLE);
                        Double lat = mLastLocation.getLatitude();
                        Double lng = mLastLocation.getLongitude();


                        //  com.mapbox.mapboxsdk.geometry.LatLng LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);
// Move map camera to the selected location
                        //   mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 15));
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                            mapboxMap.clear();
                        }
                    }

                }
                //    Toast.makeText(DriverMapActivity.this,String.valueOf(result.getLastLocation().getLatitude()),Toast.LENGTH_SHORT).show();
// Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            DriverMapActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
// Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private AlertDialog noftiDiaglog2;

    private void checkNewSchedule() {
        ListenToNewSchedule.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.e("Key1", snapshot.getKey());
                getSchedValue(snapshot.getKey());

            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getSchedValue(String key) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Schedules")
                .child(key);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("TYPE").exists()) {
                        if (snapshot.child("TYPE").getValue().toString().equals("Schedule")) {
                            Log.e("TYPE", snapshot.toString());

                            getSchedInfo(key);

                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    String full_name;
    String imagelink;
    String dd;
    String tt;
    String destination_distance;
    String fare;
    String CID;
    String phone;
    String s_from;
    String s_to;

    private void getSchedInfo(String key) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Transactions")
                .child(key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    if (snapshot.hasChild("Fare")) {
                        fare = snapshot.child("Fare").getValue().toString();
                    }
                    if (snapshot.hasChild("Distance")) {
                        destination_distance = snapshot.child("Distance").getValue().toString();
                    }
                    if (snapshot.hasChild("CID")) {
                        CID = snapshot.child("CID").getValue().toString();
                    }
                    if (snapshot.hasChild("Date")) {
                        dd = snapshot.child("Date").getValue().toString();
                    }
                    if (snapshot.hasChild("Time")) {
                        tt = snapshot.child("Time").getValue().toString();
                    }
                    if (snapshot.hasChild("pickupAddress")) {
                        s_from = snapshot.child("pickupAddress").getValue().toString();
                    }
                    if (snapshot.hasChild("destinationAddress")) {
                        s_to = snapshot.child("destinationAddress").getValue().toString();
                    }
                    getPassengerInfo(key, fare, destination_distance, s_to, s_from, dd, tt, CID);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getPassengerInfo(String key, String fare1, String cd, String desti, String pickup, String d, String t, String ID) {


        DatabaseReference PassengerInfo;
        PassengerInfo = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(ID);
        PassengerInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("last_name") && snapshot.hasChild("first_name")) {
                        full_name = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                    }
                    if (snapshot.hasChild("image")) {
                        imagelink = snapshot.child("image").getValue().toString();
                    }
                    if (snapshot.hasChild("phone")) {
                        phone = snapshot.child("phone").getValue().toString();
                    }

                    LayoutInflater inflater = LayoutInflater.from(DriverMapActivity.this);
                    View subView = inflater.inflate(R.layout.customalert, null);
                    final TextView subfare = (TextView) subView.findViewById(R.id.sfare);
                    final TextView subpickup = (TextView) subView.findViewById(R.id.spickup_text);
                    final TextView subdesti = (TextView) subView.findViewById(R.id.sdestination_text);
                    final TextView subdate = (TextView) subView.findViewById(R.id.sdate);
                    final TextView subtime = (TextView) subView.findViewById(R.id.stime);
                    final TextView subfullname = (TextView) subView.findViewById(R.id.scustomer_name);
                    final TextView subdistance = (TextView) subView.findViewById(R.id.sdistance);
                    final TextView subphone = (TextView) subView.findViewById(R.id.scustomer_phone);
                    final Button subAccept = (Button) subView.findViewById(R.id.view_request);
                    final Button subCancel = (Button) subView.findViewById(R.id.check_later);
                    final ImageView subImageView = (ImageView) subView.findViewById(R.id.sprofile_image_customer);

                    subfare.setText(fare1);
                    subpickup.setText(pickup);
                    subdesti.setText(desti);
                    subdate.setText(d);
                    subtime.setText(t);
                    subfullname.setText(full_name);
                    subdistance.setText(cd);
                    subphone.setText(phone);
                    subAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DriverMapActivity.this, ViewSched.class);
                            intent.putExtra("SID", key);
                            intent.putExtra("cname", full_name);
                            intent.putExtra("pickup", pickup);
                            intent.putExtra("destination", desti);
                            intent.putExtra("distance", cd);
                            intent.putExtra("fare", fare1);
                            intent.putExtra("dd", d);
                            intent.putExtra("tt", t);
                            startActivity(intent);
                            noftiDiaglog2.dismiss();
                        }
                    });
                    subCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noftiDiaglog2.dismiss();
                        }
                    });

                    playNotification2();

                    noftiDiaglog2 = new AlertDialog.Builder(DriverMapActivity.this)
                            //   .setIcon(R.drawable.markerpassenger)
                            .setView(subView)

                            /* .setMessage("Name :" + full_name + "\n\n" +
                                     "From   :" + pickup + "\n\n" +
                                     "To        :" + desti + "\n\n" +
                                     "Distance:  " + cd + " km \n" +
                                     "Fare:          P " + fare1 + "\n" +
                                     "Date:          " + d + "\n" +
                                     "Time:          " + t + "\n")
                             .setPositiveButton("View Request", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     Intent intent = new Intent(DriverMapActivity.this, ViewSched.class);
                                     intent.putExtra("SID", key);
                                     intent.putExtra("cname", full_name);
                                     intent.putExtra("pickup", pickup);
                                     intent.putExtra("destination", desti);
                                     intent.putExtra("distance", cd);
                                     intent.putExtra("fare", fare1);
                                     intent.putExtra("dd", d);
                                     intent.putExtra("tt", t);
                                     startActivity(intent);
                                     noftiDiaglog2.dismiss();
                                 }
                             })
                             .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     noftiDiaglog2.dismiss();
                                 }
                             })*/
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
