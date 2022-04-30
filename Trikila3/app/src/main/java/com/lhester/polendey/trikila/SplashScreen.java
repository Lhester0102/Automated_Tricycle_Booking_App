package com.lhester.polendey.trikila;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private Switch switch1,switch2;
    private Button btnNext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        switch1=findViewById(R.id.switch1);
        switch2=findViewById(R.id.switch2);
        btnNext=findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SplashScreen.this,logincustomer.class);
                startActivity(intent);
                finish();
            }
        });
        isGPSenabled();
        isNetworkAvailable();
        validate_();
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        });

    }

    private void validate_() {
        if (isNet){
            switch1.setChecked(true);
        }
        else {
            switch1.setChecked(false);
        }
        if (isGps){
            switch2.setChecked(true);
        }
        else {
            switch2.setChecked(false);
        }

        if(isNet && isGps)
        {
            btnNext.setEnabled(true);
        }
        else {
            btnNext.setEnabled(false);
        }
    }

    private Boolean isNet=false;
    private void isNetworkAvailable() {

        ConnectivityManager manager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=null;
        if(manager!=null){
            networkInfo=manager.getActiveNetworkInfo();
            if(networkInfo!=null) {
                isNet = true;
            }
        }


    }
    private Boolean isGps=false;
    private void isGPSenabled() {
        LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnables=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerEnables){
            isGps=true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isGPSenabled();
        isNetworkAvailable();
        validate_();
        // Log.e("Switch State=","resumed");
    }
}
