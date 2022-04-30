package com.lhester.polendey.trikila;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Service.START_NOT_STICKY;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    // variables for adding location layer
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private static DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private static NavigationMapRoute navigationMapRoute;

    private MapboxMap mapboxMap;
    private MapView mapView;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 1;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);


    public static TextView txt_destination, txt_pickup_loc;
    public static Marker pickup, destination_marker;
    public static int driver_Arrived = 0;
    // SET THE MARKER PICKUP LOCATION
    public static com.mapbox.mapboxsdk.geometry.LatLng pickupLocation;
    final int REQ_PERMISSION = 1;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    GeoFire geoFire;
    GeoQuery geoQuery;
    MediaPlayer notificationPlayer;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference update_transaction;
    private GoogleMap mMap;
    public static Location mLastLocation;
    private Button btncall;
    public static List<Polyline> polylines;
    private DatabaseReference check_request;
    public static BottomSheetBehavior mBottomSheetBehavior;
    private LinearLayout bottomSheet;
    private TextView txtfare;
    public static ImageView imgReset, imgLoc;
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private Boolean driver_found = false;
    //FIND DRIVER ONLINE
    private double radius = 0.1;
    public static String driver_id_found = "";
    private Boolean request_driver = false;
    private GeoQueryEventListener g;
    private RelativeLayout DriverInfo;
    private TextView DriverName;
    private TextView DriverPhone;
    private TextView DriverPlate;
    private ImageView img_msg, img_call;
    private CircleImageView DriverProfileImage;
    private float distance, distance2, distance3, current_fare, fare;
    private Marker driver_marker;
    private DatabaseReference driver_location_ref;
    private ValueEventListener driver_location_listener_ref;
    private LocationRequest mLocationRequest;
    private EditText cancel_reaon;
    private CountDownTimer countDownTimer;
    private long timeCountInMilliSeconds = 21000;
    private int ride_type = 0;
    private boolean avail_driver = false;
    private SupportMapFragment mapFragment;
    final int LOCATION_REQUEST_CODE = 1;
    public static TextView txtmsgcount;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getBaseContext(),
                MyService.class));
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_customer_map2);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        getUserInformation();
        //Declarations
        msgcount = 0;
        txtmsgcount = findViewById(R.id.msgcount);

        polylines = new ArrayList<>();
        radius = 0.1;
        final int peekHeightPx = getResources().getDimensionPixelSize(R.dimen.peek_height);
        img_msg = findViewById(R.id.ms);
        DriverInfo = findViewById(R.id.rel1);
        DriverName = findViewById(R.id.driver_name);
        DriverPhone = findViewById(R.id.driver_phone);
        DriverPlate = findViewById(R.id.driver_plate);
        DriverProfileImage = findViewById(R.id.profile_image_driver);
        txtfare = findViewById(R.id.fare);
        txt_destination = findViewById(R.id.txt_destination);
        txt_pickup_loc = findViewById(R.id.pickuptext);
        imgLoc = findViewById(R.id.btn_loc);
        imgReset = findViewById(R.id.btnrefresh);
        img_msg.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMapActivity.this, Msgs.class);
            startActivity(intent);
            txtmsgcount.setVisibility(View.GONE);
            txtmsgcount.setText("");
            msgcount = 0;

        });

        imgReset.setOnClickListener(v -> {
            txt_destination.setText("");
            Common_Variables.customer_destination = null;
            Common_Variables.setroute=0;
            if (navigationMapRoute != null) {
                navigationMapRoute.removeRoute();
            }
            if (PICKUP_ICON != null) {
                PICKUP_ICON.remove();
            }
            if (DESTINATION_ICON != null) {
                DESTINATION_ICON.remove();
            }


        });

        imgLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupLocation = new com.mapbox.mapboxsdk.geometry.LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Geocoder geocoder = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
                String address = "";
                //    String city="";
                try {
                    List<Address> addresses = geocoder.getFromLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude(), 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                txt_pickup_loc.setText(address);
            }
        });

        final RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(17.897320, 120.468656),
                new LatLng(18.575787, 120.976274));
        //dummy lat/lng
        navigations();
        View header = navigationView.getHeaderView(0);
        txtfullname = header.findViewById(R.id.fullname);
        userImage = header.findViewById(R.id.userImage);
        //bottomsheet
        bottomSheet = findViewById(R.id.bottomsheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == 4) {
                    mBottomSheetBehavior.setPeekHeight(peekHeightPx);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        });
        mBottomSheetBehavior.setPeekHeight(peekHeightPx);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        txt_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           /*     List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(bounds)
                        .build(CustomerMapActivity.this);
                startActivityForResult(intent, 1); */
                // gotoPickActivity3();
                // goToPickerActivity();
                startActivity(new Intent(CustomerMapActivity.this, ActivityPlaces_S.class));

            }
        });
        txt_pickup_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(bounds)
                        .build(CustomerMapActivity.this);
                startActivityForResult(intent, 2);*/
                //  startActivity(new Intent(CustomerMapActivity.this, Place_Pickup.class));
            }
        });
        Common_Variables.customer_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //    txtmsg = findViewById(R.id.txtmsg);
        btncall = findViewById(R.id.btncall);
        Common_Variables.customer_destination = null;

        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common_Variables.customer_destination == null) {
                    //  startActivity(new Intent(CustomerMapActivity.this, ActivityPlaces_S.class));
                    gotoPickActivity3();
                } else {
                    imgLoc.setVisibility(View.GONE);
                    imgReset.setVisibility(View.GONE);
                    ride();
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        //  check_customer_request();

    }


    private void navigations() {
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.profile:
                        startActivity(new Intent(CustomerMapActivity.this, settings2.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.histories:
                        startActivity(new Intent(CustomerMapActivity.this, Activity_Histories.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.announcement:
                        startActivity(new Intent(CustomerMapActivity.this, Announcement.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        new SweetAlertDialog(CustomerMapActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Log out")
                                .setContentText("You sure you want to log out?")
                                .setConfirmText("Logout")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        FirebaseAuth.getInstance().signOut();
                                        //  LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, CustomerMapActivity.this);
                                        finish();
                                        startActivity(new Intent(CustomerMapActivity.this, SplashScreen.class));
                                        drawerLayout.closeDrawers();
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
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private com.mapbox.mapboxsdk.annotations.Marker DESTINATION_ICON;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mBottomSheetBehavior.setState(mBottomSheetBehavior.STATE_EXPANDED);
                Place place = Autocomplete.getPlaceFromIntent(data);

                Geocoder geocoder = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
                String address = "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                txt_destination.setText(address);
                //Common_Variables.customer_destination = place.getLatLng();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(CustomerMapActivity.this, status.getStatusMessage(), Toast.LENGTH_LONG);
            } else if (resultCode == RESULT_CANCELED) {
            }
            return;
        }

        if (requestCode == 5) {
            double lat = 0, lng = 0;
            com.mapbox.mapboxsdk.geometry.LatLng LATLNG = null;
            if (resultCode == RESULT_OK) {
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(
                                    new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                        }
                        lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                        lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                        LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);

                        Location loc1 = new Location("");
                        loc1.setLatitude(mLastLocation.getLatitude());
                        loc1.setLongitude(mLastLocation.getLongitude());

                        Location loc2 = new Location("");
                        loc2.setLatitude(lat);
                        loc2.setLongitude(lng);

                        if ((loc1.distanceTo(loc2) / 1000) <= 50) {

                            //  goToPickerActivity(LATLNG);

                            Point origin = Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                            Point mapdestination = Point.fromLngLat(lng, lat);
                            GeoJsonSource source2 = mapboxMap.getStyle().getSourceAs("destination-source-id");
                            if (source2 != null) {
                                source2.setGeoJson(Feature.fromGeometry(mapdestination));
                            }
                            getRoute(origin, mapdestination);
                            IconFactory iconFactory = IconFactory.getInstance(CustomerMapActivity.this);
                            Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(CustomerMapActivity.this.getResources(), R.drawable.destination2));
                            Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                            com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(LATLNG);
                            markerOptions.icon(newIcon);
                            markerOptions.title("Passenger Destination");
                            markerOptions.setSnippet("Destination");
                            if (DESTINATION_ICON != null) {
                                DESTINATION_ICON.remove();
                            }
                            DESTINATION_ICON = mapboxMap.addMarker(markerOptions);
                            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 15));
                            Geocoder geocoder = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
                            String address = "";
                            try {
                                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                                address = addresses.get(0).getAddressLine(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            txt_destination.setText(address);
                            Common_Variables.customer_destination = LATLNG;
                        } else {
                            Toast.makeText(this, "Exceeding distance Limit", Toast.LENGTH_SHORT).show();
                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            }
                            txt_destination.setText("");
                            Common_Variables.customer_destination = null;
                        }
                    }
                }

            }
        }


        if (requestCode == 6) {
            double lat = 0, lng = 0;
            com.mapbox.mapboxsdk.geometry.LatLng LATLNG = null;
            if (resultCode == RESULT_OK) {
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(
                                    new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                        }
                        lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                        lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                        LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);

                        //   goToPickerActivity(LATLNG);

                        Point origin = Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                        Point mapdestination = Point.fromLngLat(lng, lat);
                        GeoJsonSource source2 = mapboxMap.getStyle().getSourceAs("destination-source-id");
                        if (source2 != null) {
                            source2.setGeoJson(Feature.fromGeometry(mapdestination));
                        }
                        getRoute(origin, mapdestination);
                        IconFactory iconFactory = IconFactory.getInstance(CustomerMapActivity.this);
                        Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(CustomerMapActivity.this.getResources(), R.drawable.destination2));
                        Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                        com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(LATLNG);
                        markerOptions.icon(newIcon);
                        markerOptions.title("Passenger Destination");
                        markerOptions.setSnippet("Destination");
                        if (DESTINATION_ICON != null) {
                            DESTINATION_ICON.remove();
                        }
                        DESTINATION_ICON = mapboxMap.addMarker(markerOptions);
                    }
                }
                Geocoder geocoder = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
                String address = "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                txt_destination.setText(address);
                Common_Variables.customer_destination = LATLNG;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driver_id_found).child("Customer Request").child("CustomerRideID");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                    txt_destination.setText("");
                    txt_pickup_loc.setText("");
                    Common_Variables.customer_destination = null;
                    //  erasePolylines();
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide() {
        //  imgLoc.setVisibility(View.VISIBLE);
        imgReset.setVisibility(View.VISIBLE);
        request_driver = false;

        if (driver_location_listener_ref != null) {
            driver_location_ref.removeEventListener(driver_location_listener_ref);
        }
//        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (!driver_id_found.equals("")) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                    .child(driver_id_found).child("Customer Request");
            driverRef.removeValue();

            DatabaseReference current_driver = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                    .child(driver_id_found).child("Current Driver");
            current_driver.removeValue();
            driver_id_found = "";
            if (PICKUP_ICON != null) {
                PICKUP_ICON.remove();
            }
            com.mapbox.mapboxsdk.geometry.LatLng ll = new com.mapbox.mapboxsdk.geometry.LatLng(18.5924, 121.0261);
            if (DRIVER_ICON != null) {
                DRIVER_ICON.setLatLng(ll);
                symbolManager.update(DRIVER_ICON);
                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                }
                mapboxMap.clear();
            }
            if (DESTINATION_ICON != null) {
                DESTINATION_ICON.remove();
            }

        }
        driver_found = false;
        radius = 0.1;

        //
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Customer Requests");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(Common_Variables.customer_id, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

        if (PICKUP_ICON != null) {
            PICKUP_ICON.remove();
        }
        com.mapbox.mapboxsdk.geometry.LatLng ll = new com.mapbox.mapboxsdk.geometry.LatLng(18.5924, 121.0261);
        if (DRIVER_ICON != null) {
            DRIVER_ICON.setLatLng(ll);
            symbolManager.update(DRIVER_ICON);
            if (navigationMapRoute != null) {
                navigationMapRoute.removeRoute();
            }

            mapboxMap.clear();
        }

        btncall.setVisibility(View.VISIBLE);
        btncall.setText("FIND TRICYCLE");
        //  btncall.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_search, 0, 0);
        btncall.setBackgroundResource(R.drawable.custom_button);
        DriverInfo.setVisibility(View.GONE);

        DriverName.setText("");
        DriverPhone.setText("");
        DriverPlate.setText("Destination: --");
        txtfare.setText("0.00");
        DriverProfileImage.setImageResource(R.drawable.ic_person_black_24dp);
    }

    private int half_sec = 0;

    private void findDriver() {

        final DatabaseReference drivers_location = Common_Variables.drivers_available;
        geoFire = new GeoFire(drivers_location);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(g = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if (!driver_found && request_driver) {
                    //  checkMsgs();
                    driver_found=true;
                    DatabaseReference check_drivers_available = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
                    check_drivers_available.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                geoQuery.removeAllListeners();
                                driver_found = true;
                                driver_id_found = key;
                                Common_Variables.drivers_id = driver_id_found;
                                Common_Variables.drivers_id = key;
                                DatabaseReference save_customer_reuest_to_driver = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Drivers").child(driver_id_found).child("Customer Request");
                                HashMap map = new HashMap();
                                map.put("CustomerRideID", Common_Variables.customer_id);
                                // get address
                                Geocoder geocoder = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
                                String address = "";
                                //    String city="";
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(Common_Variables.customer_destination.getLatitude(),
                                            Common_Variables.customer_destination.getLongitude(), 1);
                                    address = addresses.get(0).getAddressLine(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                map.put("destination", address);
                                map.put("destinationLat", Common_Variables.customer_destination.getLatitude());
                                map.put("destinationLng", Common_Variables.customer_destination.getLongitude());
                                save_customer_reuest_to_driver.updateChildren(map);

                                IconFactory iconFactory = IconFactory.getInstance(CustomerMapActivity.this);
                                Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(CustomerMapActivity.this.getResources(), R.drawable.destination2));
                                Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                                com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(Common_Variables.customer_destination);
                                markerOptions.icon(newIcon);
                                markerOptions.title("Passenger Destination");
                                markerOptions.setSnippet("Destination");
                                DESTINATION_ICON = mapboxMap.addMarker(markerOptions);
                                //    DESTINATION_ICON = mapboxMap.addMarker(new MarkerOptions()
                                //           .position(new com.mapbox.mapboxsdk.geometry.LatLng(Common_Variables.customer_destination.getLatitude(), Common_Variables.customer_destination.getLongitude()))
                                //        .title("Passenger Pickup Location"));

                                driver_location();
                                getDriverInfo();
                                getHasRideEnded();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driver_found && half_sec < 40) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (radius > 4) {
                        g = null;
                        radius = 0.1;
                        //   break;
                    }
                    half_sec++;
                    radius=radius+0.1;
                    findDriver();
                   // Log.e("radius", String.valueOf(radius));
                } else {
                    radius = 0.1;
                    driver_found=true;
                    half_sec = 0;
                    g = null;
                   // Log.e("Toast", String.valueOf(half_sec));
                  //  Toast.makeText(getBaseContext(), "No Driver Found!.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private String dfname = "", dlname = "";

    private void getDriverInfo() {
        msgcount = 0;
        checkMsgs();
        // mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        DriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_id_found);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("lastname") != null) {
                        dlname = map.get("lastname").toString();
                    }
                    if (map.get("firstname") != null) {
                        dfname = map.get("firstname").toString();
                    }
                    if (map.get("phone") != null) {
                        DriverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("plate_no") != null) {
                        DriverPlate.setText(map.get("plate_no").toString());
                    }
                    if (map.get("image") != null) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get()
                                .load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(DriverProfileImage);

                    }
                    //    txtmsg.setText("Driver Found.");
                    DriverName.setText(dfname + " " + dlname);
                    playNotification();
                    phone_vibrate();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void playNotification() {
        if (notificationPlayer == null) {
            notificationPlayer = MediaPlayer.create(this, R.raw.notification);
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

    com.mapbox.mapboxsdk.geometry.LatLng driverLatLng = null;
    int driver_action = 0;
    public static String final_distance = "";
    private AlertDialog noftiDiaglog;

    private void driver_location() {

        driver_location_ref = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
                .child(driver_id_found).child("l");
        driver_location_listener_ref = driver_location_ref.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && request_driver) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    float bearing = 0;
                    getFare();
                    //  Log.e("MF",String.valueOf(mf));
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    if (map.get(2) != null) {
                        driver_action = Integer.parseInt(map.get(2).toString());
                    }
                    if (map.get(3) != null) {
                        bearing = Float.parseFloat(map.get(3).toString());
                    }
                    driverLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(locationLat, locationLng);
                    set_marker_driver(driverLatLng, bearing);

                    Location loc1 = new Location("");
                    if (pickupLocation != null) {
                        loc1.setLatitude(pickupLocation.getLatitude());
                        loc1.setLongitude(pickupLocation.getLongitude());
                    }
                    Location loc2 = new Location("");
                    if (driverLatLng != null) {
                        loc2.setLatitude(driverLatLng.getLatitude());
                        loc2.setLongitude(driverLatLng.getLongitude());
                    }
                    Location loc3 = new Location("");
                    if (Common_Variables.customer_destination != null) {
                        loc3.setLatitude(Common_Variables.customer_destination.getLatitude());
                        loc3.setLongitude(Common_Variables.customer_destination.getLongitude());
                    }

                    distance = loc1.distanceTo(loc2); //  driver to pick up location
                    distance2 = loc2.distanceTo(loc3); // driver to destination
                    distance3 = loc1.distanceTo(loc3); // pick up  to destination
                    current_fare = loc2.distanceTo(loc1); // current fare


                    if (driver_Arrived == 2) {
                        fare = mf + ((current_fare / 1000) - 1) * af;
                        if (fare < mf) {
                            fare = mf;
                        }
                        String strfare = String.format("%.2f", fare);
                        txtfare.setText(strfare);
                    }

                    // not yet in the customer
                    if (distance > 100 && driver_Arrived == 0 && driver_action == 1) {
                        driver_Arrived = 1;

                        update_driver_arrive(driver_Arrived);
                        set_marker_driver(driverLatLng, bearing);

                        Point mapdestination = Point.fromLngLat(Common_Variables.customer_pickuploaction.getLongitude(), Common_Variables.customer_pickuploaction.getLatitude());
                        Point origin = Point.fromLngLat(driverLatLng.getLongitude(), driverLatLng.getLatitude());
                        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                        if (source != null) {
                            source.setGeoJson(Feature.fromGeometry(mapdestination));
                        }
                        getRoute(origin, mapdestination);

                        noftiDiaglog = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setIcon(R.drawable.driver_icon)
                                .setTitle("Driver Found!.")
                                .setMessage("The Driver Accepts your request." +
                                        "\nDriver Distance:" + String.format("%.2f", distance / 1000) + "km " +
                                        "\nDestination Distance: " + String.format("%.2f", distance3 / 1000) + "km")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //set what would happen when positive button is clicked
                                        com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                                                .include(new com.mapbox.mapboxsdk.geometry.LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude()))
                                                .include(new com.mapbox.mapboxsdk.geometry.LatLng(driverLatLng.getLatitude(), driverLatLng.getLongitude()))
                                                .build();
                                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds(latLngBounds, 15));
                                        noftiDiaglog.dismiss();
                                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                })
                                .show();


                    } else if (distance < 100 && driver_Arrived == 0 && driver_action == 1) {

                        driver_Arrived = 1;
                        update_driver_arrive(driver_Arrived);
                    }
