package com.lhester.polendey.drivers.Schedules;


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
import com.lhester.polendey.drivers.Common_Var;
import com.lhester.polendey.drivers.DriverMapActivity;
import com.lhester.polendey.drivers.R;

import java.util.HashMap;


public class SubmitRating2 extends AppCompatActivity {

    private String SID="";
    RatingBar ratingStar;
    private float rbar = 5;
    private Button btnsubmit;
    private EditText comments;
    TextView fare,loc,des;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_rating2);
        btnsubmit=findViewById(R.id.button);
        comments=findViewById(R.id.txtcomment);
        fare = findViewById(R.id.fare);
        loc = findViewById(R.id.pickuploc);
        des = findViewById(R.id.destination);
        ratingStar=findViewById(R.id.ratingBar);
        rbar=ratingStar.getNumStars();

        getValueExtras();
        ratingStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rbar = rating;
            }
        });

        des.setText("To: "+ ViewSched._to.getText().toString());
        loc.setText("From: "+ViewSched._from.getText().toString());
        fare.setText("Fare: ₱" + ViewSched._fare.getText().toString());
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropoff();
                getDriverRating();
                Common_Var.customerID="";
                finish();
            }
        });
    }
    private void getValueExtras() {
        Bundle bundle = getIntent().getExtras();

        SID = bundle.getString("SID");
     /*  bundle.getString("cname");
        bundle.getString("pickup");
        bundle.getString("destination");
        bundle.getString("distance");
        bundle.getString("fare");
        bundle.getString("dd");
        bundle.getString("tt"); */

    }

    private void dropoff() {
        DatabaseReference dropoff_ref = FirebaseDatabase.getInstance().getReference("Transactions").child(SID);
        HashMap userMap = new HashMap();
        userMap.put("driver_comment",comments.getText().toString());
        userMap.put("crating",rbar);
        dropoff_ref.updateChildren(userMap);
    }
    private float getDriverRating(){

        DatabaseReference driverrating = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(Common_Var.customerID);
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
        DatabaseReference upnewrate = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(Common_Var.customerID );
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",rbar));
        upnewrate.updateChildren(map);
    }

    private void updateRating(float r) {
        DatabaseReference uprate = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(Common_Var.customerID );
        HashMap map = new HashMap();
        map.put("rating", String.format("%.2f",(rbar+r)/2));
        uprate.updateChildren(map);
    }

}