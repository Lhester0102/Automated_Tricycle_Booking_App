package com.lhester.polendey.trikila;

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
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dref.removeValue();
        DatabaseReference  dref2=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Current Driver");
        dref2.removeValue();
        DatabaseReference  dref3=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(CustomerMapActivity.driver_id_found).child("Customer Request");
        dref3.removeValue();
        stopSelf();
    }
}