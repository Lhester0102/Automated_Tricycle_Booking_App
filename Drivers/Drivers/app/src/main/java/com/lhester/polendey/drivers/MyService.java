package com.lhester.polendey.drivers;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyService extends Service {
    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override public int onStartCommand(Intent intent, int flags, int
            startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY; }

    @Override public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed"); }

    @Override public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        //Code here

        DatabaseReference dref= FirebaseDatabase.getInstance().getReference().child("Drivers Available").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dref.removeValue();
        DatabaseReference  dref2=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").
                child(Common_Var.customerID).child("Current Driver");
        dref2.removeValue();

        DatabaseReference  dref22=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").
                child(DriverMapActivity.customerId).child("Current Driver");
        dref22.removeValue();

        DatabaseReference  dref3=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Customer Request");
        dref3.removeValue();

        DatabaseReference working= FirebaseDatabase.getInstance().getReference().child("Drivers Working").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        working.removeValue();
        stopSelf();
    }
}