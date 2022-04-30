package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RateDriver extends AppCompatActivity {
    RatingBar ratingStar;
    Button btnsubmit;
    TextView tid, fare,loc,des;
    EditText txtcomment;
    String DID="";
    private float rbar = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_driver);
        ratingStar = findViewById(R.id.ratingBar);
        btnsubmit = findViewById(R.id.button);
        tid = findViewById(R.id.tid);
        fare = findViewById(R.id.fare);
        loc = findViewById(R.id.pickuploc);
        des = findViewById(R.id.destination);
        txtcomment=findViewById(R.id.txtcomment);
        rbar=ratingStar.getNumStars();
        getValueExtras();
        Log.e("DID",DID);
        ratingStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rbar = rating;
            }
        });
        getTID();
        Geocoder geocoder = new Geocoder(RateDriver.this, Locale.getDefault());
        String address = "",address2="";
        try {
            List<Address> addresses = geocoder.getFromLocation(Common_Variables.customer_destination.getLatitude(), Common_Variables.customer_destination.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        des.setText("To: "+address);
        try {
            List<Address> addresses = geocoder.getFromLocation(Common_Variables.customer_pickuploaction.getLatitude(), Common_Variables.customer_pickuploaction.getLongitude(), 1);
            address2 = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loc.setText("From: "+address2);
        fare.setText("Fare: ₱" + NewRide.fares.getText().toString());
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDriverRating();
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                        .child(Common_Variables.newRideDriverID).child("Customer Request");
                driverRef.removeValue();
                updateTransaction();
                removeUpdateCustomer();
                NewRide.driver_Arrived = 0;
                NewRide.fa.finish();
                OnlineDrivers.refreshContent();
                Common_Variables.customer_pickuploaction=null;
                Common_Variables.customer_destination=null;
                Common_Variables.newRideDriverID = "";
                finish();
            }
        });
    }
    private void getValueExtras() {
        Bundle bundle = getIntent().getExtras();

        DID = bundle.getString("DID");
     /*  bundle.getString("cname");
        bundle.getString("pickup");
        bundle.getString("destination");
        bundle.getString("distance");
        bundle.getString("fare");
        bundle.getString("dd");
        bundle.getString("tt"); */

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
                        tid.setText(Common_Variables.current_time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    DatabaseReference update_transaction;

    private void updateTransaction() {
        //getTID();
        update_transaction = FirebaseDatabase.getInstance().getReference()
                .child("Transactions").child(Common_Variables.current_time);
        HashMap map = new HashMap();
        map.put("Fare",NewRide.fares.getText().toString());
        map.put("Type", "Completed");
        map.put("Comment", txtcomment.getText().toString());
        map.put("Distance",NewRide.final_distance);
        map.put("rating", rbar);
        update_transaction.updateChildren(map);

    }

    DatabaseReference update_customer;

    private void removeUpdateCustomer() {
        update_customer = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Current Driver");
        update_customer.removeValue();
    }
    private float getDriverRating(){

        DatabaseReference driverrating = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID);
        driverrating.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    float r=0;
                    if(snapshot.hasChild("rating")){

                        r=Float.parseFloat( snapshot.child("rating").getValue().toString());
                        updateRating(r);
                    }
                    else{
                        updateNewRating();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        float ratings=0;
        return ratings;

    }

    private void updateNewRating() {
        DatabaseReference upnewrate = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",rbar));
        upnewrate.updateChildren(map);
    }

    private void updateRating(float r) {
        DatabaseReference uprate = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",(rbar+r)/2));
        uprate.updateChildren(map);
    }
}