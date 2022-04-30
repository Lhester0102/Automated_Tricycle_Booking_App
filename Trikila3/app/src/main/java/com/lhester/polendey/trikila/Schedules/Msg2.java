package com.lhester.polendey.trikila.Schedules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
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
import com.google.firebase.database.ServerValue;
import com.lhester.polendey.trikila.Common_Variables;
import com.lhester.polendey.trikila.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg2);
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                .child(Common_Variables.drivers_id2)
                // .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
                .child("Messages");

        lv = findViewById(R.id.listsmsgs);

        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(Msg2.this, lists, R.layout.msglists,
                new String[]{"messageD", "messageC"},
                new int[]{R.id.received, R.id.sent}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);

                if ((String) obj.get("messageD") != "") {

                    // findViewById(R.id.received).setBackgroundResource(R.drawable.messaged);
                    itemView.findViewById(R.id.received).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.sent).setVisibility(View.GONE);
                    itemView.findViewById(R.id.received).setBackgroundResource(R.drawable.messaged);

                }
                if ((String) obj.get("messageC") != "") {
                    //   findViewById(R.id.sent).setBackgroundResource(R.drawable.messagec);
                    itemView.findViewById(R.id.sent).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.received).setVisibility(View.GONE);
                    itemView.findViewById(R.id.sent).setBackgroundResource(R.drawable.messagec);
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
                        .child(Common_Variables.drivers_id2)
                        //  .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
                        .child("Messages");
                String userkey = msgRef.push().getKey();

                DateFormat dfgmt = new java.text.SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                dfgmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                String timteStamp = dfgmt.format(new Date());


                Log.e("time", timteStamp);
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("messageC", txtmsg.getText().toString());
                // userMap.put("DID",Common_Variables.drivers_id);
                userMap.put("CID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                userMap.put("timeStamp", timteStamp);
                userMap.put("seen", "False");
                userMap.put("key", userkey);
                DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers")
                        .child(Common_Variables.drivers_id2)
                        //  .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
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
                            if (value1.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                // String value2 = dataSnapshot.getKey();
                                String messageC = "";
                                String messageD = "";
                                String seen = "";
                                if (dataSnapshot.child("messageD").exists()) {
                                    messageD = dataSnapshot.child("messageD").getValue().toString();

                                    if (dataSnapshot.child("seen").exists()) {
                                        seen = dataSnapshot.child("seen").getValue().toString();
                                    }
                                    if(seen.equals("False")) {
                                      //  update_seen(dataSnapshot.child("key").getValue().toString());
                                    }
                                }
                                if (dataSnapshot.child("messageC").exists()) {
                                    messageC = dataSnapshot.child("messageC").getValue().toString();
                                }

                                Log.e("seen", seen);
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

    MediaPlayer notificationPlayer2;

    private void playNotification2() {
        if (notificationPlayer2 == null) {
            notificationPlayer2 = MediaPlayer.create(this, R.raw.msgtone);
            notificationPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }
        notificationPlayer2.start();
    }

    private void stopPlayer2() {
        if (notificationPlayer2 != null) {
            notificationPlayer2.release();
            notificationPlayer2 = null;
            //Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }

    private void update_seen(String key) {
        DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers")
                // .child("SWMcPAoFjmfLUIqTwrhtK5S10R92")
                .child(Common_Variables.drivers_id2)
                //.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Messages").child(key);
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("seen", "True");
        newmsg.updateChildren(userMap);
    }
}