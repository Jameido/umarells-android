/*
 * Copyright 2017.  Luca Rossi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.spikes.umarells.features.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spikes.easylocationmanager.ActivityEasyLocationManager;
import com.spikes.easylocationmanager.EasyLocationManager;
import com.spikes.umarells.R;
import com.spikes.umarells.features.create.CreateActivity;
import com.spikes.umarells.features.detail.DetailActivity;
import com.spikes.umarells.models.BuildingSite;
import com.spikes.umarells.shared.AppCompatActivityExt;
import com.spikes.umarells.shared.Constants;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

public class MapActivity extends AppCompatActivityExt
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, EasyLocationManager.OnLocationChangedListener {

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
    private ActivityEasyLocationManager mEasyLocationManager;
    private Map<String, Marker> mMarkers = new HashMap<>();
    private Marker mUserMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        mEasyLocationManager = new ActivityEasyLocationManager(this);
        mEasyLocationManager.setOnLocationChangedListener(this);
        mEasyLocationManager.requestLocationUpdates();

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
        if (null != mEasyLocationManager) {
            mEasyLocationManager.onDestroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mEasyLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        LatLng currentPosition;
        Location lastKnownLocation = mEasyLocationManager.getLastKnownLocation();
        if (null != lastKnownLocation) {
            currentPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            currentPosition = new LatLng(Constants.DEF_LAT, Constants.DEF_LNG);
        }

        moveUserMarker(currentPosition);

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
    public void onLocationChanged(Location location) {
        mEasyLocationManager.removeLocationUpdates();
        if (null != mMap) {
            moveUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        startActivity(DetailActivity.getStartIntent(MapActivity.this, String.valueOf(marker.getTag())));
    }

    @OnClick(R.id.fab_add_building_site)
    void addNewBuildingSite() {
        if (null != getUser()) {
            startActivity(CreateActivity.getStartIntent(MapActivity.this));
            //addDummyBuildingSite();
        } else {
            startAuthentication();
        }
        //TODO open form activity
        //startActivityForResult(DetailActivity.getStartIntentNew(this), RC_ADD_BUILDING_SITE);
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
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(name)
        );
        newMarker.setTag(id);
        mMarkers.put(id, newMarker);
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
            marker.setTag(id);
        } else {
            addMarker(id, latitude, longitude, name);
        }
    }

    private void addUserMarker(LatLng position) {
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(getString(R.string.user_marker_title))
                .draggable(true)
        );
    }

    private void moveUserMarker(LatLng position) {
        if (null != mUserMarker) {
            mUserMarker.setPosition(position);
        } else {
            addUserMarker(position);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEF_ZOOM));
    }
}
