package com.lhester.polendey.trikila.Schedules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhester.polendey.trikila.Common_Variables;
import com.lhester.polendey.trikila.Msgs;
import com.lhester.polendey.trikila.R;

import java.util.HashMap;

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
    private Button btncaneltrip, btnpickup, btncomplete;
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

        btnpickup = findViewById(R.id.btnPickup);
        btncomplete = findViewById(R.id.btnComplete);


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
                                map.put("Type", "Canceled by the Passenger");

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


        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
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

                                Intent i = new Intent(ViewSched.this, SubmitRating2.class);
                                i.putExtra("SID", SID2);
                                i.putExtra("CID", SID2);
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

                                Intent i = new Intent(ViewSched.this, SubmitRating2.class);
                                i.putExtra("SID", SID2);
                                i.putExtra("CID", SID2);
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
                        if (stat.getText().toString().equals("In Progress")) {
                            stats.setText("Your Driver is on the way");
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.GONE);
                            second.setVisibility(View.VISIBLE);
                            inRide.setVisibility(View.VISIBLE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                        } else if (stat.getText().toString().equals("Passenger has been pick up")) {
                            btnpickup.setVisibility(View.VISIBLE);
                            btncomplete.setVisibility(View.GONE);
                            stats.setText("On the way to passenger's destination");
                            scrollView.setVisibility(View.GONE);
                            second.setVisibility(View.GONE);
                            third.setVisibility(View.VISIBLE);
                            inRide.setVisibility(View.VISIBLE);
                        } else if (stat.getText().toString().equals("Completed")) {
                            stats.setText("Arkila Trip has been Completed");
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.VISIBLE);
                            findViewById(R.id.idmsg).setVisibility(View.GONE);

                        }else if (stat.getText().toString().equals("Rated")) {
                            stats.setText("Arkila Trip has been Completed");
                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                            findViewById(R.id.idmsg).setVisibility(View.GONE);

                        }

                        else  {
                            stats.setText("Arkila Canceled by the Driver");

                            btnpickup.setVisibility(View.GONE);
                            btncomplete.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            second.setVisibility(View.VISIBLE);
                            third.setVisibility(View.GONE);
                            inRide.setVisibility(View.GONE);
                            findViewById(R.id.button4).setVisibility(View.GONE);
                            findViewById(R.id.button5).setVisibility(View.GONE);
                        }
                    }
                    if (snapshot.hasChild("CID")) {
                        CID2 = snapshot.child("CID").getValue().toString();
                    }
                    if (snapshot.hasChild("DID")) {
                        Common_Variables.drivers_id = snapshot.child("DID").getValue().toString();
                        Common_Variables.drivers_id2 = snapshot.child("DID").getValue().toString();
                    }
                    Common_Variables.customer_id = CID2;

                    getCustomerInfo(CID2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}