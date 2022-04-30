package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewRide extends AppCompatActivity {
    private TextView dname, contact, sidecarnumber, dt, tm, stats;
    private float distances = 0;
    private float distance2 = 0;
    private float distance3 = 0;
    private float current_fare = 0;
    private final float fare = 0;
    private ScrollView scrollView;
    public static TextView fr, to, fares, distance;
    DatabaseReference databaseReference;
    LinearLayout schedlayout, first, second, third, inRide;
    private CheckBox sched_chk;
    Button btncancelRide, btnSave;
    public static Activity fa;
    TextView txtmsgcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ride);
        fa = this;
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        scrollView = findViewById(R.id.myview);
        dname = findViewById(R.id.dname);
        contact = findViewById(R.id.phone);
        fr = findViewById(R.id.frm);
        to = findViewById(R.id.to);
        dt = findViewById(R.id.dt);
        tm = findViewById(R.id.tm);
        fares = findViewById(R.id.fare);
        distance = findViewById(R.id.distance);
        sidecarnumber = findViewById(R.id.plate_no);
        sched_chk = findViewById(R.id.checkBox);
        schedlayout = findViewById(R.id.setSched);
        first = findViewById(R.id.first);
        second = findViewById(R.id.second);
        stats = findViewById(R.id.status);
        third = findViewById(R.id.third);
        btncancelRide = findViewById(R.id.btncancelRide);
        btnSave = findViewById(R.id.button5);
        inRide = findViewById(R.id.inRides);
        txtmsgcount=findViewById(R.id.msgcount);

        findViewById(R.id.idmsg).setOnClickListener(v -> {

            startActivity(new Intent(NewRide.this,Msgs.class));
            txtmsgcount.setVisibility(View.GONE);
            txtmsgcount.setText("");
            msgcount = 0;
        });
        scrollView.setVisibility(View.VISIBLE);
        inRide.setVisibility(View.GONE);
        first.setVisibility(View.VISIBLE);
        second.setVisibility(View.GONE);
        third.setVisibility(View.GONE);
        stats.setText("REQUEST DRIVER");

        sched_chk.setOnClickListener(v -> {
            if (sched_chk.isChecked()) {
                schedlayout.setVisibility(View.VISIBLE);
                btnSave.setText("SAVE");
            } else {
                schedlayout.setVisibility(View.GONE);
                btnSave.setText("CALL TRICYCLE");
            }
        });
        if (b != null) {
            dname.setText((String) b.get("DNAME"));
            //contact.setText((String) b.get("plate"));
            sidecarnumber.setText((String) b.get("plate"));
            Common_Variables.newRideDriverID = (String) b.get("DID");
            Log.e("newrideID", Common_Variables.newRideDriverID);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                    .child(Common_Variables.newRideDriverID);
            getUserInformation();

        }

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common_Variables.customer_destination = null;
                Common_Variables.customer_pickuploaction = null;
                Common_Variables.current_time="";
                finish();
            }
        });
        findViewById(R.id.button4).setOnClickListener(v -> {
            Common_Variables.customer_destination = null;
            Common_Variables.customer_pickuploaction = null;
            Common_Variables.current_time="";
            finish();
        });
        findViewById(R.id.button5).setOnClickListener(v -> {


            new SweetAlertDialog(NewRide.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Confirm Ride")
                    .setContentText("Confirm Ride Information")
                    .setConfirmText("Request")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {


                            if (sched_chk.isChecked()) {
                                if(fr.getText().toString()!="" && to.getText().toString()!=""
                                        && dt.getText().toString()!="" && tm.getText().toString()!="") {
                                    scheduleRide();
                                    //customerSched(newID);
                                    Common_Variables.customer_destination = null;
                                    Common_Variables.customer_pickuploaction = null;
                                    Common_Variables.current_time = "";
                                    finish();
                                }
                                else{
                                    Toast.makeText(NewRide.this,"Please Complete the form",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                if(fr.getText().toString()!="" && to.getText().toString()!="") {
                                    CustomerRequestToDriver();
                                    CustomerRequest();
                                    driver_location();
                                    getHasRideEnded();
                                    scrollView.setVisibility(View.GONE);
                                    inRide.setVisibility(View.VISIBLE);
                                }
                                else{
                                    Toast.makeText(NewRide.this,"Please Complete the form",Toast.LENGTH_SHORT).show();
                                }
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


        });

        findViewById(R.id.to).setOnClickListener(v -> {
            startActivity(new Intent(NewRide.this, ActivityPlaces_S.class));

        });

        findViewById(R.id.frm).setOnClickListener(v -> {
            startActivity(new Intent(NewRide.this, Place_Pickup.class));

        });
        findViewById(R.id.tm).setOnClickListener(v -> {
            showHourPicker();
        });
        findViewById(R.id.dt).setOnClickListener(v -> {
            DatePickerDialog datePickerDialog;
            int year;
            int month;
            int dayOfMonth;
            Calendar calendar;
            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(NewRide.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            dt.setText(year + "-" + String.format ("%02d",(month + 1)) + "-" +  String.format ("%02d",day));
                        }
                    }, year, month, dayOfMonth);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();

        });
        checkMsgs();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Toast.makeText(CustomerMapActivity.this,"Please Cancel The Ride First",Toast.LENGTH_LONG);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void CustomerRequest() {
        DatabaseReference CustomerRequest = Common_Variables.customer_requests
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("l");

        HashMap<String, Object> req = new HashMap<>();
        req.put("0", Common_Variables.customer_pickuploaction.getLatitude());
        req.put("1", Common_Variables.customer_pickuploaction.getLongitude());
        CustomerRequest.updateChildren(req);
    }

    private String getAddress(LatLng latLng) {
        String address = "";
        Geocoder geocoder = new Geocoder(NewRide.this, Locale.getDefault());

        //    String city="";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(),
                    latLng.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void CustomerRequestToDriver() {
        DatabaseReference save_customer_reuest_to_driver = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(Common_Variables.newRideDriverID).child("Customer Request");
        HashMap map = new HashMap();
        map.put("CustomerRideID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        // get address

        map.put("destination", getAddress(Common_Variables.customer_destination));
        map.put("pickup", getAddress(Common_Variables.customer_pickuploaction));
        map.put("destinationLat", Common_Variables.customer_destination.getLatitude());
        map.put("destinationLng", Common_Variables.customer_destination.getLongitude());
        save_customer_reuest_to_driver.updateChildren(map);

    }

    private void customerSched(String newID) {
        DatabaseReference schedrefcustomer = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Schedules")
                .child(newID);

        HashMap<String, Object> userMap3 = new HashMap<>();
        userMap3.put("DID", Common_Variables.newRideDriverID);
        userMap3.put("TYPE", "Schedule");
        userMap3.put("TID", newID);
        schedrefcustomer.updateChildren(userMap3);
        finish();
    }

    private void scheduleRide() {

        String type = "Schedule", newID = "";
        DatabaseReference schedrefdriver = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(Common_Variables.newRideDriverID).child("Schedules").push();

        newID = schedrefdriver.getKey();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("CID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        userMap.put("TYPE", "Schedule");
        userMap.put("TID", newID);
        schedrefdriver.setValue(userMap);

        customerSched(newID);

        DatabaseReference schedref = FirebaseDatabase.getInstance().getReference().child("Transactions").child(newID);
        HashMap<String, Object> userMap2 = new HashMap<>();
        userMap2.put("CID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        userMap2.put("DID", Common_Variables.newRideDriverID);
        userMap2.put("pickupLat", Common_Variables.customer_pickuploaction.getLatitude());
        userMap2.put("pickupLong", Common_Variables.customer_pickuploaction.getLongitude());
        userMap2.put("destinationLat", Common_Variables.customer_destination.getLatitude());
        userMap2.put("destinationLong", Common_Variables.customer_destination.getLongitude());
        userMap2.put("pickupAddress", fr.getText().toString());
        userMap2.put("destinationAddress", to.getText().toString());
        userMap2.put("Date", dt.getText().toString());
        userMap2.put("Time", hh + ":" + mm + " " + am_pm);
        userMap2.put("Distance", distance.getText().toString());
        userMap2.put("Fare", fares.getText().toString());
        userMap2.put("Type", type);
        userMap2.put("fd", dt.getText().toString() + " - " + hh + ":" + mm + " " + am_pm);
        userMap2.put("tid", newID);
        schedref.updateChildren(userMap2);
    }

    public static float computeDistance(com.mapbox.mapboxsdk.geometry.LatLng locs1, com.mapbox.mapboxsdk.geometry.LatLng locs2) {
        float d = 0;
        Location loc1 = new Location("");
        loc1.setLatitude(locs1.getLatitude());
        loc1.setLongitude(locs1.getLongitude());

        Location loc2 = new Location("");
        loc2.setLatitude(locs2.getLatitude());
        loc2.setLongitude(locs2.getLongitude());

        d = (loc1.distanceTo(loc2)) / 1000;
        return d;

    }

    private void getUserInformation() {
        //databaseReference.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //  if (dataSnapshot.hasChild("phone")) {
                    String phone_num = dataSnapshot.child("phone").getValue().toString();
                    contact.setText(phone_num);
                    //    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int hh = 0, mm = 0;
    private String am_pm = "";

    private void showHourPicker() {
        final Calendar myCalender = Calendar.getInstance();
        int hour = myCalender.get(Calendar.HOUR_OF_DAY);
        int minute = myCalender.get(Calendar.MINUTE);
        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                if (view.isShown()) {
                    myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);
                    String AM_PM;
                    if (hourOfDay < 12) {
                        AM_PM = "AM";
                    } else {
                        AM_PM = "PM";
                    }
                    if (hourOfDay == 0) {
                        hourOfDay = 12;
                    }
                    hh = hourOfDay;
                    if (hourOfDay > 12) {
                        hourOfDay = hourOfDay - 12;
                    }
                    am_pm = AM_PM;
                    mm = minute;
                    tm.setText(hourOfDay + ":" + minute + ":" + AM_PM);
                }
            }

        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(NewRide.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, false);
        timePickerDialog.setTitle("Choose hour:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    com.mapbox.mapboxsdk.geometry.LatLng driverLatLng = null;
    int driver_action = 0;
    public static String final_distance = "";
    private AlertDialog noftiDiaglog;
    public static int driver_Arrived;
    ValueEventListener driver_location_listener_ref;
    private DatabaseReference driver_location_ref;

    private void driver_location() {
        //if()
        driver_location_ref = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
                .child(Common_Variables.newRideDriverID).child("l");
        driver_location_listener_ref = driver_location_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    float bearing = 0;
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

                    Location loc1 = new Location("");
                    if (Common_Variables.customer_pickuploaction != null) {
                        loc1.setLatitude(Common_Variables.customer_pickuploaction.getLatitude());
                        loc1.setLongitude(Common_Variables.customer_pickuploaction.getLongitude());
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

                    distances = loc1.distanceTo(loc2); //  driver to pick up location
                    distance2 = loc2.distanceTo(loc3); // driver to destination
                    distance3 = loc1.distanceTo(loc3); // pick up  to destination


                    // not yet in the customer
                    if (distances > 100 && driver_Arrived == 0 && driver_action == 1) {
                        driver_Arrived = 1;
                        stats.setText("DRIVER ACCEPTED THE REQUEST");
                        btncancelRide.setVisibility(View.GONE);
                        first.setVisibility(View.GONE);
                        second.setVisibility(View.VISIBLE);
                        update_driver_arrive(driver_Arrived);

                     /*   noftiDiaglog = new AlertDialog.Builder(NewRide.this)
                                .setIcon(R.drawable.driver_icon)
                                .setTitle("Driver Accepts Request!.")
                                .setMessage("The Driver Accepts your request." +
                                        "\nDriver Distance:" + String.format("%.2f", distances / 1000) + "km " +
                                        "\nDestination Distance: " + String.format("%.2f", distance3 / 1000) + "km")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //set what would happen when positive button is clicked

                                        noftiDiaglog.dismiss();
                                    }
                                })
                                .show(); */


                    } else if (distances < 100 && driver_Arrived == 0 && driver_action == 1) {

                        driver_Arrived = 1;
                        update_driver_arrive(driver_Arrived);
                    }
//in pickup location
                    else if (distances < 100 && driver_Arrived == 1) {
                        second.setVisibility(View.GONE);
                        third.setVisibility(View.VISIBLE);
                        stats.setText("PICKUP TO DESTINATION");
                        //  updateCameraBearing();
                        driver_Arrived = 2;
                        update_driver_arrive(driver_Arrived);
                        playNotification();
                        phone_vibrate();
                    btncancelRide.setVisibility(View.GONE);
                     /*   noftiDiaglog = new AlertDialog.Builder(NewRide.this)
                                .setIcon(R.drawable.driver_icon)
                                .setMessage("Your Driver is Here!\nDestination Distance from Pickup location: " + String.format("%.2f", distance2 / 1000) + "km")
                                .setPositiveButton("Ok", (dialogInterface, i) ->
                                        noftiDiaglog.dismiss())
                                .show(); */
                    }
// in destination
                    else if (distance2 < 500 && driver_Arrived == 2 && driver_action == 3) {
                        playNotification();
                        phone_vibrate();
                        String strfare = String.format("%.2f", fare);
                        Common_Variables.fare = strfare;
                        final_distance = String.format("%.2f", distance3 / 1000) + " km";
                        Intent intent = new Intent(NewRide.this, RateDriver.class);
                        intent.putExtra("DID",Common_Variables.newRideDriverID);
                        first.setVisibility(View.VISIBLE);
                        second.setVisibility(View.GONE);
                        third.setVisibility(View.GONE);
                        stats.setText("REQUEST DRIVER");
                        scrollView.setVisibility(View.VISIBLE);
                        inRide.setVisibility(View.GONE);
                        startActivity(intent);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void update_driver_arrive(int d) {
        DatabaseReference Driver_Arrive = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(Common_Variables.newRideDriverID)
                .child("Customer Request");
        HashMap map = new HashMap();
        map.put("driver_arrive", d);
        Driver_Arrive.updateChildren(map);
    }

    private void phone_vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    MediaPlayer notificationPlayer;

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

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(Common_Variables.newRideDriverID).child("Customer Request").child("CustomerRideID");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                    Common_Variables.customer_destination = null;
                    Common_Variables.customer_pickuploaction = null;
                    Common_Variables.current_time="";
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
        if (driver_location_listener_ref != null) {
            driver_location_ref.removeEventListener(driver_location_listener_ref);
        }


        if (!Common_Variables.newRideDriverID.equals("")) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                    .child(Common_Variables.newRideDriverID).child("Customer Request");
            driverRef.removeValue();

            DatabaseReference current_driver = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                    .child(Common_Variables.newRideDriverID).child("Current Driver");
            current_driver.removeValue();
          //  Common_Variables.newRideDriverID = "";
            scrollView.setVisibility(View.VISIBLE);
            inRide.setVisibility(View.GONE);
                 noftiDiaglog = new AlertDialog.Builder(NewRide.this)
                                .setIcon(R.drawable.driver_icon)
                                .setTitle("Trip Status")
                                .setMessage("Ride has Ended")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //set what would happen when positive button is clicked
                                        noftiDiaglog.dismiss();
                                        finish();
                                    }
                                })
                                .show();

        }
    }

    public static int msgcount = 0;

    private void checkMsgs() {
        DatabaseReference mdatabaseReference;
        mdatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                .child(Common_Variables.newRideDriverID)
                .child("Messages");

       mdatabaseReference.addChildEventListener(new ChildEventListener() {

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
}