package com.lhester.polendey.drivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Submit_rating extends AppCompatActivity {

    RatingBar ratingStar;
    private float rbar = 5;
    private Button btnsubmit;
    private EditText comments;
    TextView fare,loc,des;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_rating);
        btnsubmit=findViewById(R.id.button);
        comments=findViewById(R.id.txtcomment);
        fare = findViewById(R.id.fare);
        loc = findViewById(R.id.pickuploc);
        des = findViewById(R.id.destination);
        ratingStar=findViewById(R.id.ratingBar);
        rbar=ratingStar.getNumStars();
        ratingStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rbar = rating;
            }
        });

        Geocoder geocoder = new Geocoder(Submit_rating.this, Locale.getDefault());
        String address = "",address2="";
        try {
            List<Address> addresses = geocoder.getFromLocation(DriverMapActivity.destinationLatLng.getLatitude(),DriverMapActivity.destinationLatLng.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        des.setText("To: "+address);
        try {
            List<Address> addresses = geocoder.getFromLocation(DriverMapActivity.pickupLatLng.getLatitude(),DriverMapActivity.pickupLatLng.getLongitude(), 1);
            address2 = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loc.setText("From: "+address2);
        fare.setText("Fare: ₱" + String.valueOf(DriverMapActivity.final_fare));

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropoff();
                getDriverRating();
                DriverMapActivity.play_drop = false;
                DriverMapActivity.play_pickup = false;
                DriverMapActivity.driver_action = 3;
                DriverMapActivity.toolbar.setTitle("₱" + DriverMapActivity.final_fare);
                DriverMapActivity.btndrop.setVisibility(View.GONE);
                DriverMapActivity.driver_Arrived = 0;
                DriverMapActivity.pickupLatLng = null;
                DriverMapActivity.destinationLatLng = null;

                if(DriverMapActivity.navigationMapRoute!=null){
                    DriverMapActivity.navigationMapRoute.removeRoute();
                }
                finish();
            }
        });
    }
   private void dropoff() {
        DatabaseReference dropoff_ref = FirebaseDatabase.getInstance().getReference("Transactions").child(DriverMapActivity.currentDateandTime);
        HashMap userMap = new HashMap();
        userMap.put("Type", "Dropped");
        userMap.put("driver_comment",comments.getText().toString());
        userMap.put("crating",rbar);
        dropoff_ref.updateChildren(userMap);
    }
    private float getDriverRating(){

        DatabaseReference driverrating = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(DriverMapActivity.customerId);
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
        DatabaseReference upnewrate = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(DriverMapActivity.customerId);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",rbar));
        upnewrate.updateChildren(map);
    }

    private void updateRating(float r) {
        DatabaseReference uprate = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(DriverMapActivity.customerId);
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",(rbar+r)/2));
        uprate.updateChildren(map);
    }

}