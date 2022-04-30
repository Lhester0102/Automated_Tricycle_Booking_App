package com.lhester.polendey.trikila.Schedules;


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
import com.lhester.polendey.trikila.Common_Variables;
import com.lhester.polendey.trikila.CustomerMapActivity;
import com.lhester.polendey.trikila.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubmitRating2 extends AppCompatActivity {
    private String SID="";
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
        getValueExtras();
        ratingStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rbar = rating;
            }
        });

        des.setText("To: "+ViewSched._to.getText().toString());
        loc.setText("From: "+ViewSched._from.getText().toString());
        fare.setText("Fare: ₱" + String.valueOf(ViewSched._fare.getText().toString()));
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTransaction();
                getDriverRating();
                Common_Variables.drivers_id="";
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

    DatabaseReference update_transaction;

    private void updateTransaction() {
        //getTID();
        update_transaction = FirebaseDatabase.getInstance().getReference()
                .child("Transactions").child(SID);
        HashMap map = new HashMap();
        map.put("Type", "Rated");
        map.put("rating", rbar);
        update_transaction.updateChildren(map);

    }

    DatabaseReference update_customer;
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