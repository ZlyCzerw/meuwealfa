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
import android.widget.Toast;


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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    private static final int PERMISSION_ACCESS_COARSE_LOCATION =10;
    private static final int DEFAULT_ZOOM =15;
    private static final int MARKERS_EXTENT = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private LatLng cameraPosition;

    //Firebase variables
    private FirebaseFirestore firestore;
    private CollectionReference postsRef;
    private List<Post> localPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialise screen with data from database
        firestore = FirebaseFirestore.getInstance();
        // Create a reference to the posts collection
        postsRef = firestore.collection("posts");

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

        // Look for all posts nearby and add a marker


        //mMap.addMarker(new MarkerOptions().position(
        //        new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
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
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * This is what happens when user clicks on the info window of selected marker
     * Opens DisplayMeuweActivity and passes unique UUID of the post.
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        //get UUID out of marker tag
        Intent intent = new Intent(MapsActivity.this, DisplayMeuweActivity.class);
        intent.putExtra("EventUUID",(String) marker.getTag());
        startActivity(intent);
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

    /**
     * Handle update of FusedLocationProviderClient and move camera to user location
     */

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

                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    /** Refresh markers in the visible map
     *
     */
    private void refreshMarkers() {
        cameraPosition = mMap.getCameraPosition().target;
        LatLngBounds cameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //return this.southwest.latitude <= var4 && var4 <= this.northeast.latitude && this.zza(var1.longitude);
        /*if (this.southwest.longitude <= this.northeast.longitude) {
            return this.southwest.longitude <= var1 && var1 <= this.northeast.longitude;
        } else {
            return this.southwest.longitude <= var1 || var1 <= this.northeast.longitude;
        }*/
        //find all markers within range
        if(cameraBounds.southwest.longitude <= cameraBounds.northeast.longitude) {
            postsRef.whereGreaterThanOrEqualTo("latitude", cameraBounds.southwest.latitude)
                    .whereLessThanOrEqualTo("latitude", cameraBounds.northeast.latitude);
            postsRef.whereGreaterThanOrEqualTo("longitude", cameraBounds.southwest.longitude)
                    .whereLessThanOrEqualTo("longitude", cameraBounds.northeast.longitude);
        }
        else
        {
            postsRef.whereGreaterThanOrEqualTo("latitude", cameraBounds.southwest.latitude)
                    .whereLessThanOrEqualTo("latitude", cameraBounds.northeast.latitude)
                    .whereGreaterThanOrEqualTo("longitude", cameraBounds.northeast.longitude)
                    .whereLessThanOrEqualTo("longitude", cameraBounds.southwest.longitude);
        }
        postsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                localPosts = queryDocumentSnapshots.toObjects(Post.class);
                //put a marker for each local post in range
                for(Post onePost:localPosts)
                {
                    LatLng markerPosition = new LatLng(onePost.getLatitude(), onePost.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(markerPosition)
                            .title(onePost.getTitle())).setTag(onePost.getUuid());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onMyLocationButtonClick() {
       UpdateLocation();
       refreshMarkers();
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
        mMeuweActivity.putExtra("Latitude",latLng.latitude);
        mMeuweActivity.putExtra("Longitude",latLng.longitude);
        startActivity(mMeuweActivity);
    }
}
