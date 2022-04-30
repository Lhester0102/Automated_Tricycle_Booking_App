package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class View_History extends AppCompatActivity {
    private ImageView closeButton;
    private String fname = "", lname = "", mname = "", did = "",d,p;
    private TextView tid, dname, phone, plate, distance, fare, ratings, status, frm, to;
    CircleImageView profileImage;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view__history);
        tid = findViewById(R.id.id);
        dname = findViewById(R.id.dname);
        phone = findViewById(R.id.phone);
        plate = findViewById(R.id.plate_no);
        distance = findViewById(R.id.distance);
        fare = findViewById(R.id.fare);
        ratings = findViewById(R.id.rating);
        status = findViewById(R.id.type);
        frm = findViewById(R.id.frm);
        to = findViewById(R.id.to);
        profileImage = findViewById(R.id.profile_image);

        tid.setText(Common_Variables.histori_id);
        getTransactionInfo();
        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getDriverInfo() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(did);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map.get("firstname") != null) {
                    String fullname = dataSnapshot.child("firstname").getValue().toString();
                    fname = fullname;
                }
                if (map.get("lastname") != null) {
                    String lastname = dataSnapshot.child("lastname").getValue().toString();
                    lname = lastname;
                }
                if (map.get("middlename") != null) {
                    String middlename = dataSnapshot.child("middlename").getValue().toString();
                    mname = middlename;
                }
                if (map.get("phone") != null) {
                    String _phone = dataSnapshot.child("phone").getValue().toString();
                    phone.setText(_phone);
                }
                if (map.get("plate_no") != null) {
                    String _plate = dataSnapshot.child("plate_no").getValue().toString();
                    plate.setText(_plate);
                }

                if (map.get("image") != null) {
                    String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(profileImage);
                }
                dname.setText(lname + ", " + fname + " " + mname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getTransactionInfo() {

        Query mCustomerDatabase =
                FirebaseDatabase.getInstance().getReference()
                .child("Transactions")
                .child(Common_Variables.histori_id).orderByChild("Transaction Date");
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Found", String.valueOf(dataSnapshot.getChildrenCount()));
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                double pickup_locationLat = 0;
                double pickup_locationLng = 0;
                double destination_locationLat = 0;
                double destination_locationLng = 0;
                if (map.get("rating") != null) {
                    String _ratings = dataSnapshot.child("rating").getValue().toString();
                    ratings.setText(_ratings);
                }
                if (map.get("Fare") != null) {
                    String _fare = dataSnapshot.child("Fare").getValue().toString();
                    fare.setText(_fare);
                }
                if (map.get("Type") != null) {
                    String _type = dataSnapshot.child("Type").getValue().toString();
                    status.setText(_type);
                }

                if (map.get("pickupLat") != null) {
                    pickup_locationLat = Double.parseDouble(map.get("pickupLat").toString());
                }
                if (map.get("pickupLong") != null) {
                    pickup_locationLng = Double.parseDouble(map.get("pickupLong").toString());
                }
                if (map.get("destinationLat") != null) {
                    destination_locationLat = Double.parseDouble(map.get("destinationLat").toString());
                }
                if (map.get("destinationLong") != null) {
                    destination_locationLng = Double.parseDouble(map.get("destinationLong").toString());
                }


                Location pickup = new Location("");
                pickup.setLatitude(pickup_locationLat);
                pickup.setLongitude(pickup_locationLng);

                Location destination = new Location("");
                destination.setLatitude(destination_locationLat);
                destination.setLongitude(destination_locationLng);
                float computed_distance = pickup.distanceTo(destination);
                distance.setText(String.format("%.2f", computed_distance / 1000) + " km");

                p=getAddress(pickup_locationLat, pickup_locationLng);
                d=getAddress(destination_locationLat, destination_locationLng);
                frm.setText(p);
                to.setText(d);
                if (map.get("DID") != null) {
                    String _did = dataSnapshot.child("DID").getValue().toString();
                    did = _did;
                    Log.e("DID", did);
                }
                getDriverInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getAddress(double pickup_locationLat, double pickup_locationLng) {
        Geocoder geocoder = new Geocoder(View_History.this, Locale.getDefault());
        String address = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(pickup_locationLat, pickup_locationLng, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

}
