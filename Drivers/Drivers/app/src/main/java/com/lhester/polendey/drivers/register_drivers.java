package com.lhester.polendey.drivers;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class register_drivers extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference DriverDatabaseRef;

    private String em="",pas="",_fn="",_mn="",_ln="";
    private  String DriverOnlineID;

    private ProgressDialog loadingbar;
     EditText fn,mn,ln;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_drivers);

        mAuth = FirebaseAuth.getInstance();


        Button btnregdriver=findViewById(R.id.btnregister);
        Button btncanregdriver=findViewById(R.id.btncanceldriver);
        final EditText txtemail=findViewById(R.id.txtemail);
        final EditText txtpas=findViewById(R.id.txtpassword);
        ln=findViewById(R.id.lastname);
        fn=findViewById(R.id.firstname);
        mn=findViewById(R.id.mi);

        loadingbar = new ProgressDialog(this);
        btnregdriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                em = txtemail.getText().toString();
                pas = txtpas.getText().toString();
                _fn = fn.getText().toString();
                _ln = ln.getText().toString();
                _mn = mn.getText().toString();
                if (em== "") {
                    Toast.makeText(register_drivers.this, "Check Email ", Toast.LENGTH_SHORT).show();
                }
                if (pas == "") {
                    Toast.makeText(register_drivers.this, "Check Password ", Toast.LENGTH_SHORT).show();
                } else {
                    loadingbar.setTitle("Driver Registration");
                    loadingbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingbar.setIndeterminate(true);
                    loadingbar.setCancelable(false);
                    loadingbar.setMessage("Please Wait..");
                    loadingbar.show();
                    mAuth.createUserWithEmailAndPassword(em, pas)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        DriverOnlineID=mAuth.getCurrentUser().getUid();
                                        DriverDatabaseRef= FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child("Drivers");
                                        DriverDatabaseRef.setValue(true);

                                        HashMap<String,Object> userMap = new HashMap<>();
                                        userMap.put("uid",mAuth.getCurrentUser().getUid());
                                        userMap.put("firstname",_fn);
                                    //    userMap.put("phone",phoneEditxt.getText().toString());
                                        userMap.put("lastname",_ln);
                                        userMap.put("middlename",_mn);
                                      //  userMap.put("address",address.getText().toString());
                                     //   userMap.put("image",myurl);
                                     //   userMap.put("plate no",plate_noEditext.getText().toString());
                                    //    new SettingsActivity.Async().execute();
                                        DriverDatabaseRef.child(DriverOnlineID).updateChildren(userMap);

                                        Intent intent = new Intent(register_drivers.this,DriverMapActivity.class);
                                        startActivity(intent);
                                        finish();
                                      //  new Async().execute();
                                        Toast.makeText(register_drivers.this, "Driver Registered.",
                                                Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                    } else {
                                        Toast.makeText(register_drivers.this, "Driver Error." + task.getException().toString(),
                                                Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                    }
                                }
                            });
                }
            }
        });
        btncanregdriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(register_drivers.this,LogInDriver.class);
                startActivity(intent);
                finish();
            }
        });
    }

/*  //  ProgressDialog   mProgressDialog;
    String text,errorText;
    //register
    class Async extends AsyncTask<Void, Void, Void> {
        String records = "",error="";

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try
            {
                Thread.sleep(1000);
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://freedb.tech/freedbtech_lhester",
                        "freedbtech_lhester08","Lhester08");
                Statement statement = connection.createStatement();
                if(connection!=null){
                    Log.e("DBC:","Connected");
                }

                PreparedStatement prest = connection.prepareStatement
                        ("Insert into users(username,email,password,usertype,id,firstname,lastname,middlename) values(?,?,?,?,?,?,?,?)");
                prest.setString(1,em);
                prest.setString(2,em);
                prest.setString(3,pas);
                prest.setString(4,"Driver");
                prest.setString(5,mAuth.getCurrentUser().getUid());
                prest.setString(6,_fn);
                prest.setString(7,_ln);
                prest.setString(8,_mn);
                prest.executeUpdate();

            }
            catch(Exception e)
            {
                error = e.toString();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            text=records;
            if(error != "") {
                errorText = error;
                super.onPostExecute(aVoid);
                Toast toast = Toast.makeText(register_drivers.this, errorText, Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                Toast toast = Toast.makeText(register_drivers.this, "Account Has Been Saved!..", Toast.LENGTH_SHORT);
                toast.show();

            }

        }

    }
// */
}
