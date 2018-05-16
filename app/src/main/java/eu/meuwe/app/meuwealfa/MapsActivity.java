package eu.meuwe.app.meuwealfa;

import android.content.Intent;
import android.view.WindowManager;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private Location mLocation;
    double latitude, longitude;
    private Marker myMarker;
    private Marker fakeMarker;
    private Marker fakeMarker2;
    private Marker fakeMarker3;
    private Marker fakeMarker4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);



        gpsTracker = new GPSTracker(getApplicationContext());// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mLocation = gpsTracker.getLocation();

        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker and move the camera

        LatLng myPosition = new LatLng(latitude,longitude);
        myMarker = mMap.addMarker(new MarkerOptions().position(myPosition)
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.logoredsmall))
                .title("meuwe")
                .anchor(0.5f,0.5f));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16));
        myMarker.setTag(0);
        mMap.setOnMarkerClickListener(this);
       // mMap.getMinZoomLevel(); //Not yet running. Movable field of view has to be limited to 1km.
        mMap.getUiSettings().setScrollGesturesEnabled(false); // disables scrolling
       // mMap.getUiSettings().setZoomGesturesEnabled(false); // disables zooming in and out
        mMap.setMaxZoomPreference(17f);
        mMap.setMinZoomPreference(15f);
        MapStyleOptions styleOptions=MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style_meuwe1);
        mMap.setMapStyle(styleOptions);
        fakeMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(50.031689, 22.006073))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconhuman1))
                .title("Longboard")
                .anchor(0.5f,0.5f));
        fakeMarker2 = mMap.addMarker(new MarkerOptions().position(new LatLng(50.031552, 22.009977))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconhuman2))
                .title("Spacer z psem")
                .anchor(0.5f,0.5f));
        fakeMarker3 = mMap.addMarker(new MarkerOptions().position(new LatLng(50.032579, 22.012347))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconhuman3))
                .title("Piwo")
                .anchor(0.5f,0.5f));
        fakeMarker4 = mMap.addMarker(new MarkerOptions().position(new LatLng(50.030085, 22.008550))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconhuman4))
                .title("Pomoc w domu")
                .anchor(0.5f,0.5f));
    }


    @Override

    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        //Integer clickCount = (Integer) myMarker.getTag(); // To be used later, when i want to add activity upon every next click.

        if (marker.equals(myMarker))
        {
            Intent intent =new Intent(MapsActivity.this, CameraPreview.class);
            startActivity(intent);
        }
        if (marker.equals(fakeMarker))
        {
            Intent intent2 =new Intent(MapsActivity.this, AniaIconActivity.class );
            startActivity(intent2);
        }
        /*
        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            myMarker.setTag(clickCount);
            Toast.makeText(this,
                    myMarker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show(); //Toast is a popup grey field
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
         */
        return false;
    }



}
