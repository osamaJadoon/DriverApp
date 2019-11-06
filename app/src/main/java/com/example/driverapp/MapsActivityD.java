package com.example.driverapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivityD extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    DatabaseReference driversRef;
    Marker mCurrent;

    SupportMapFragment mapFragment;

    LatLng currentLocation;
    LatLng driverLocation;

    double currentLatitude;
    double currentLongitude;


    DLocation dLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapsd);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dLocation = new DLocation();



    }



    //back press function start here

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("EXIT?")
                .setIcon(R.drawable.exit)
                .setMessage("Are you sure...")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finishAffinity();
                    }
                })

                .create().show();
    }

    //back press ends.....


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }
        mMap.setMyLocationEnabled(true);

        double latitude = 34.2010552;
        double longitude = 73.1622682;
        LatLng latLng  = new LatLng(latitude,longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        currentLocation = new LatLng(currentLatitude, currentLongitude);


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        mMap.clear();
        markerOptions.title("You");
        markerOptions.snippet("Your Current location");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrent = mMap.addMarker(markerOptions);

        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(34.1967604,73.2264985))
                .add(new LatLng(34.1977509,73.2291002))
                .add(new LatLng(34.194691,73.2352134))
                .add(new LatLng(34.155799,73.2171863))
                .add(new LatLng(34.16514,73.2607653))
                .width(5f)
                .color(Color.BLUE)
        );

        saveLocationToFirebase();


    }

    private void saveLocationToFirebase() {

        driversRef = FirebaseDatabase.getInstance().getReference().child("123");

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();

        driverLocation = new LatLng(latitude, longitude);

//        dLocation.setLatitude(latitude);
//        dLocation.setLongitude(longitude);


        // driversRef.push().setValue(driverLocation);
        driversRef.setValue(driverLocation);
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        driversRef = FirebaseDatabase.getInstance().getReference().child("123");
        double latitude = 00.00;
        double longitude = 00.00;

        driverLocation = new LatLng(latitude,longitude);
        driversRef.setValue(driverLocation);
        super.onDestroy();
    }

//    public void StopDriving(View view) {
//        driversRef = FirebaseDatabase.getInstance().getReference().child("123");
//        double latitude = 00.00;
//        double longitude = 00.00;
//
//        driverLocation = new LatLng(latitude,longitude);
//        new AlertDialog.Builder(this)
//                .setTitle("Confirmation")
//                .setMessage("Are u sure you want to Stop Driving")
//                .setPositiveButton("Yes, Stop Driving", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        driversRef.setValue(driverLocation);
//                        driversRef.endAt(true);
//                        driversRef.startAt(false);
//                        //finish();
//                        finishAffinity();
//
//
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                })
//                .create().show();
//
//
//    }

}
