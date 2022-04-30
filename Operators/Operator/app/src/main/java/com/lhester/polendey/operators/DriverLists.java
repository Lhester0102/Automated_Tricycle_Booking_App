package com.lhester.polendey.operators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverLists extends AppCompatActivity {
    private ImageView closeButton;
    ListView lv;
    SearchView sv;

    ArrayAdapter<String> larray;
    String[] data = {"sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas", "sdsa", "dsadas"};
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    ArrayList<HashMap<String, String>> lists;
    //private String[] titleArray,subItemArray;
    private ArrayList<String> sub_lists = new ArrayList<>();
    private SimpleAdapter adapter;
    String did = "";
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_lists);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Operators")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Drivers");
        lv = findViewById(R.id.history);
        getUserInformation();
        naviations();
        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(DriverLists.this, lists, R.layout.lists_layout,
                new String[]{"image","Transaction_ID", "Content"},
                new int[]{R.id.imageView1ID,R.id.TID, R.id.child}){
            @SuppressLint("WrongViewCast")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                String uri=  obj.get("image").toString();
                if (obj.get("image")!="") {
                 //   itemView.findViewById(R.id.received).setBackgroundResource(R.drawable.messagec);

                    Picasso.get().load(uri)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into((CircleImageView) itemView.findViewById(R.id.imageView1ID));
                }


                return itemView;
            }
        };
        
        lv.setAdapter(adapter);
        //  adapter.notifyDataSetChanged();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Transaction_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                Common_Var.histori_id = (String) obj.get("Transaction_ID");
                Common_Var.OperatorDriverID = (String) obj.get("Transaction_ID");
                startActivity(new Intent(DriverLists.this, Transaction_Histories.class));
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


    }
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    private CircleImageView userImage;
    private TextView txtfullname;
    private void naviations() {
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        txtfullname = header.findViewById(R.id.fullname);
        userImage = header.findViewById(R.id.userImage);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.profile:
                        startActivity(new Intent(DriverLists.this, SettingsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
               /*     case R.id.driver:
                        startActivity(new Intent(DriverLists.this, DriverLists.class));
                        drawerLayout.closeDrawers();
                        break; */

                    case R.id.notif:
                        startActivity(new Intent(DriverLists.this, Announcement.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        drawerLayout.closeDrawers();
                        new SweetAlertDialog(DriverLists.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Log out")
                                .setContentText("You sure you want to log out?")
                                .setConfirmText("Logout")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        finish();
                                        FirebaseAuth.getInstance().signOut();
                                        drawerLayout.closeDrawers();
                                        startActivity(new Intent(DriverLists.this, LogInDriver.class));
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();

                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    private void getLists() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("Values", String.valueOf(dataSnapshot));
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                        if (postSnapShot.getKey().equals("uid")) {
                            String value2 = dataSnapshot.getKey();
                            //  datum.put("Images", value2);
                            getDriverinfo(value2);

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

    private void getDriverinfo(final String driverId) {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    String full_name = "",mn= "",fn= "",ln= "",img="";
                    if (map.get("lastname") != null) {
                        ln = map.get("lastname").toString();
                    }
                    if (map.get("firstname") != null) {
                        fn = map.get("firstname").toString();
                    }
                    if (map.get("middlename") != null) {
                        mn =map.get("middlename").toString();
                    }
                    if (map.get("image") != null) {
                        img =map.get("image").toString();
                    }
                    full_name=fn + " " + mn+" "+ ln;
                    HashMap<String, String> datum = new HashMap<String, String>();
                    datum.put("Transaction_ID", driverId);
                    datum.put("Content", full_name);
                    datum.put("image", img);
                    lists.add(datum);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void getUserInformation() {
        DatabaseReference databaseReference2=FirebaseDatabase.getInstance().getReference().child("Users").child("Operators");
        databaseReference2.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            String ln, fn, mn;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    if (dataSnapshot.hasChild("firstname")) {
                        fn = dataSnapshot.child("firstname").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("lastname")) {
                        ln = dataSnapshot.child("lastname").getValue().toString();

                    }
                    if (dataSnapshot.hasChild("middlename")) {
                        mn = dataSnapshot.child("middlename").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(userImage);
                    }
                    txtfullname.setText(ln + ", " + fn + " " + mn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}