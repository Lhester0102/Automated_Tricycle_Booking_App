package com.lhester.polendey.drivers;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LogInDriver extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String emm = "";
    private String pas = "";
    private ProgressDialog loadingbar;
    private  SweetAlertDialog pDialog=null;
    private CheckBox saveLoginCheckBox;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private EditText em,ps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_driver);

        Common_Var.get_static_fare();
        mAuth = FirebaseAuth.getInstance();
        Button btnlogin = findViewById(R.id.btnlogin);
        Button btncancel = findViewById(R.id.btncanceldriver);
        em = findViewById(R.id.txtemail);
        ps = findViewById(R.id.txtpassword);

        saveLoginCheckBox=findViewById(R.id.checkBox);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        if(username!=null && password!=null) {
            saveLoginCheckBox.setChecked(false);
            em.setText(username);
            ps.setText(password);
        }
        loadingbar = new ProgressDialog(this);
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInDriver.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emm = em.getText().toString();
                pas = ps.getText().toString();
                if (emm.equals("")) {
                    Toast.makeText(LogInDriver.this, "Check Email ", Toast.LENGTH_SHORT).show();
                }
                if (pas.equals("")) {
                    Toast.makeText(LogInDriver.this, "Check Password ", Toast.LENGTH_SHORT).show();
                }
                if(!emm.equals("") && !pas.equals("")){
                     pDialog = new SweetAlertDialog(LogInDriver.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Driver Login");
                    pDialog.setContentText("Please Wait..");
                    pDialog.setCancelable(false);
                    pDialog.show();
                    mAuth.signInWithEmailAndPassword(emm, pas)
                            .addOnCompleteListener(LogInDriver.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(LogInDriver.this, "Log in Error", Toast.LENGTH_SHORT).show();
                                        // loadingbar.dismiss();
                                        pDialog.dismiss();
                                    } else {
                                    getUserInformation();
                                    //    Intent intent = new Intent(LogInDriver.this,DriverMapActivity.class);
                                     //   startActivity(intent);
                                        //  Toast.makeText(logincustomer.this, "Log in Successful", Toast.LENGTH_SHORT).show();
                                        //loadingbar.dismiss();

                                      //  pDialog.dismiss();
                                      //  finish();

                                    }
                                }
                            });


               /*     loadingbar.setTitle("Driver Login");
                    loadingbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingbar.setIndeterminate(true);
                    loadingbar.setCancelable(false);
                    loadingbar.setMessage("Please Wait..");
                    loadingbar.show(); */

                }
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        isGPSenabled();
        isNetworkAvailable();
        validate_();
    }

    public void Register_Driver(View v) {
        Intent intent = new Intent(LogInDriver.this, register_drivers.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();

    }
    private DatabaseReference databaseReference;
    private String customerVerify="false";
    private  void getUserInformation() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {

                    if (dataSnapshot.child("status").getValue().toString().equals("Active")) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(em.getWindowToken(), 0);

                        if (saveLoginCheckBox.isChecked()) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putString(PREF_USERNAME, em.getText().toString())
                                    .putString(PREF_PASSWORD, ps.getText().toString())
                                    .apply();
                        }

                        if (checkIfEmailVerified()) {
                            Intent intent = new Intent(LogInDriver.this, DriverMapActivity.class);
                            startActivity(intent);
                            pDialog.dismiss();
                            finish();
                        } else {
                            pDialog.dismiss();
                            sendVerificationEmail();

                        }
                    }
                    else {
                        new SweetAlertDialog(LogInDriver.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error Login")
                                .setContentText("Your Account is suspended,please contact system administrator")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                       sweetAlertDialog.dismissWithAnimation();
                                        pDialog.dismiss();
                                    }
                                })
                                .show();
                    }


                }
                else{
                    Toast.makeText(LogInDriver.this, "Error Log in", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkIfEmailVerified() {
        boolean verified=false;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {
            verified=true;

        } else {
            verified=false;
        }
        return verified;
    }
    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                new SweetAlertDialog(LogInDriver.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Email Verification")
                                        .setContentText("Email is not yet Verified and please check your email for the link verification ")
                                        .setConfirmText("  OK  ")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .show();
                                // Toast.makeText(LogInDriver.this, "Send", Toast.LENGTH_LONG).show();
                            } else {
                                //   Toast.makeText(LogInDriver.this, "Error Send", Toast.LENGTH_LONG).show();
                                new SweetAlertDialog(LogInDriver.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Email Verification")
                                        .setContentText("Email is not yet Verified and an error on sending email verification")
                                        .setConfirmText("  OK  ")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .show();
                            }
                        }
                    });
        }
    }
    private void validate_() {
        //    if(isNet && isGps)
        if(isGps)
        {
            // btnNext.setEnabled(true);

        }
        else {
            //showLocationDisabledInfo();
            onGPS2();
            //  btnNext.setEnabled(false);
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
        if(MyDialog!=null) {
            MyDialog.dismiss();
        }
        isGPSenabled();
        isNetworkAvailable();
        validate_();
        // Log.e("Switch State=","resumed");
    }
    SweetAlertDialog MyDialog;
    private void onGPS2(){
        MyDialog=  new SweetAlertDialog(LogInDriver.this, SweetAlertDialog.WARNING_TYPE);
        MyDialog.setTitleText("Location");
        MyDialog.setContentText("Please Enable GPS / Location");
        MyDialog.setConfirmText("YES");
        MyDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                sweetAlertDialog.dismiss();
            }
        }).setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                finish();
            }
        });
        MyDialog.show();
    }
}
