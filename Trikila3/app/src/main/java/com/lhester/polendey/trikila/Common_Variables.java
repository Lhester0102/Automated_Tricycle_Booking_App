package com.lhester.polendey.trikila;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Common_Variables {
    public static DatabaseReference drivers_available = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
    public static DatabaseReference drivers_working = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
    public static DatabaseReference customer_requests = FirebaseDatabase.getInstance().getReference().child("Customer Requests");
    public static DatabaseReference users_customers = FirebaseDatabase.getInstance().getReference().child("Customers");
    public static DatabaseReference users_drivers = FirebaseDatabase.getInstance().getReference().child("Drivers");

    public static int setroute=0;
    public static String customer_id= "";
    public static String customer_id2= "";
    public static String drivers_id="";
    public static String drivers_id2="";
    public static  String distance="";
    public static String estimated_fare="";
    public static String fare="";
    public static com.mapbox.mapboxsdk.geometry.LatLng customer_destination;
    public static com.mapbox.mapboxsdk.geometry.LatLng customer_pickuploaction;
    public static String current_time="";
    public static String histori_id="";
    public static String newRideDriverID="";
    public static String pn="";


}
