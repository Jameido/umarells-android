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

package com.spikes.umarells.features.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spikes.umarells.R;
import com.spikes.umarells.models.BuildingSite;
import com.spikes.umarells.shared.AppCompatActivityExt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BuildingSiteDetailActivity extends AppCompatActivityExt
        implements OnMapReadyCallback {

    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String TAG = BuildingSiteDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String buildingSiteId) {
        Intent startIntent = new Intent(context, BuildingSiteDetailActivity.class);
        startIntent.putExtra(EXTRA_ID, buildingSiteId);
        return startIntent;
    }

    @BindView(R.id.text_building_site_name)
    AppCompatTextView mTextName;
    @BindView(R.id.text_building_site_start)
    AppCompatTextView mTextStart;
    @BindView(R.id.text_building_site_end)
    AppCompatTextView mTextEnd;
    @BindView(R.id.text_building_site_address)
    AppCompatTextView mTextAddress;
    @BindView(R.id.text_building_site_description)
    AppCompatTextView mTextDescription;
    @BindView(R.id.recycler_gallery)
    RecyclerView mRecyclerGallery;

    private GoogleMap mMap;
    private GalleryAdapter mGalleryAdapter;
    private BuildingSite mBuildingSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_site_detail);

        Bundle extras = getIntent().getExtras();
        if (null != extras && extras.containsKey(EXTRA_ID)) {

            initDataSource(extras.getString(EXTRA_ID, ""));

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_building_site);
            mapFragment.getMapAsync(this);
        } else {
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addBuildingSiteMarker();
    }

    @OnClick(R.id.fab_add_picture)
    void openCameraActivity(){
        startActivity(CameraActivity.getStartIntent(this));
    }

    private void initDataSource(String buildingSiteId) {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_sites")
                .child(buildingSiteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mBuildingSite = dataSnapshot.getValue(BuildingSite.class);
                        addBuildingSiteMarker();
                        fillBuildingSiteData();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        Query imagesQuery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_images")
                .child(buildingSiteId);

        mGalleryAdapter = new GalleryAdapter(imagesQuery);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerGallery.setLayoutManager(mLayoutManager);
        mRecyclerGallery.setAdapter(mGalleryAdapter);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerGallery);
    }

    private void fillBuildingSiteData() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        mTextName.setText(mBuildingSite.getName());
        mTextStart.setText(dateFormat.format(new Date(mBuildingSite.getStart())));
        mTextEnd.setText(dateFormat.format(mBuildingSite.getEnd()));
        mTextAddress.setText(mBuildingSite.getAddress());
        mTextDescription.setText(mBuildingSite.getDescription());
    }


    private void addBuildingSiteMarker() {
        if (null != mMap && null != mBuildingSite) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(mBuildingSite.getLat(), mBuildingSite.getLng())));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mBuildingSite.getLat(), mBuildingSite.getLng() - 0.015)));
        }
    }
}
