package com.lhester.polendey.drivers.Schedules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lhester.polendey.drivers.Common_Var;
import com.lhester.polendey.drivers.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class Msg2 extends AppCompatActivity {
    private Button btnsend;
    private EditText txtmsg;
    private DatabaseReference msgRef;
    ListView lv;
    private DatabaseReference databaseReference;
    ArrayList<HashMap<String, String>> lists;
    private SimpleAdapter adapter;
    private TextView t1,t2,t3,t4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgs);
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                // .child(CustomerMapActivity.driver_id_found)
                .child("Messages");


        lv = findViewById(R.id.listsmsgs);
        t1=findViewById(R.id.textView1);
        t2=findViewById(R.id.textView2);
        t3=findViewById(R.id.textView3);
        t4=findViewById(R.id.textView4);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtmsg.setText(txtmsg.getText().toString() + " " + t1.getText().toString());
            }
        });
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtmsg.setText(txtmsg.getText().toString() + " " + t2.getText().toString());
            }
        });
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtmsg.setText(txtmsg.getText().toString() + " " + t3.getText().toString());
            }
        });
        t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtmsg.setText(txtmsg.getText().toString() + " " + t4.getText().toString());
            }
        });

        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(Msg2.this, lists, R.layout.msglists,
                new String[]{"messageC", "messageD"},
                new int[]{R.id.sent, R.id.received}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);

                if ((String) obj.get("messageD")!="") {

                    // findViewById(R.id.received).setBackgroundResource(R.drawable.messaged);
                    itemView.findViewById(R.id.received).setBackgroundResource(R.drawable.messagec);
                    itemView.findViewById(R.id.received).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.sent).setVisibility(View.GONE);

                }
                if ((String) obj.get("messageC")!="") {
                    //   findViewById(R.id.sent).setBackgroundResource(R.drawable.messagec);
                    itemView.findViewById(R.id.sent).setBackgroundResource(R.drawable.messaged);
                    itemView.findViewById(R.id.sent).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.received).setVisibility(View.GONE);
                }


                return itemView;
            }
        };

        lv.setAdapter(adapter);

        Log.e("count", String.valueOf(lv.getAdapter().getCount()));


        btnsend = findViewById(R.id.btnsend);
        txtmsg = findViewById(R.id.txtmsg);
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        // .child(CustomerMapActivity.driver_id_found)
                        .child("Messages");

                DateFormat dfgmt = new java.text.SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                dfgmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                String timteStamp = dfgmt.format(new Date());

                String userkey = msgRef.push().getKey();
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("messageD", txtmsg.getText().toString());
                // userMap.put("DID",Common_Variables.drivers_id);
                userMap.put("CID", Common_Var.customerID2);
                userMap.put("key", userkey);
                userMap.put("timeStamp", timteStamp);
                userMap.put("seen", "False");
                DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers")
                        // .child(CustomerMapActivity.driver_id_found)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Messages").child(userkey);

                newmsg.updateChildren(userMap);
                txtmsg.setText("");
                Toast.makeText(Msg2.this, "sent", Toast.LENGTH_SHORT).show();
                //    getLists();
            }
        });
    }

    private void getLists() {
//        lists.clear();
        databaseReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        if (postSnapShot.getKey().equals("CID")) {
                            String value1 = dataSnapshot.child("CID").getValue().toString();
                            if (value1.equals(Common_Var.customerID2)) {
                                // String value2 = dataSnapshot.getKey();
                                String messageC = "";
                                String messageD = "";
                                String seen = "";
                                if (dataSnapshot.child("messageD").exists()) {
                                    messageD = dataSnapshot.child("messageD").getValue().toString();

                                }
                                if (dataSnapshot.child("messageC").exists()) {
                                    messageC = dataSnapshot.child("messageC").getValue().toString();
                                    if (dataSnapshot.child("seen").exists()) {
                                        seen = dataSnapshot.child("seen").getValue().toString();
                                    }
                                    if(seen.equals("False")) {
                                      //  update_seen(dataSnapshot.child("key").getValue().toString());
                                    }
                                }

                                HashMap<String, String> datum = new HashMap<String, String>();
                                //  datum.put("Images", value2);
                                datum.put("messageD", messageD);
                                datum.put("messageC", messageC);
                                lists.add(datum);
                                adapter.notifyDataSetChanged();

                            }

                        }
                    }
                }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
            .child("Users").child("Drivers")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .child("Messages");
    private void update_seen(String key) {
        Log.e("MSGKEY",key);
        newmsg.child(key);
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("seen", "True");
        newmsg.updateChildren(userMap);
    }
}