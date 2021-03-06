package com.example.tomer.locationcollector;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private TextView tvLat, tvLong;
    private BroadcastReceiver broadcastReceiver;
    private ListView lvLocations;
    ArrayList<String> locationsList;
    ArrayAdapter<String> adp;
    private boolean isRecording = false;


    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double latitude, longtitude;
                    latitude = (double)intent.getExtras().get("latValue");
                    longtitude = (double)intent.getExtras().get("longValue");
                    tvLat.setText("Lat: " + latitude);
                    tvLong.setText("Long: " + longtitude);

                    locationsList.add("Latitude: " + latitude + ", Longtitude: " + longtitude);
                    adp.notifyDataSetChanged();
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button)findViewById(R.id.startBtn);
        stopBtn = (Button)findViewById(R.id.stopBtn);
        tvLat = (TextView)findViewById(R.id.tvLat);
        tvLong = (TextView)findViewById(R.id.tvLong);
        lvLocations = (ListView) findViewById(R.id.lvLocations);
        locationsList = new ArrayList<String>();

        adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locationsList);
        lvLocations.setAdapter(adp);
        if(!runtime_permissions())
        {
            enable_buttons();
        }
    }

    private void enable_buttons(){
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording == false) {

                    Intent i = new Intent(getApplicationContext(), GPS_Service.class);

                    startService(i);

                    isRecording = true;
                }
                else{
                    Toast.makeText(MainActivity.this, "Already Recording Location Data!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLat.setText("Lat: None");
                tvLong.setText("Long: None");
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                stopService(i);
                locationsList.clear();
                adp.notifyDataSetChanged();

                isRecording = false;
            }
        });
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                enable_buttons();
            else{
                runtime_permissions();
            }
        }
    }
}
