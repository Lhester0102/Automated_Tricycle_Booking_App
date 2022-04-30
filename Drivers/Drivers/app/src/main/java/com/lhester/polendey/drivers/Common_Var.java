package com.lhester.polendey.drivers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Common_Var {
    public static String histori_id="";
    public static String customerID="";
    public static String customerID2="";
    public static String minimum_fare="";
    public static String per_km="";
    public static String pn="";

    public static void get_static_fare(){
        DatabaseReference get_fare_matrix = FirebaseDatabase.getInstance().getReference().child("Fare");
        get_fare_matrix.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("min") != null) {
                        minimum_fare=map.get("min").toString();
                    }
                    if (map.get("per_km") != null) {
                        per_km=map.get("per_km").toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
