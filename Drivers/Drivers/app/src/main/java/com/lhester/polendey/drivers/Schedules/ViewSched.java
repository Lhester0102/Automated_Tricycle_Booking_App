package com.lhester.polendey.drivers.Schedules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhester.polendey.drivers.Alarms.SetAlarms;
import com.lhester.polendey.drivers.Common_Var;
import com.lhester.polendey.drivers.DriverMapActivity;
import com.lhester.polendey.drivers.Msgs;
import com.lhester.polendey.drivers.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSched extends AppCompatActivity {
    public TextView SID;
    public TextView cname;
    public static TextView _from;
    public static TextView _to;
    public TextView _distance;
    public static TextView _fare;
    public TextView _date;
    public TextView stat;
    public TextView phone;
    public TextView _time;
    public TextView stats;
    public CircleImageView img;
    private ImageView imgMsg, imgMsg2;
    private Button btncaneltrip, btnpickup, btncomplete, btnaccept;
    String CID2 = "", SID2 = "";
    private ScrollView scrollView;
    LinearLayout schedlayout, first, second, third, inRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sched);
        SID = findViewById(R.id.SID);
        cname = findViewById(R.id.cname);
        //img = findViewById(R.id.userImg);
        _date = findViewById(R.id.dt);
        _time = findViewById(R.id.tm);
        _distance = findViewById(R.id.distance);
        _fare = findViewById(R.id.fare);
        _from = findViewById(R.id.frm);
        _to = findViewById(R.id.to);
        stat = findViewById(R.id.stat);
        phone = findViewById(R.id.phone);

        stats = findViewById(R.id.status);
        scrollView = findViewById(R.id.myview);
        second = findViewById(R.id.second);
        third = findViewById(R.id.third);
        inRide = findViewById(R.id.inRides);

        scrollView.setVisibility(View.VISIBLE);
        inRide.setVisibility(View.GONE);
        // first.setVisibility(View.VISIBLE);
        second.setVisibility(View.GONE);
        third.setVisibility(View.GONE);

        btncaneltrip = findViewById(R.id.btncancelRide);
        btnpickup = findViewById(R.id.btnPickup);
        btncomplete = findViewById(R.id.btnComplete);
        btnaccept = findViewById(R.id.btnAccept);


        getValueExtras();
        getSchedInfo(SID.getText().toString());

        imgMsg = findViewById(R.id.idmsg);

        imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewSched.this, Msg2.class);
                startActivity(intent);
                // txtmsgcount.setVisibility(View.GONE);
                // txtmsgcount.setText("");
                // msgcount = 0;
            }
        });
        imgMsg2 = findViewById(R.id.idmsg2);

        imgMsg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewSched.this, Msg2.class);
                startActivity(intent);
                // txtmsgcount.setVisibility(View.GONE);
                // txtmsgcount.setText("");
                // msgcount = 0;
            }
        });


        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(ViewSched.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Cancel")
                        .setContentText("Are you sure you want to cancel this Arkila?")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {


                                DatabaseReference schedInfo2;
                                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Transactions").child(SID2);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("Type", "Arkila Canceled by the Driver");

                                schedInfo2.updateChildren(map);
                                updateSchedType(SID2, "Arkila Canceled by the Driver");

                                finish();
                                Toast.makeText(ViewSched.this, "Canceled", Toast.LENGTH_SHORT).show();

                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();


            }
        });


        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(ViewSched.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Ride")
                        .setConfirmText("Start")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                DatabaseReference schedInfo2;
                                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Transactions").child(SID2);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("Type", "In Progress");

                                schedInfo2.updateChildren(map);
                                updateSchedType(SID2, "In Progress");
                                Toast.makeText(ViewSched.this, "Go Pickup", Toast.LENGTH_SHORT).show();

                                stats.setText("On the way to pickup passenger");

                                btncaneltrip.setVisibility(View.VISIBLE);
                                btnpickup.setVisibility(View.VISIBLE);
                                btncomplete.setVisibility(View.GONE);
                                scrollView.setVisibility(View.GONE);
                                second.setVisibility(View.VISIBLE);
                                inRide.setVisibility(View.VISIBLE);


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

        btnpickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(ViewSched.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Pickup")
                        .setContentText("Pickup Passenger")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {


                                DatabaseReference schedInfo2;
                                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Transactions").child(SID2);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("Type", "Passenger has been pick up");
                                updateSchedType(SID2, "Passenger has been pick up");
                                schedInfo2.updateChildren(map);
                                Toast.makeText(ViewSched.this, "Passenger has been pickup", Toast.LENGTH_SHORT).show();

                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();


            }
        });
        btncomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(ViewSched.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Complete Trip")
                        .setContentText("Complete Trip")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {


                                DatabaseReference schedInfo2;
                                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Transactions").child(SID2);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("Type", "Completed");
                                schedInfo2.updateChildren(map);
                                updateSchedType(SID2, "Completed");
                                Intent i = new Intent(ViewSched.this, SubmitRating2.class);
                                i.putExtra("SID", SID2);
                                startActivity(i);
                                Toast.makeText(ViewSched.this, "Complete Trip", Toast.LENGTH_SHORT).show();

                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();
            }
        });

        btncaneltrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(ViewSched.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Cancel")
                        .setContentText("Are you sure you want to cancel this Arkila?")
                        .setConfirmText("Yes")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {


                                DatabaseReference schedInfo2;
                                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Transactions").child(SID2);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("Type", "Arkila Canceled by the Driver");

                                schedInfo2.updateChildren(map);

                                finish();
                                Toast.makeText(ViewSched.this, "Canceled", Toast.LENGTH_SHORT).show();

                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .show();

            }
        });
        btnaccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DatabaseReference schedInfo2;
                schedInfo2 = FirebaseDatabase.getInstance().getReference()
                        .child("Transactions").child(SID2);

                HashMap<String, Object> map = new HashMap<>();
                map.put("Type", "Accepted Schedule");

                schedInfo2.updateChildren(map);

                DatabaseReference schedInfo3;
                schedInfo3 = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Schedules")
                        .child(SID2);

                HashMap<String, Object> map3 = new HashMap<>();
                map3.put("TYPE", "Accepted Schedule");

                schedInfo3.updateChildren(map3);

                DatabaseReference schedInfo4;
                schedInfo4 = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Customers")
                        .child(Common_Var.customerID)
                        .child("Schedules")
                        .child(SID2);

                schedInfo4.updateChildren(map3);
                String dtStart = _date.getText().toString()+_time.getText().toString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddHH:mm a", Locale.ENGLISH);
                int y=0,mm=0,d=0,m=0,h=0;
                try {
                    Date date = format.parse(dtStart);
                    Log.e("DATE FORMATED ALARM",date.toString());
                    h=date.getHours();
                    y=date.getYear() +1900;
                    mm=date.getMonth();
                    m=date.getMinutes();
                    d=date.getDate();
                    if((m-5)<0){
                        h=h-1;
                        m=(m+60)-5;
                    }
                    else {
                        m=m-5;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SetAlarms.AddAlarm(SID2,ViewSched.this,y,mm,d,h,m);
                Toast.makeText(ViewSched.this, "Request Accepted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getValueExtras() {
        Bundle bundle = getIntent().getExtras();

        SID2 = bundle.getString("SID");
     /*  bundle.getString("cname");
        bundle.getString("pickup");
        bundle.getString("destination");
        bundle.getString("distance");
        bundle.getString("fare");
        bundle.getString("dd");
        bundle.getString("tt"); */

        SID.setText(bundle.getString("SID"));
        cname.setText(bundle.getString("cname"));
        _from.setText(bundle.getString("pickup"));
        _to.setText(bundle.getString("destination"));
        _distance.setText(bundle.getString("distance"));
        _fare.setText(bundle.getString("fare"));
        _date.setText(bundle.getString("dd"));
        _time.setText(bundle.getString("tt"));

    }

    private void getCustomerInfo(String CID) {
        DatabaseReference driverInfo;
        driverInfo = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(CID);
        driverInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //    if (snapshot.hasChild("image")) {
                    // imagelink = snapshot.child("image").getValue().toString();
                    // }
                    if (snapshot.hasChild("phone")) {
                        phone.setText(snapshot.child("phone").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSchedInfo(String MSID) {
        DatabaseReference schedInfo;
        schedInfo = FirebaseDatabase.getInstance().getReference()
                .child("Transactions");
        schedInfo.child(MSID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("Type")) {
                        stat.setText(snapshot.child("Type").getValue().toString());


                        if (stat.getText().toString().equals("Schedule")) {
                            stats.setText("Waiting for the driver to Accept the request");
                            btncaneltrip.setVisibility(View.GONE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.VISIBLE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                            btnaccept.setVisibility(View.VISIBLE);

                        } else if (stat.getText().toString().equals("Accepted Schedule")) {
                            stats.setText("Arkila Trip has been Accepted");
                            btncaneltrip.setVisibility(View.GONE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.VISIBLE);
                            btnaccept.setVisibility(View.GONE);

                        } else if (stat.getText().toString().equals("In Progress")) {

                            stats.setText("On the way to pickup passenger");
                            btncaneltrip.setVisibility(View.VISIBLE);
                            btnpickup.setVisibility(View.VISIBLE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.GONE);
                            second.setVisibility(View.VISIBLE);
                            inRide.setVisibility(View.VISIBLE);
                            btnaccept.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);

                        } else if (stat.getText().toString().equals("Passenger has been pick up")) {
                            btncaneltrip.setVisibility(View.VISIBLE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.VISIBLE);
                            stats.setText("On the way to passenger's destination");
                            scrollView.setVisibility(View.GONE);
                            second.setVisibility(View.GONE);
                            third.setVisibility(View.VISIBLE);
                            inRide.setVisibility(View.VISIBLE);
                            btnaccept.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);

                        } else if (stat.getText().toString().equals("Completed")) {
                            stats.setText("Arkila Trip has been Completed");
                            btncaneltrip.setVisibility(View.GONE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                            findViewById(R.id.idmsg).setVisibility(View.GONE);
                            btnaccept.setVisibility(View.GONE);

                        } else if (stat.getText().toString().equals("Rated")) {
                            stats.setText("Arkila Trip has been Rated");
                            btncaneltrip.setVisibility(View.GONE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                            findViewById(R.id.idmsg).setVisibility(View.GONE);
                            btnaccept.setVisibility(View.GONE);

                        } else if (stat.getText().toString().equals("Arkila Canceled by the Driver")) {
                            stats.setText("Arkila Canceled by the Driver");
                            btncaneltrip.setVisibility(View.GONE);
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                            btnaccept.setVisibility(View.GONE);
                        }


                    }
                    if (snapshot.hasChild("CID")) {
                        CID2 = snapshot.child("CID").getValue().toString();
                    }
                    Common_Var.customerID = CID2;
                    Common_Var.customerID2 = CID2;
                    getCustomerInfo(CID2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateSchedType(String SID, String type) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Schedules")
                .child(SID);

        HashMap userMap = new HashMap();
        userMap.put("TYPE", type);
        databaseReference.updateChildren(userMap);


        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(Common_Var.customerID)
                .child("Schedules")
                .child(SID);

        HashMap userMap2 = new HashMap();
        userMap2.put("TYPE", type);
        databaseReference1.updateChildren(userMap);

    }
}