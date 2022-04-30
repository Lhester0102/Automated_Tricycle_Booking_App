package com.lhester.polendey.trikila;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Announcement extends AppCompatActivity {
    private ImageView closeButton;
    ListView lv;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    ArrayList<HashMap<String, String>> lists;
    private ArrayList<String> sub_lists = new ArrayList<>();
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        lv = findViewById(R.id.history);
        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(Announcement.this, lists, R.layout.announcement_lists,
                new String[]{"announcement", "dateposted", "postedtime", "postedby"},
                new int[]{R.id.announcement, R.id.date_posted, R.id.posted_time, R.id.postedby});
        lv.setAdapter(adapter);
        //  adapter.notifyDataSetChanged();
    /*    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Transaction_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                Common_Var.histori_id = (String) obj.get("Transaction_ID");
                Common_Var.OperatorDriverID = (String) obj.get("Transaction_ID");
                startActivity(new Intent(Announcement.this, Transaction_Histories.class));
                Log.e("Yourtag", Common_Var.histori_id);
            }
        }); */


        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void getLists() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("Values", String.valueOf(dataSnapshot));
                if (dataSnapshot.exists()) {
                    // String value2 = dataSnapshot.getKey();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    String msg = "", pd = "", pt = "", pb = "";
                    if (map.get("message") != null) {
                        msg = map.get("message").toString();
                    }
                    if (map.get("date") != null) {
                        pd = map.get("date").toString();
                    }
                    if (map.get("time") != null) {
                        pt = map.get("time").toString();
                    }
                    if (map.get("postedbyname") != null) {
                        pb = map.get("postedbyname").toString();
                    }

                    HashMap<String, String> datum = new HashMap<String, String>();
                    datum.put("announcement", msg);
                    datum.put("dateposted", pd);
                    datum.put("postedtime", pt);
                    datum.put("postedby", pb);
                    lists.add(datum);
                    adapter.notifyDataSetChanged();
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