package com.lhester.polendey.trikila;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhester.polendey.trikila.Schedules.MySchedules;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineDrivers extends AppCompatActivity {
    private static final String TAG = "DirectionsActivity";
    LocationManager locationManager;
    FusedLocationProviderClient mFusedLocationClient;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    public static LatLng MyLocation = null;
    public static TextView txtmsgcount;
    private DatabaseReference databaseReference;
    private static DatabaseReference onlindrivers;
    private static DatabaseReference driverinfo;
    private static SwipeRefreshLayout pullToRefresh;
    private CircleImageView userImage;
    private TextView txtfullname;

    private static ListView lv;
    static ArrayList<HashMap<String, String>> lists;
    private static SimpleAdapter adapter;
    private static final int REQUEST_LOCATION = 1;
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 1;
    private final OnlineDrivers.MainActivityLocationCallback callback = new OnlineDrivers.MainActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_drivers);
        startService(new Intent(getBaseContext(),
                MyService.class));
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            initLocationEngine();
        }
        lv = findViewById(R.id.oldrivers);
        navigations();
        View header = navigationView.getHeaderView(0);
        txtfullname = header.findViewById(R.id.fullname);
        userImage = header.findViewById(R.id.userImage);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        driverinfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        onlindrivers = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        getUserInformation();

        refreshContent();
        //findDriver();


        lists = new ArrayList<>();

        adapter = new SimpleAdapter(OnlineDrivers.this, lists, R.layout.online_driver_lists,
                new String[]{"DID", "DNAME", "distance", "plate", "rating"},
                new int[]{R.id.DID, R.id.DNAME, R.id.distance, R.id.plate_no, R.id.rates});

        compareAll();
        lv.setAdapter(adapter);
        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            // TODO Auto-generated method stub
            onlindrivers.removeEventListener(childListener);
            refreshContent();
            adapter.notifyDataSetChanged();
            compareAll();
            lv.setAdapter(adapter);
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Activity_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                //   Common_Variables.newRideDriverID = (String) obj.get("DID");
                Intent intent = new Intent(OnlineDrivers.this, NewRide.class);
                intent.putExtra("dname", (String) obj.get("DID"));
                intent.putExtra("DNAME", (String) obj.get("DNAME"));
                intent.putExtra("DID", (String) obj.get("DID"));
                intent.putExtra("plate", (String) obj.get("plate"));
                startActivity(intent);
                //   startActivity(new Intent(OnlineDrivers.this, NewRide.class));
                //  Log.e("Yourtag", result);
            }
        });

        txtmsgcount = findViewById(R.id.msgcount);
        navigations();
        setMenuCounter(R.id.sched, 4);

    }

    @Override
    protected void onPause() {
        if(childListener!=null) {
            onlindrivers.removeEventListener(childListener);
        }
        super.onPause();
    }

    private static void compareAll() {
        Comparator<HashMap<String, String>> distanceComparator = (o1, o2) -> {
            // Get the distance and compare the distance.
            Double distance1 = Double.parseDouble(o1.get("distance"));
            Double distance2 = Double.parseDouble(o2.get("distance"));

            return distance1.compareTo(distance2);
        };

        Collections.sort(lists, distanceComparator);
    }

    public static void refreshContent() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                  pullToRefresh.setRefreshing(false);
                getLists();
            }
        }, 1000);

    }

    private void refreshContent2() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefresh.setRefreshing(false);
                // getLists();
            }
        }, 1000);

    }

    private void navigations() {
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.profile:
                    startActivity(new Intent(OnlineDrivers.this, settings2.class));
                    drawerLayout.closeDrawers();
                    break;
                case R.id.histories:
                    startActivity(new Intent(OnlineDrivers.this, Activity_Histories.class));
                    drawerLayout.closeDrawers();
                    break;
                case R.id.sched:
                    startActivity(new Intent(OnlineDrivers.this, MySchedules.class));
                    drawerLayout.closeDrawers();
                    break;
                case R.id.announcement:
                    startActivity(new Intent(OnlineDrivers.this, Announcement.class));
                    drawerLayout.closeDrawers();
                    break;
                case R.id.logout:
                    new SweetAlertDialog(OnlineDrivers.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Log out")
                            .setContentText("You sure you want to log out?")
                            .setConfirmText("Logout")
                            .setConfirmClickListener(sDialog -> {
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                Intent i = new Intent(OnlineDrivers.this, logincustomer.class);
                                //  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                drawerLayout.closeDrawers();
                                sDialog.dismissWithAnimation();
                            })
                            .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                            .show();
                    break;
                default:
                    return true;
            }
            return true;
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(OnlineDrivers.this, logincustomer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

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

    static ChildEventListener childListener;
    public static void getLists() {
        if (lists != null) {
            lists.clear();
            adapter.notifyDataSetChanged();
        }
        lv.setAdapter(adapter);
        childListener= onlindrivers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    double ds = 0, lng = 0, lat = 0;
                    lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                    lng = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                    Location loc1 = new Location("");
                    if (MyLocation != null) {
                        loc1.setLatitude(MyLocation.latitude);
                        loc1.setLongitude(MyLocation.longitude);

                        Location loc2 = new Location("");
                        loc2.setLatitude(lat);
                        loc2.setLongitude(lng);

                        ds = (loc1.distanceTo(loc2)) / 1000;
                        getDriversInformation(ds, dataSnapshot.getKey());
                        adapter.notifyDataSetChanged();
                        // compareAll();
                        //   Toast.makeText(OnlineDrivers.this,String.valueOf(lat),Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //   getLists();

                // compareAll();
                //   Toast.makeText(OnlineDrivers.this,String.valueOf(lat),Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //  getLists();
                //   adapter.notifyDataSetChanged();

                double ds = 0, lng = 0, lat = 0;
                lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                lng = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                Location loc1 = new Location("");
                loc1.setLatitude(MyLocation.latitude);
                loc1.setLongitude(MyLocation.longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(lat);
                loc2.setLongitude(lng);

                ds = (loc1.distanceTo(loc2)) / 1000;
                getDriversInformation2(ds, dataSnapshot.getKey());

                adapter.notifyDataSetChanged();
                // compareAll();
                //   Toast.makeText(OnlineDrivers.this,String.valueOf(lat),Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void getDriversInformation(double ds, String ID) {
        driverinfo.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            String ln, fn, mn, plate_no;
            float rbar = 0;

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ds <= 10) {
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

                        if (dataSnapshot.hasChild("plate_no")) {
                            plate_no = dataSnapshot.child("plate_no").getValue().toString();

                        }

                        if (dataSnapshot.hasChild("rating")) {
                            rbar = Float.parseFloat(dataSnapshot.child("rating").getValue().toString());

                        }

                        HashMap<String, String> datum = new HashMap<String, String>();
                        datum.put("DID", dataSnapshot.getKey());
                        datum.put("DNAME", fn + " " + mn + " " + ln);
                        datum.put("distance", String.format("%.02f", ds));
                        datum.put("plate", plate_no);
                        datum.put("rating", String.format("%.01f", rbar));
                        lists.add(datum);
                        adapter.notifyDataSetChanged();
                        compareAll();


                        //   Toast.makeText(OnlineDrivers.this,dataSnapshot.getKey().toString(),Toast.LENGTH_SHORT).show();

                        //adapter.notifyDataSetChanged();


                        //  ;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static void getDriversInformation2(double ds, String ID) {
        driverinfo.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            String ln, fn, mn, plate_no;
            float rbar = 0;

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ds <= 10) {
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

                        if (dataSnapshot.hasChild("plate_no")) {
                            plate_no = dataSnapshot.child("plate_no").getValue().toString();

                        }
                        if (dataSnapshot.hasChild("rating")) {
                            rbar = Float.parseFloat(dataSnapshot.child("rating").getValue().toString());

                        }
                        HashMap<String, String> datum = new HashMap<String, String>();
                        datum.put("DID", dataSnapshot.getKey());
                        datum.put("DNAME", fn + " " + mn + " " + ln);
                        datum.put("distance", String.format("%.02f", ds));
                        datum.put("plate", plate_no);
                        datum.put("rating", String.format("%.01f", rbar));
                        lists.remove(datum);
                        adapter.notifyDataSetChanged();


                        //   Toast.makeText(OnlineDrivers.this,dataSnapshot.getKey().toString(),Toast.LENGTH_SHORT).show();

                        //adapter.notifyDataSetChanged();


                        //  ;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable Location").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", (dialog, which) -> dialog.cancel()
        );
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(
                OnlineDrivers.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                OnlineDrivers.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double lng = locationGPS.getLongitude();
                MyLocation = new LatLng(lat, lng);

            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<OnlineDrivers> activityWeakReference;

        MainActivityLocationCallback(OnlineDrivers activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            OnlineDrivers activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }
                if (getApplicationContext() != null) {
                    //  mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MyLocation = latLng;

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
            OnlineDrivers activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
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
    protected void onStart() {
        super.onStart();
        refreshContent2();
    }

    private void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }
}
