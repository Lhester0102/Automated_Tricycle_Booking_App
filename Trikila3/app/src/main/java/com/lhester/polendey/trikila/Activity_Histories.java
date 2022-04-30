package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Histories extends AppCompatActivity {
    private ImageView closeButton;
    ListView lv;
    SearchView sv;
    ArrayAdapter<String> larray;
    String[] data = {"sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas"};
    private FirebaseDatabase database;
    private Query databaseReference;

    ArrayList<HashMap<String, String>> lists;
    //private String[] titleArray,subItemArray;
    private ArrayList<String> sub_lists = new ArrayList<>();
    private SimpleAdapter adapter;
    String did = "";
    private TextView txtdate;
    String mYear = "", mMonth = "", mDay = "";
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__histories);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Transactions");
        lv = findViewById(R.id.history);
        txtdate = findViewById(R.id.txtdate);

        getLists();
        lists = new ArrayList<HashMap<String, String>>();
       // Collections.reverse(lists);

        adapter = new SimpleAdapter(Activity_Histories.this, lists, R.layout.lists_layout,
                new String[]{"Transaction_ID", "Content", "DT"},
                new int[]{R.id.TID, R.id.child, R.id.DT});

        lv.setAdapter(adapter);
        //  adapter.notifyDataSetChanged();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Activity_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                Common_Variables.histori_id = (String) obj.get("Transaction_ID");
                startActivity(new Intent(Activity_Histories.this, View_History.class));
                //  Log.e("Yourtag", result);
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
        txtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(Activity_Histories.this,
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

    private void getLists() {
        if (lists != null) {
            lists.clear();
            adapter.notifyDataSetChanged();
        }
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // lists.clear();
                //  String value = dataSnapshot.getKey() + "-" + dataSnapshot.child("last_name").getValue().toString() ;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                        //   if(courseUID.child("CID").getValue()==FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                        if (postSnapShot.getKey().equals("CID")) {
                            //String cid= (String) postSnapShot.getValue();
                            // if(postSnapShot.child("CID").toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            String value1 = dataSnapshot.child("CID").getValue().toString();
                            if (value1.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                if (dataSnapshot.child("Date").exists()) {
                                    String dates = dataSnapshot.child("Date").getValue().toString();
                                    //    if (dates.equals(txtdate.getText().toString())) {
                                    String value2 = dataSnapshot.getKey();
                                    String Fare = dataSnapshot.child("Fare").getValue().toString();
                                    String Type = dataSnapshot.child("Type").getValue().toString();
                                    String dd = "", tt = "";
                                    String Rating = "";
                                    if (dataSnapshot.child("rating").getValue() != null) {
                                        Rating = dataSnapshot.child("rating").getValue().toString();
                                    }
                                    if (dataSnapshot.child("DID").getValue() != null) {
                                        did = dataSnapshot.child("DID").getValue().toString();
                                    }
                                    if (dataSnapshot.child("Date").getValue() != null) {
                                        dd = dataSnapshot.child("Date").getValue().toString();
                                    }
                                    if (dataSnapshot.child("Time").getValue() != null) {
                                        tt = dataSnapshot.child("Time").getValue().toString();
                                    }
                                    HashMap<String, String> datum = new HashMap<String, String>();
                                    datum.put("Transaction_ID", value2);
                                    datum.put("DT", dd + " " + tt);
                                    datum.put("Content", "Fare:" + Fare + ", Type:" + Type + ", Rating:" + Rating);
                                    lists.add(datum);
                                   adapter.notifyDataSetChanged();
                                    // }
                                }
                            }
                        }
                    }

                   // adapter.notifyDataSetChanged();
                }
                Collections.reverse(lists);

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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