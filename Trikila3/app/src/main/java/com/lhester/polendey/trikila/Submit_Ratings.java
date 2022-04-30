package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.media.Rating;
import android.os.Bundle;
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

public class Submit_Ratings extends AppCompatActivity {
    RatingBar ratingStar;
    Button btnsubmit;
    TextView tid, fare,loc,des;
    EditText txtcomment;
    private float rbar = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit__ratings);
        ratingStar = findViewById(R.id.ratingBar);
        btnsubmit = findViewById(R.id.button);
        tid = findViewById(R.id.tid);
        fare = findViewById(R.id.fare);
        loc = findViewById(R.id.pickuploc);
        des = findViewById(R.id.destination);
        txtcomment=findViewById(R.id.txtcomment);
        rbar=ratingStar.getNumStars();
        ratingStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rbar = rating;
            }
        });
        getTID();
        Geocoder geocoder = new Geocoder(Submit_Ratings.this, Locale.getDefault());
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
        fare.setText("Fare: ₱" + String.valueOf(Common_Variables.fare));
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                        .child(Common_Variables.drivers_id).child("Customer Request");
                driverRef.removeValue();
                updateTransaction();
                removeUpdateCustomer();
                CustomerMapActivity.driver_Arrived = 0;
                Common_Variables.current_time = "";
               // CustomerMapActivity.erasePolylines();
             //   CustomerMapActivity.mBottomSheetBehavior.setState(CustomerMapActivity.mBottomSheetBehavior.STATE_EXPANDED);
              //  CustomerMapActivity.imgLoc.setVisibility(View.VISIBLE);
                getDriverRating();
                CustomerMapActivity.imgReset.setVisibility(View.VISIBLE);
                finish();
            }
        });
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
        map.put("Fare", Common_Variables.fare);
        map.put("Type", "Completed");
        map.put("Comment", txtcomment.getText().toString());
        map.put("Distance",CustomerMapActivity.final_distance);
        map.put("rating", rbar);
        update_transaction.updateChildren(map);

    }

    DatabaseReference update_customer;

    private void removeUpdateCustomer() {
        update_customer = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(Common_Variables.customer_id).child("Current Driver");
        update_customer.removeValue();
    }
    private float getDriverRating(){

        DatabaseReference driverrating = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(Common_Variables.drivers_id);
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
        DatabaseReference upnewrate = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(Common_Variables.drivers_id);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",rbar));
        upnewrate.updateChildren(map);
    }

    private void updateRating(float r) {
        DatabaseReference uprate = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(Common_Variables.drivers_id);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",(rbar+r)/2));
        uprate.updateChildren(map);
    }
}