//in pickup location
                    else if (distance < 100 && driver_Arrived == 1) {
                        //  updateCameraBearing();
                        driver_Arrived = 2;
                        update_driver_arrive(driver_Arrived);
                        btncall.setVisibility(View.GONE);
                        com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                                .include(new com.mapbox.mapboxsdk.geometry.LatLng(pickupLocation.getLatitude(),
                                        pickupLocation.getLongitude()))
                                .include(new com.mapbox.mapboxsdk.geometry.LatLng(Common_Variables.customer_destination.getLatitude(),
                                        Common_Variables.customer_destination.getLongitude()))
                                .build();

                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds(latLngBounds, 15));

                        Point mapdestination = Point.fromLngLat(Common_Variables.customer_destination.getLongitude(),
                                Common_Variables.customer_destination.getLatitude());
                        Point origin = Point.fromLngLat(Common_Variables.customer_pickuploaction.getLongitude(), Common_Variables.customer_pickuploaction.getLatitude());
                        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                        if (source != null) {
                            source.setGeoJson(Feature.fromGeometry(mapdestination));
                        }

                        getRoute(origin, mapdestination);
                        Common_Variables.fare = String.format("%.2f", fare);
                        playNotification();
                        phone_vibrate();
                        noftiDiaglog = new AlertDialog.Builder(CustomerMapActivity.this)
                                .setIcon(R.drawable.driver_icon)
                                .setMessage("Your Driver is Here!\nDestination Distance from Pickup location: " + String.format("%.2f", distance2 / 1000) + "km")
                                .setPositiveButton("Ok", (dialogInterface, i) ->
                                        noftiDiaglog.dismiss())
                                .show();
                    }
