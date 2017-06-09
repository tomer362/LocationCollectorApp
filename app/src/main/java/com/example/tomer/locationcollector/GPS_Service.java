package com.example.tomer.locationcollector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class GPS_Service extends Service {
    private LocationListener listener;
    private LocationManager locationManager;
    private String filenameToSaveTo;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Date newDate = new Date(System.currentTimeMillis());
        filenameToSaveTo = newDate.toString() + ".txt";
        Toast.makeText(this, "Saved Here: " + getExternalFilesDir("MyFileStorage").toString() + "\\" + filenameToSaveTo, Toast.LENGTH_SHORT).show();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("latValue", location.getLatitude());
                i.putExtra("longValue", location.getLongitude());
                sendBroadcast(i);


                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                try {
                    File myExternalFile = new File(getExternalFilesDir("MyFileStorage"), filenameToSaveTo);

                    FileOutputStream fos = new FileOutputStream(myExternalFile, true);
                    fos.write(("[" + currentDateTimeString + "]" + "Latitude: " + location.getLatitude() + ", Longtitude: " + location.getLongitude() + "\n").getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }
}
