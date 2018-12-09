package eu.meuwe.app.meuwealfa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import static com.google.maps.android.ui.IconGenerator.STYLE_RED;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener{


    private class meuweClusterItem implements ClusterItem {
        private final LatLng mPosition;
        private final String mTitle;
        private final String mTag;

        public meuweClusterItem(LatLng position, String title, String tag) {
            this.mPosition = position;
            this.mTitle = title;
            this.mTag = tag;
        }


        public LatLng getPosition() {
            return mPosition;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getSnippet() {
            return null;
        }

        public String getTag() {
            return mTag;
        }
    }

    private class meuweClusterRenderer extends DefaultClusterRenderer<meuweClusterItem>{
        private final IconGenerator iconGenerator = new IconGenerator(getApplicationContext());

        public meuweClusterRenderer() {
            super(getApplicationContext(), mMap, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(meuweClusterItem item, MarkerOptions markerOptions) {
            //super.onBeforeClusterItemRendered(item, markerOptions);

            Bitmap icon = iconGenerator.makeIcon(item.getTitle());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<meuweClusterItem> cluster) {
            return cluster.getSize() >1;
        }

    }
    //UI declarations
    private GoogleMap mMap;
    private NavigationView navigationView;
    private Menu navigationMenu;
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView searchTag;

    private static final int PERMISSION_ACCESS_COARSE_LOCATION =10;
    private static final int DEFAULT_ZOOM =15;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private LatLng cameraPosition;
    private HashSet <String> localTags = new HashSet<>();
    private HashSet <String> filterTags = new HashSet<>();
    private LatLngBounds cameraBounds;

    //Maps Utils
    private IconGenerator iconGenerator;
    private ClusterManager<meuweClusterItem> clusterManager;

    //Firebase variables
    private FirebaseFirestore firestore;
    private CollectionReference postsRef;
    private List<Post> localPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Initialise screen with data from database
        firestore = FirebaseFirestore.getInstance();
        // Create a reference to the posts collection
        postsRef = firestore.collection("posts");



        //get UI reference
        navigationView = findViewById(R.id.nav_view);
        navigationMenu = navigationView.getMenu();
        navigationMenu.setGroupCheckable(R.id.tag_group,true,false);
        drawerLayout = findViewById(R.id.drawer_layout);


    /**
     *  This is what happens when we open the navigation drawer on the left
     *  we fill the menu list with tags of the markers available in the visible screen
     */
        drawerLayout.addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                navigationMenu.clear();
                for (String tag : localTags)
                {
                    navigationMenu.add(tag).setChecked(filterTags.contains(tag));
                }
                /** Add an adapter to the search line in side drawer
                 * to help user find proper tags
                 */
                List <String> tagsList = new ArrayList<>(localTags); //ArrayAdapter doesn't accept hashset
        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.tagsList));*/
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_dropdown_item_1line, tagsList);
                searchTag = findViewById(R.id.searchTag);
                searchTag.setAdapter(adapter);

                searchTag.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //Refresh navigationMenu list with only the ones that contain CharSequence s
                        navigationMenu.clear();
                        for (String tag : localTags)
                        {
                            if(tag.contains(s.toString().toUpperCase()))//all the tags must be in Upper Case!
                                navigationMenu.add(tag).setChecked(filterTags.contains(tag));
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                refreshMarkers();
            }
        }
        );

        /**
         * Handling of the selection of tags in the drawer menu
         * We check the selected tag and add it to selected Tags list
         */

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //toggle menu item
                    if(menuItem.isChecked())
                    {
                        menuItem.setChecked(false);
                        filterTags.remove(menuItem.getTitle().toString());

                    }
                    else
                    {
                        menuItem.setChecked(true);
                        filterTags.add(menuItem.getTitle().toString());
                    }

                    return true;
                }
        });


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
        //Applying map style
        boolean result = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.maps_style_meuwe1));

        if(result)result = true;

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

        updateLocation();


        //Map utils
        //Generation of icons with titles
        iconGenerator = new IconGenerator(this);
        iconGenerator.setStyle(STYLE_RED);
        //clustering large amount of markers
        clusterManager = new ClusterManager<meuweClusterItem>(this, mMap);
        clusterManager.setAnimation(true);
        clusterManager.setRenderer(new meuweClusterRenderer());

        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<meuweClusterItem>() {
            @Override
            public boolean onClusterItemClick(meuweClusterItem meuweClusterItem) {
                //get UUID out of marker tag
                Intent intent = new Intent(MapsActivity.this, DisplayMeuweActivity.class);
                intent.putExtra("EventUUID", meuweClusterItem.getTag());
                startActivity(intent);
                return true;
            }
        });

    }

    /**
     *  Handle what happens when user agrees on the required permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

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

    private void updateLocation()
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
                        // Look for all posts nearby and add a marker
                        refreshMarkers();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    /** Refresh markers in the visible part of map
     *
     */
    private void refreshMarkers() {
        cameraPosition = mMap.getCameraPosition().target;
        cameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        //find all markers within range
        if(cameraBounds.southwest.longitude <= cameraBounds.northeast.longitude) {
            //postsRef.whereGreaterThanOrEqualTo("latitude", cameraBounds.southwest.latitude)
            //        .whereLessThanOrEqualTo("latitude", cameraBounds.northeast.latitude);
            //TODO firestore doesnt work with such complex query, think of a workaround
            postsRef.whereGreaterThanOrEqualTo("longitude", cameraBounds.southwest.longitude)
                    .whereLessThanOrEqualTo("longitude", cameraBounds.northeast.longitude);
        }
        else
        {
            postsRef
                    //.whereGreaterThanOrEqualTo("latitude", cameraBounds.southwest.latitude)
                    //.whereLessThanOrEqualTo("latitude", cameraBounds.northeast.latitude)
                    .whereGreaterThanOrEqualTo("longitude", cameraBounds.northeast.longitude)
                    .whereLessThanOrEqualTo("longitude", cameraBounds.southwest.longitude);
        }
        postsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                localPosts = queryDocumentSnapshots.toObjects(Post.class);

                //put a marker for each local post in range
                clusterManager.clearItems();
                filterTags = getCheckedMenuItems(navigationMenu);
                for(Post onePost:localPosts)
                {
                    LatLng markerPosition = new LatLng(onePost.getLatitude(), onePost.getLongitude());
                    if (cameraBounds.contains(markerPosition)//TODO this is workaround, but is done on application side, not server. It may cause problems with a lot of markers
                            && onePost.getTags().containsAll(filterTags))
                    {//TODO add marker filtering on tag list

                           /*mMap.addMarker(new MarkerOptions()
                                .position(markerPosition)
                                //Generate Marker Icon on the go with title
                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(onePost.getTitle()))))
                                .setTag(onePost.getUuid());*/
                        meuweClusterItem item = new meuweClusterItem(markerPosition,onePost.getTitle(),onePost.getUuid());
                        clusterManager.addItem(item);
                        //add post tags to hash set
                        //hashset removes duplicates
                        localTags.addAll(onePost.getTags());
                    }
                }
                clusterManager.cluster();
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
        updateLocation();
        refreshMarkers();
        return false;
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(MapsActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        updateLocation();
        Intent mMeuweActivity = new Intent(MapsActivity.this, MeuweActivity.class);
        mMeuweActivity.putExtra("Latitude",latLng.latitude);
        mMeuweActivity.putExtra("Longitude",latLng.longitude);
        startActivity(mMeuweActivity);
    }

    /**
     *
     */

    @Override
    public void onCameraIdle() {
        refreshMarkers();
        clusterManager.onCameraIdle();
    }

    /** Retrieve HashSet of checked items from Menu
     *
     * @param menu  object to scan for items
     * @return Hashset of Strings with all the checked items
     */
    private HashSet <String> getCheckedMenuItems(Menu menu)
    {
        HashSet <String> filterTags = new HashSet<>();
        for(int i=0;i<menu.size();i++)
        {
            if(menu.getItem(i).isChecked())
                filterTags.add(menu.getItem(i).getTitle().toString());
        }
        return filterTags;
    }



}
