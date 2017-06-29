package com.spikes.umarells.features.add;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spikes.umarells.R;
import com.spikes.umarells.shared.AppCompatActivityExt;
import com.spikes.umarells.shared.PositionManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddBuildingSiteActivity extends AppCompatActivityExt
        implements OnMapReadyCallback, PositionManager.OnPositionListener {


    private static final String TAG = AddBuildingSiteActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context) {
        Intent startIntent = new Intent(context, AddBuildingSiteActivity.class);
        return startIntent;
    }

    private GoogleMap mMap;
    private Marker mUserMarker;
    private DatabaseReference mBuildingSitesDatabase;
    private PositionManager mPositionManager;

    @BindView(R.id.text_building_site_lat)
    AppCompatTextView mTextLat;
    @BindView(R.id.text_building_site_lng)
    AppCompatTextView mTextLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building_site);

        mPositionManager = new PositionManager(this);
        mPositionManager.setOnPositionListener(this);
        mPositionManager.setCoordinatorLayout(ButterKnife.findById(this, R.id.coordinator));
        mPositionManager.requestPositionUpdates();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected boolean isLoginRequired() {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPositionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        Location lastKnownLocation = mPositionManager.getLastKnownLocation();
        if (null != lastKnownLocation) {
            moveUserMarker(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
        }

        initDataSource();
    }

    @Override
    public void onPositionChanged(Location position) {
        mPositionManager.removePositionUpdates();
        if (null != mMap) {
            moveUserMarker(new LatLng(position.getLatitude(), position.getLongitude()));
        }
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
    }

    private void onMarkerPositionChanged(LatLng position){
        mTextLat.setText(String.valueOf(position.latitude));
        mTextLng.setText(String.valueOf(position.longitude));

    }
}