// in destination
                    else if (distance2 < 500 && driver_Arrived == 2 && driver_action == 3) {
                        playNotification();
                        phone_vibrate();
                        if (PICKUP_ICON != null) {
                            PICKUP_ICON.remove();

                        }

                        if (DESTINATION_ICON != null) {
                            DESTINATION_ICON.remove();
                        }

                        com.mapbox.mapboxsdk.geometry.LatLng ll = new com.mapbox.mapboxsdk.geometry.LatLng(18.5924, 121.0261);
                        if (DRIVER_ICON != null) {
                            DRIVER_ICON.setLatLng(ll);
                            symbolManager.update(DRIVER_ICON);
                        }

                        driver_Arrived = 3;
                        fare = mf + ((current_fare / 1000) - 1) * af;
                        if (fare < mf) {
                            fare = mf;
                        }
                        String strfare = String.format("%.2f", fare);
                        txtfare.setText("₱" + strfare);
                        //erasePolylines();
                        Common_Variables.fare = strfare;

                        Common_Variables.customer_pickuploaction = pickupLocation;
                        Common_Variables.drivers_id = driver_id_found;
                        final_distance = String.format("%.2f", distance3 / 1000) + " km";
                        driver_location_ref.removeEventListener(driver_location_listener_ref);
                        Intent intent = new Intent(CustomerMapActivity.this, Submit_Ratings.class);
                        startActivity(intent);
                        btncall.setVisibility(View.VISIBLE);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        }

                        Double lat = mLastLocation.getLatitude();
                        Double lng = mLastLocation.getLongitude();
                        com.mapbox.mapboxsdk.geometry.LatLng LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);
                        locationComponent.setCameraMode(CameraMode.TRACKING);
