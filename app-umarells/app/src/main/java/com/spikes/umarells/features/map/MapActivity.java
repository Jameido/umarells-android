package com.spikes.umarells.features.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spikes.umarells.R;
import com.spikes.umarells.features.add.AddBuildingSiteActivity;
import com.spikes.umarells.models.BuildingSite;
import com.spikes.umarells.shared.AppCompatActivityExt;
import com.spikes.umarells.shared.PositionManager;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

import static com.spikes.umarells.shared.Constants.RC_ADD_BUILDING_SITE;

public class MapActivity extends AppCompatActivityExt
        implements OnMapReadyCallback, PositionManager.OnPositionListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context) {
        Intent startIntent = new Intent(context, MapActivity.class);
        return startIntent;
    }

    @BindView(R.id.fab_add_building_site)
    FloatingActionButton mFabAdd;

    private GoogleMap mMap;
    private DatabaseReference mBuildingSitesDatabase;
    private ChildEventListener mBuildingSiteEventListener;
    private Map<String, Marker> mMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBuildingSiteEventListener) {
            mBuildingSitesDatabase.removeEventListener(mBuildingSiteEventListener);
        }
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        initDataSource();
    }

    @Override
    protected void onAuthenticationSuccessful(FirebaseUser user) {
        super.onAuthenticationSuccessful(user);
        mFabAdd.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        //mFabAdd.setVisibility(View.GONE);
    }

    @Override
    public void onPositionChanged(Location location) {

    }

    @OnClick(R.id.fab_add_building_site)
    void addNewBuildingSite(){
        startActivityForResult(AddBuildingSiteActivity.getStartIntent(this), RC_ADD_BUILDING_SITE);
    }

    private void initDataSource() {
        mBuildingSitesDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_sites");

        mBuildingSiteEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                BuildingSite buildingSite = dataSnapshot.getValue(BuildingSite.class);
                changeMarker(dataSnapshot.getKey(), buildingSite.getLat(), buildingSite.getLng(), buildingSite.getName());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                BuildingSite buildingSite = dataSnapshot.getValue(BuildingSite.class);
                changeMarker(dataSnapshot.getKey(), buildingSite.getLat(), buildingSite.getLng(), buildingSite.getName());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarker(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mBuildingSitesDatabase.addChildEventListener(mBuildingSiteEventListener);
    }

    private void addMarker(String id, double latitude, double longitude, String name) {
        mMarkers.put(
                id,
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(name))
        );
    }

    private void removeMarker(String id) {
        Marker markerToRemove = mMarkers.get(id);
        if (null != markerToRemove) {
            markerToRemove.remove();
            mMarkers.remove(id);
        }
    }

    private void changeMarker(String id, double latitude, double longitude, String name) {
        if (mMarkers.containsKey(id)) {
            Marker marker = mMarkers.get(id);
            marker.setPosition(new LatLng(latitude, longitude));
            marker.setTitle(name);
        } else {
            addMarker(id, latitude, longitude, name);
        }
    }
}
