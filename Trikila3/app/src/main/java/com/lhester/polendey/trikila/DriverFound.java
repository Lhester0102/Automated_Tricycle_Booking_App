package com.lhester.polendey.trikila;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DriverFound extends AppCompatActivity {
    private TextView f,t,d,ef;
    private Button btnok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_found);
        ef=findViewById(R.id.fare);
        d=findViewById(R.id.dis);
        btnok=findViewById(R.id.button);

        f.setText("Estimated Fare:"+ Common_Variables.estimated_fare);
        d.setText("Distance:" + Common_Variables.distance+" km");
        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}