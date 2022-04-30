package com.lhester.polendey.drivers.Schedules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhester.polendey.drivers.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MySchedules extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    DatabaseReference myschedlues, schedInfo;

    List<SchedUtils> personUtilsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedules);


        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Requested Schedule", "Accepted Schedule","Completed","Rated","Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        myStats = "Schedule";
                        personUtilsList.clear();
                        getLists();
                        break;
                    case 1:
                        myStats = "Accepted Schedule";
                        personUtilsList.clear();
                        getLists();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        myStats = "Completed";
                        personUtilsList.clear();
                        getLists();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        myStats = "Rated";
                        personUtilsList.clear();
                        getLists();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 4:
                        myStats = "Cancelled";
                        personUtilsList.clear();
                        getLists();
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myschedlues = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Schedules");

        schedInfo = FirebaseDatabase.getInstance().getReference()
                .child("Transactions");

        recyclerView = findViewById(R.id.mysched);
        recyclerView.setHasFixedSize(true);

        personUtilsList = new ArrayList<>();
        //Adding Data into ArrayList

        getLists();
        //  personUtilsList.add(new SchedUtils("Todd Miller","https://firebasestorage.googleapis.com/v0/b/trikila-laoag.appspot.com/o/Profile%20Pictures%2Fx1GdTgEXQNQTBJxKEQt8FZM7Swy2.jpg?alt=media&token=f8e65f1e-c821-4a5d-a2d6-1aa203a70f7a"));
        //    personUtilsList.add(new SchedUtils("Bradley Matthews","https://firebasestorage.googleapis.com/v0/b/trikila-laoag.appspot.com/o/Profile%20Pictures%2Fx1GdTgEXQNQTBJxKEQt8FZM7Swy2.jpg?alt=media&token=f8e65f1e-c821-4a5d-a2d6-1aa203a70f7a"));
        //   mAdapter = new CustomRecyclerAdapter(this, personUtilsList);
        // recyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CustomRecyclerAdapter(MySchedules.this, personUtilsList);
        recyclerView.setAdapter(mAdapter);
    }
    String myStats="Schedule";
    private void getLists() {
        myschedlues.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.e("KEY", dataSnapshot.getKey());
                    getSchedInfo(dataSnapshot.getKey(),myStats);
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
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    String full_name;
    String imagelink;
    String dd;
    String tt;
    String distance;
    String plate;
    String fare;
    String MCID;
    String s_from;
    String s_to;
    String status;
    long total_min = 0;

    private long getTotalMin(String d, String t) throws ParseException {
        long minutedif=0;
        try {
            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("yyyy-MM-ddHH:mm a");

            //Setting dates
            date1 = dates.parse(d + t);
            date2 = dates.parse(date_today() +time_today());
            Log.e("Schedule",d + t);
            Log.e("Today",date_today() +time_today());

            //Comparing dates
            long difference = Math.abs(date1.getTime() - date2.getTime());
            minutedif = difference / (1000 * 60);
            //Convert long to String
            Log.e("HERE", "HERE: " + minutedif);

        } catch (
                Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }
        return minutedif;
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
    private void getCustomerInfo(long tm, String stat, String SID, String from_, String to_,String dd, String tt, String fare, String distance, String CID) {
        DatabaseReference driverInfo;
        MCID = CID;
        driverInfo = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(CID);
        driverInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("last_name") && snapshot.hasChild("first_name")) {
                        full_name = snapshot.child("first_name").getValue().toString()+" " + snapshot.child("last_name").getValue().toString() ;
                    }
                    if (snapshot.hasChild("image")) {
                        imagelink = snapshot.child("image").getValue().toString();
                    }

                    personUtilsList.add(new SchedUtils(tm,stat, SID,from_,to_,full_name, imagelink, dd, tt, distance, fare, MCID));
                    mAdapter.notifyDataSetChanged();
                    if (personUtilsList.size() > 0) {
                        Collections.sort(personUtilsList, SchedUtils.minutes_asc);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSchedInfo(String SID, String stat) {

        schedInfo.child(SID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("Fare")) {
                        fare = snapshot.child("Fare").getValue().toString();
                    }
                    if (snapshot.hasChild("Distance")) {
                        distance = snapshot.child("Distance").getValue().toString();
                    }
                    if (snapshot.hasChild("CID")) {
                        MCID = snapshot.child("CID").getValue().toString();
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
                    if (snapshot.hasChild("Type")) {
                        status = snapshot.child("Type").getValue().toString();
                    }
                    try {
                        total_min=getTotalMin(dd,tt);
                        Log.e("Date",dd+tt);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("Cant convert",e.getMessage().toString());
                    }

                    if(myStats.equals("Cancelled"))
                    {
                        if (status.equals("Accepted Schedule") || status.equals("Schedule") || status.equals("Rated")|| status.equals("Completed")) {

                        }
                        else{
                            getCustomerInfo(total_min,status, SID, s_from,s_to,dd, tt, fare, distance, MCID);
                        }
                    }
                    else {
                        if (status.equals(stat)) {
                            getCustomerInfo(total_min,status, SID, s_from,s_to,dd, tt, fare, distance, MCID);
                        }
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}