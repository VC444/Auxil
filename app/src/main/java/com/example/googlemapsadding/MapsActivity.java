package com.example.googlemapsadding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

    private GoogleMap mMap;
    private final String LOG_TAG = "CometTracker";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 100;
    private boolean permissionIsGranted = false;
    Marker curPosMarker;
    public String mUid;
    private double otherLat;
    private double otherLong;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    // DatabaseReference lat1 = mRootRef.child("ID").child("1").child("Position").child("Lat");
    // DatabaseReference long1 = mRootRef.child("ID").child("1").child("Position").child("Long");

    // Firebase Stuff

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPreferences sharedPref = getSharedPreferences("loginBool", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("TrueOrFalse", false);

        //READ if LoggedIN

        if(!isLoggedIn)
        {
            Intent loginIntent = new Intent(this, LoginPageActivity.class);
            startActivity(loginIntent);
        }

        /* Firebase Stuff

        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        */

        // Firebase Stuff

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        mRootRef.child("Users").orderByChild("Latitude").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                UserInfo info = dataSnapshot.getValue(UserInfo.class);
                Toast.makeText(MapsActivity.this, dataSnapshot.getKey() + "'s latitude is " + info.latitude, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        SharedPreferences uidPref = getSharedPreferences("userUid", Context.MODE_PRIVATE);
        String tempUid = uidPref.getString("Uid", "");
        this.mUid = tempUid;

        /* Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(10,19);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = LocationRequest.create(); // Another way to write a new object
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3 * 1000); // Always write in milliseconds
        requestLocationUpdates();
    }

    public void requestLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else
            {
                permissionIsGranted = true;
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        Log.i(LOG_TAG, location.toString());

        double tempLat = location.getLatitude();
        double tempLong = location.getLongitude();

        // mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

        // Send Location data to firebase
        String stringLat = Double.toString(tempLat);
        String stringLong = Double.toString(tempLong);
        mRootRef.child("Users").child(mUid).child("latitude").setValue(stringLat);
        mRootRef.child("Users").child(mUid).child("longitude").setValue(stringLong);

        // Read Data from the database

        DatabaseReference otherLatRef = mRootRef.child("Users").child("mtiP2QAIjiSj888TdmzJhUAIZm22").child("latitude");
        DatabaseReference otherLongRef = mRootRef.child("Users").child("mtiP2QAIjiSj888TdmzJhUAIZm22").child("longitude");

        otherLatRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String dbstringLat = dataSnapshot.getValue(String.class);
                double tempLat = Double.parseDouble(dbstringLat);
                otherLat = tempLat;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        otherLongRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String dbstringLong = dataSnapshot.getValue(String.class);
                double tempLong = Double.parseDouble(dbstringLong);
                otherLong = tempLong;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        LatLng latlng = new LatLng(otherLat, otherLong);

        MarkerOptions markerOptions = new MarkerOptions(); // MarkerOptions object to hold marker attributes
        markerOptions.position(latlng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title("Akshay Position");
        curPosMarker = mMap.addMarker(markerOptions); // Add marker with markerOptions options
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        CameraUpdateFactory.zoomTo(14);


        if (curPosMarker != null) // Keep only one marker for an object and delete past ones
        {
            mMap.clear();
        }

        curPosMarker = mMap.addMarker(markerOptions);

        /*
        MarkerOptions markerOptions = new MarkerOptions(); // MarkerOptions object to hold marker attributes
        markerOptions.position(latlng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title("Current Position");
        curPosMarker = mMap.addMarker(markerOptions); // Add marker with markerOptions options
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

        if (curPosMarker != null) // Keep only one marker for an object and delete past ones
        {
            mMap.clear();
        }

        curPosMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        */
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onResume()
    {
        super.onResume();
        if (permissionIsGranted)
        {
            if (mGoogleApiClient.isConnected())
            {
                requestLocationUpdates();
            }
        }
    }

    protected void onPause()
    {
        super.onPause();
        if (permissionIsGranted)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (permissionIsGranted)
            mGoogleApiClient.disconnect();
    }

    // Check if user granted required permissions and proceed
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission granted
                    permissionIsGranted = true;
                }
                else
                {
                    // permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(), "This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
                }
                break;

            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                break;
        }
    }
}