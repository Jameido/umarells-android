package com.spikes.umarells.features.detail;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spikes.easylocationmanager.ActivityEasyLocationManager;
import com.spikes.easylocationmanager.EasyLocationManager;
import com.spikes.umarells.R;
import com.spikes.umarells.shared.AppCompatActivityExt;
import com.spikes.umarells.shared.Constants;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BuildingSiteDetailActivity extends AppCompatActivityExt
        implements OnMapReadyCallback, EasyLocationManager.OnLocationChangedListener {

    /**
     * TODO Add missing features:
     *  - Taking a picture
     *  - Inserting description
     *  - Rating
     *  - Start date end date with datepicker
     *  - Request delete
     */

    private static final String TAG = BuildingSiteDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context) {
        Intent startIntent = new Intent(context, BuildingSiteDetailActivity.class);
        return startIntent;
    }

    private GoogleMap mMap;
    private Marker mUserMarker;
    private DatabaseReference mBuildingSitesDatabase;
    private ActivityEasyLocationManager mEasyLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_site_detail);

        mEasyLocationManager = new ActivityEasyLocationManager(this);
        mEasyLocationManager.setOnLocationChangedListener(this);
        mEasyLocationManager.setCoordinatorLayout(ButterKnife.findById(this, R.id.coordinator));
        mEasyLocationManager.requestPositionUpdates();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEasyLocationManager.onDestroy();
    }

    @Override
    protected boolean isLoginRequired() {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mEasyLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                onMarkerPositionChanged(marker.getPosition());
            }
        });

        LatLng currentPosition;
        Location lastKnownLocation = mEasyLocationManager.getLastKnownLocation();
        if (null != lastKnownLocation) {
            currentPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }else {
            currentPosition = new LatLng(Constants.DEF_LAT, Constants.DEF_LNG);
        }

        moveUserMarker(currentPosition);

        initDataSource();
    }

    @Override
    public void onLocationChanged(Location location) {
        mEasyLocationManager.removePositionUpdates();
        if (null != mMap) {
            moveUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @OnClick(R.id.button_save_building_site)
    void saveBuildingSite(){

    }

    private void initDataSource() {
        mBuildingSitesDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_sites");
    }

    private void addUserMarker(LatLng position) {
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getString(R.string.user_marker_title))
                .draggable(true)
        );
        onMarkerPositionChanged(position);
    }

    private void moveUserMarker(LatLng position) {
        if (null != mUserMarker) {
            mUserMarker.setPosition(position);
            onMarkerPositionChanged(position);
        } else {
            addUserMarker(position);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEF_ZOOM));
    }

    private void onMarkerPositionChanged(LatLng position){

    }
}
