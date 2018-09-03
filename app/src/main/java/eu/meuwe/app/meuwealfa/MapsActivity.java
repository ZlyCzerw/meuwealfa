package eu.meuwe.app.meuwealfa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;

    private static final int PERMISSION_ACCESS_COARSE_LOCATION =10;
    private static final int DEFAULT_ZOOM =15;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
            }
        };

        mMap.setOnMapLongClickListener(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000); // two minute interval
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        // Add a marker in Sydney and move the camera
        final LatLng sydney = new LatLng(-34, 151);

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Add My Location Pointer on the map
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);   //add the MyLocationButton on the map
            mMap.setOnMyLocationButtonClickListener(this); //add event on My Location Button click
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null); //send location request to system, and register Callback
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }

        UpdateLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);   //add the MyLocationButton on the map
            mMap.setOnMyLocationButtonClickListener(this); //add event on My Location Button click
        }
    }

    private void UpdateLocation()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null) {
                        mLastLocation = location;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude()), DEFAULT_ZOOM));
                        //mMap.addMarker(new MarkerOptions().position(
                        //        new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
       UpdateLocation();
        return false;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(1);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        UpdateLocation();
        Intent mMeuweActivity = new Intent(MapsActivity.this, MeuweActivity.class);
        mMeuweActivity.putExtra("Latitude",mLastLocation.getLatitude());
        mMeuweActivity.putExtra("Longitude",mLastLocation.getLongitude());
        startActivity(mMeuweActivity);
    }
}
