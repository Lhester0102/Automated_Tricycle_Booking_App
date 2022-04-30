package com.lhester.polendey.drivers;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class Transaction_Histories extends AppCompatActivity {
    private ImageView closeButton;
    ListView lv;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    ArrayList<HashMap<String, String>> lists;
    private ArrayList<String> sub_lists = new ArrayList<>();
    private SimpleAdapter adapter;
    String did = "";
    private TextView txtdate, txttotal;
    String mYear = "", mMonth = "", mDay = "";

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction__histories);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Transactions");
        lv = findViewById(R.id.history);
        txtdate = findViewById(R.id.txtdate);
        txttotal = findViewById(R.id.fares);
        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(Transaction_Histories.this, lists, R.layout.lists_layout,
                new String[]{"Transaction_ID", "Content","td"},
                new int[]{R.id.TID, R.id.child,R.id.TID2});
        lv.setAdapter(adapter);
        //  adapter.notifyDataSetChanged();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Transaction_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                Common_Var.histori_id = (String) obj.get("Transaction_ID");
                startActivity(new Intent(Transaction_Histories.this, View_History.class));
                Log.e("Yourtag", Common_Var.histori_id);
            }
        });


        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Calendar c = Calendar.getInstance();
        mYear = String.valueOf(c.get(Calendar.YEAR));
        mMonth = String.format("%02d", c.get(Calendar.MONTH) + 1);
        mDay = String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
        txtdate.setText(mYear + "-" + mMonth + "-" + mDay);
        // txtdate.setText("2021-04-30");
        txtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(Transaction_Histories.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                txtdate.setText(year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth));
                                getLists();

                            }
                        }, Integer.parseInt(mYear), Integer.parseInt(mMonth) - 1, Integer.parseInt(mDay));
                dpd.show();
            }
        });
    }

    float fares = 0;

    private void getLists() {
        if (lists != null) {
            fares = 0;
            txttotal.setText(String.format("%.2f", fares));
            lists.clear();
            adapter.notifyDataSetChanged();
        }
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        if (postSnapShot.getKey().equals("DID")) {
                            String value1 = dataSnapshot.child("DID").getValue().toString();
                            if (dataSnapshot.child("Date").exists()) {
                                String dates = dataSnapshot.child("Date").getValue().toString();
                                if (dates.equals(txtdate.getText().toString())) {
                                    String value2 = dataSnapshot.getKey();
                                    String Fare = dataSnapshot.child("Fare").getValue().toString();
                                    String Type = dataSnapshot.child("Type").getValue().toString();
                                    String d=dataSnapshot.child("Date").getValue().toString();
                                    String t =dataSnapshot.child("Time").getValue().toString();
                                    String Rating = "";
                                    if (dataSnapshot.child("rating").getValue() != null) {
                                        Rating = dataSnapshot.child("rating").getValue().toString();
                                    }
                                    if (dataSnapshot.child("DID").getValue() != null) {
                                        did = dataSnapshot.child("DID").getValue().toString();
                                    }
                                    HashMap<String, String> datum = new HashMap<String, String>();
                                    datum.put("Transaction_ID", value2);
                                    datum.put("td", d + " " + t);
                                    datum.put("Content", "Fare:" + Fare + ", Type:" + Type + ", Rating:" + Rating);
                                    lists.add(datum);
                                    fares = fares + Float.parseFloat(Fare);
                                    txttotal.setText(String.format("%.2f", fares));
                                    adapter.notifyDataSetChanged();
                                }


                            }
                        }
                    }
                }
                Collections.reverse(lists);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}