// Move map camera to the selected location
                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 15));


                    }
                    // Log.e("driver action1", String.valueOf(driver_Arrived));;
                    //Log.e("driver action2", String.valueOf(driver_action));;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private Symbol DRIVER_ICON;

    private void set_marker_driver(com.mapbox.mapboxsdk.geometry.LatLng driverLatLng, Float brng) {
        if (DRIVER_ICON == null) {
            symbolManager = new SymbolManager(mapView, mapboxMap, mapboxMap.getStyle());
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            DRIVER_ICON = symbolManager.create(new SymbolOptions()
                    .withLatLng(driverLatLng)
                    .withIconRotate(brng)
                    .withIconImage("driver-icon-id"));
        } else {
            DRIVER_ICON.setLatLng(driverLatLng);
            DRIVER_ICON.setIconRotate(brng);
            symbolManager.update(DRIVER_ICON);
        }
    }

    private com.mapbox.mapboxsdk.annotations.Marker PICKUP_ICON;

    private void setPickupLocation() {
        if (pickupLocation == null) {
            pickupLocation = new com.mapbox.mapboxsdk.geometry.LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
        Common_Variables.customer_pickuploaction = pickupLocation;
        Geocoder geocoder2 = new Geocoder(CustomerMapActivity.this, Locale.getDefault());
        String pick_address = "";

        try {
            List<Address> addresses = geocoder2.getFromLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude(), 1);
            pick_address = addresses.get(0).getAddressLine(0);
            txt_pickup_loc.setText(pick_address);

        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseReference ref = Common_Variables.customer_requests;
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(Common_Variables.customer_id,
                new GeoLocation(pickupLocation.getLatitude(),
                        pickupLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if (PICKUP_ICON != null) {
            PICKUP_ICON.remove();

        }
        IconFactory iconFactory = IconFactory.getInstance(CustomerMapActivity.this);
        Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(CustomerMapActivity.this.getResources(), R.drawable.pickupmarkers));
        Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
        com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(pickupLocation);
        markerOptions2.icon(newIcon);
        markerOptions2.title("Passenger Pickup Location");
        markerOptions2.setSnippet("Pickup Location");
        PICKUP_ICON = mapboxMap.addMarker(markerOptions2);
        //  DESTINATION_ICON = mapboxMap.addMarker(markerOptions);
        //   PICKUP_ICON = mapboxMap.addMarker(new MarkerOptions()
        //         .position(new com.mapbox.mapboxsdk.geometry.LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude()))
        //         .title("Passenger Pickup Location"));
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        ActivityCompat.requestPermissions(
                CustomerMapActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapView.getMapAsync(this);
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CustomerMapActivity.this, logincustomer.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //  mGoogleApiClient.disconnect();
        Intent intent = new Intent(CustomerMapActivity.this, logincustomer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        //  geoQuery.removeAllListeners();
        if (g != null) {
            geoQuery.removeGeoQueryEventListener(g);
            g = null;
        }
        FirebaseAuth.getInstance().signOut();
        //   LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        finish();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && driver_found) {
            // Toast.makeText(CustomerMapActivity.this,"Please Cancel The Ride First",Toast.LENGTH_LONG);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void ride() {
        if (request_driver) {
            getTID();
            cancel_reaon = new EditText(this);
            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Reason Canceling")
                    .setConfirmText("Submit")
                    .setCustomView(cancel_reaon)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            del_current_driver();
                            // erasePolylines();
                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            }
                            endRide();
                            pickupLocation = null;
                            Common_Variables.customer_pickuploaction = null;
                            Common_Variables.drivers_id = "";
                            Common_Variables.customer_destination = null;
                            txt_destination.setText("");
                            txt_pickup_loc.setText("");

                            if (g != null) {
                                geoQuery.removeGeoQueryEventListener(g);
                                g = null;
                                driver_found = true;
                                //  geoFire.removeLocation(String.valueOf(Common_Variables.drivers_working));
                                //geoFire.removeLocation(String.valueOf(Common_Variables.drivers_available));
                            }

                            //remove geofire

                            driver_found = false;
                            Common_Variables.customer_destination = null;
                            if (ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
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

        } else {
            //
            driver_Arrived = 0;
            distance = 0;
            radius = 0.1;
            //  LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, CustomerMapActivity.this);
            request_driver = true;
            setPickupLocation();
            btncall.setText("Waiting for Driver");
            btncall.setEnabled(false);
            // btncall.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_cancel, 0, 0);
            btncall.setBackgroundResource(R.drawable.custom_btn_red);
            startCountDownTimer();
            findDriver();
        }
    }

    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getTID();
                if (Common_Variables.current_time != "") {
                    countDownTimer.onFinish();
                }
            }

            @Override
            public void onFinish() {
                btncall.setEnabled(true);
                btncall.setText("Cancel Ride");
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

        }.start();
        //  countDownTimer.start();
    }

    private void del_current_driver() {
        if (Common_Variables.current_time != "") {
            Update_transaction();
            Common_Variables.current_time = "";
        }
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Current Driver");
        driverRef.removeValue();
    }

    private void getTID() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Current Driver");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("TID") != null) {
                        Common_Variables.current_time = map.get("TID").toString();
                        // tid.setText(Common_Variables.current_time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void update_driver_arrive(int d) {
        DatabaseReference Driver_Arrive = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(driver_id_found)
                .child("Customer Request");
        HashMap map = new HashMap();
        map.put("driver_arrive", d);
        Driver_Arrive.updateChildren(map);
    }

    private void Update_transaction() {
        update_transaction = FirebaseDatabase.getInstance().getReference()
                .child("Transactions").child(Common_Variables.current_time);
        HashMap map = new HashMap();
        map.put("Fare", "0");
        map.put("Type", "Canceled by the Passenger");
        map.put("Reason", cancel_reaon.getText().toString());
        update_transaction.updateChildren(map);
    }


    private void phone_vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
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

    //msgs
    public static int msgcount = 0;

    private void checkMsgs() {
        DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                .child(Common_Variables.drivers_id)
                // .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
                .child("Messages");

        databaseReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    msgcount = 0;
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        if (postSnapShot.getKey().equals("CID")) {
                            String value1 = dataSnapshot.child("CID").getValue().toString();
                            if (value1.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                String seen = "";
                                String messageD = "";

                                if (dataSnapshot.child("messageD").exists()) {
                                    messageD = dataSnapshot.child("messageD").getValue().toString();
                                    if (dataSnapshot.child("seen").exists()) {
                                        seen = dataSnapshot.child("seen").getValue().toString();
                                        if (seen.equals("False") && !messageD.equals("")) {
                                            //  Intent intent = new Intent(CustomerMapActivity.this, Msgs.class);
                                            //  startActivity(intent);
                                            msgcount = msgcount + 1;
                                            Log.e("MSG", String.valueOf(msgcount));
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

    private CircleImageView userImage;
    private TextView txtfullname;

    private void getUserInformation() {
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            String ln, fn, mn;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    if (dataSnapshot.hasChild("first_name")) {
                        fn = dataSnapshot.child("first_name").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("last_name")) {
                        ln = dataSnapshot.child("last_name").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(userImage);
                        //   Log.e("image", image);
                    }
                    txtfullname.setText(fn + " " + ln);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void drawNewRoute(com.mapbox.mapboxsdk.geometry.LatLng origins, com.mapbox.mapboxsdk.geometry.LatLng destination) {
        com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                .include(new com.mapbox.mapboxsdk.geometry.LatLng(origins.getLatitude(), origins.getLongitude()))
                .include(new com.mapbox.mapboxsdk.geometry.LatLng(destination.getLatitude(), destination.getLongitude()))
                .build();
        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds(latLngBounds, 48));

        Point mapdestination = Point.fromLngLat(origins.getLongitude(), origins.getLatitude());
        Point origin = Point.fromLngLat(destination.getLongitude(), destination.getLatitude());
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(mapdestination));
        }
        getRoute(origin, mapdestination);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    private static final String MAKI_ICON_CAFE = "cafe-15";
    private static final String MAKI_ICON_HARBOR = "harbor-15";
    private static final String MAKI_ICON_AIRPORT = "airport-15";
    private SymbolManager symbolManager;
    private Symbol symbol;
    MarkerViewManager markerViewManager = null;

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        CustomerMapActivity.this.mapboxMap = mapboxMap;

        //    markerViewManager = new MarkerViewManager(mapView, mapboxMap);
        if (mapboxMap.getStyle() == null) {
            mapboxMap.setStyle(Style.MAPBOX_STREETS,
                    style -> {
                        enableLocationComponent(style);
                        addDriverIconSymbol(style);
                    });
        }
    }

    private float bearingMarker = 0;
    private SymbolLayer driverIconSymbolSymbolLayer = null;

    private void addDriverIconSymbol(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("driver-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_nav));

        GeoJsonSource geoJsonSource = new GeoJsonSource("driver-source-id");
        loadedMapStyle.addSource(geoJsonSource);

        driverIconSymbolSymbolLayer = new SymbolLayer("driver-symbol-layer-id", "driver-source-id");
        driverIconSymbolSymbolLayer.withProperties(
                iconImage("driver-icon-id"),
                iconAllowOverlap(true),
                iconRotate(bearingMarker),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(driverIconSymbolSymbolLayer);
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

        private final WeakReference<CustomerMapActivity> activityWeakReference;

        MainActivityLocationCallback(CustomerMapActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            CustomerMapActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

// Create a Toast which displays the new location's coordinates
                if (getApplicationContext() != null) {
                    mLastLocation = location;
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //must set
                    pickupLocation = new com.mapbox.mapboxsdk.geometry.LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    if (!driver_id_found.equals("")) {
                        // locationComponent.setCameraMode(CameraMode.TRACKING);

                        driver_location();
                    } else {
                        Double lat = mLastLocation.getLatitude();
                        Double lng = mLastLocation.getLongitude();
                        com.mapbox.mapboxsdk.geometry.LatLng LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);
                        if (Common_Variables.customer_destination == null) {
                            //  mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 48));
                            if(navigationMapRoute!=null)
                            {
                                navigationMapRoute.removeRoute();
                            }
                            Common_Variables.setroute = 0;
                            locationComponent.setCameraMode(CameraMode.TRACKING);
                            btncall.setText("FIND TRICYCLE");
                        } else {
                            com.mapbox.mapboxsdk.geometry.LatLngBounds latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                                    .include(new com.mapbox.mapboxsdk.geometry.LatLng(Common_Variables.customer_destination.getLatitude(),
                                            Common_Variables.customer_destination.getLongitude()))
                                    .include(new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng))
                                    .build();
                            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds(latLngBounds, 48));
                            if (Common_Variables.setroute == 1) {
                                Common_Variables.setroute = 0;

                                Point origin = Point.fromLngLat(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                                Point mapdestination = Point.fromLngLat(Common_Variables.customer_destination.getLongitude(), Common_Variables.customer_destination.getLatitude());
                                GeoJsonSource source2 = mapboxMap.getStyle().getSourceAs("destination-source-id");
                                if (source2 != null) {
                                    source2.setGeoJson(Feature.fromGeometry(mapdestination));
                                }
                                getRoute(origin,mapdestination);
                                IconFactory iconFactory = IconFactory.getInstance(CustomerMapActivity.this);
                                Icon icon = iconFactory.fromBitmap(BitmapFactory.decodeResource(CustomerMapActivity.this.getResources(), R.drawable.destination2));
                                Icon newIcon = IconFactory.recreate(icon.getId(), Bitmap.createScaledBitmap(icon.getBitmap(), 70, 70, false));
                                com.mapbox.mapboxsdk.annotations.MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new com.mapbox.mapboxsdk.geometry.LatLng(Common_Variables.customer_destination.getLatitude(), Common_Variables.customer_destination.getLongitude()));
                                markerOptions.icon(newIcon);
                                markerOptions.title("Passenger Destination");
                                markerOptions.setSnippet("Destination");
                                if (DESTINATION_ICON != null) {
                                    DESTINATION_ICON.remove();
                                }
                                DESTINATION_ICON = mapboxMap.addMarker(markerOptions);
                            }

                        }

// Move map camera to the selected location


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
            CustomerMapActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        Log.e("checking error", "Stop");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mapView.isActivated()) {
            mapView.onStart();

        }
        Log.e("checking error", "Start");
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.e("checking error", "resume");
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("checking error", "pause");
        mapView.onPause();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("checking error", "low memory");
        mapView.onLowMemory();

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    public void getRoute(Point origin, Point destination) {
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

    private void gotoPickActivity2() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken())
                .build(CustomerMapActivity.this);
        startActivityForResult(intent, 5);
    }


    private void goToPickerActivity(com.mapbox.mapboxsdk.geometry.LatLng latLng) {
        startActivityForResult(
                new PlacePicker.IntentBuilder()
                        .accessToken(getString(R.string.access_token))
                        .placeOptions(PlacePickerOptions.builder()
                                .statingCameraPosition(new CameraPosition.Builder()
                                        .target(new com.mapbox.mapboxsdk.geometry.LatLng(latLng.getLatitude(), latLng.getLongitude())).zoom(16).build())
                                .build())
                        .build(this), 6);
    }

    private void gotoPickActivity3() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken("pk.eyJ1IjoibGhlc3RlcjA4IiwiYSI6ImNrbmd2MjNqMTAwb24yc3QxOGVxNjJiMDAifQ.lEm25NigjnVbNSO8kUQuiQ")
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .country("PH")
                        //.bbox(17.861711,17.885563, 18.160851, 18.62972 )
                        //.addInjectedFeature(home)
                        //.addInjectedFeature(work)
                        .build(PlaceOptions.MODE_CARDS))
                .build(CustomerMapActivity.this);
        startActivityForResult(intent, 5);
    }

}
