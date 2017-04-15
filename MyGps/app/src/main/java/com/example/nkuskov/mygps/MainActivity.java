package com.example.nkuskov.mygps;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ViewPropertyAnimatorCompatSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {

    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    TextView yourAddress;

    Geocoder geocoder;


    LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        yourAddress = (TextView) findViewById(R.id.locationLatitude);

        geocoder = new Geocoder(this, Locale.getDefault());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
            }, 1);
            return;
        }

        checkLocation();
    }


    @Override
    public void onLocationChanged(Location location) {
        showLocation(location);
        getAddress(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            tvStatusGPS.setText("Status: " + String.valueOf(status));
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            tvStatusNet.setText("Status: " + String.valueOf(status));
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        checkEnabled();
        showLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        Toast.makeText(getBaseContext(), "GPS is turned on",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "GPS is turnedoff",Toast.LENGTH_SHORT).show();
    }


    public void onRequestPermissionResult(int requestCode, String permission[], int[] grantResult) {
        switch (requestCode) {
            case 1: {
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocation();
                }
                return;
            }
        }
    }


    private void checkLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, this);
    }

    /**
     * Method for formatting location into readable view
     *
     * @param location
     * @return
     */
    public String formatLocation(Location location) {
        if (location == null) {
            return " ";
        }
        return String.format("Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
    }

    /**
     * Method for setting formatted location into TextView
     *
     * @param location
     */
    private void showLocation(Location location) {
        if (location == null) {
            return;
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    /**
     * Method For checking Enabled or Disabled GPS and NETWORK Providers
     */
    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Method for Location Settings Button
     * @param view
     */
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * Method for window switch
     */
    public void onClickToMap(View view){
        startActivity(new Intent(MainActivity.this, MapsActivity.class));
    }


    /**
     * Method for getting your address from your coordinates
     */
    private void getAddress(Location location){
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        yourAddress.setText(addresses.get(0).getAddressLine(0) + "\n" + addresses.get(0).getLocality() + "\n" +addresses.get(0).getAdminArea() + "\n" + addresses.get(0).getCountryName());
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();
    }


}
