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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spikes.easyphotopicker.ActivityEasyCameraPicker;
import com.spikes.umarells.R;
import com.spikes.umarells.features.comments.CommentsActivity;
import com.spikes.umarells.features.comments.TopCommentsAdapter;
import com.spikes.umarells.models.BuildingSite;
import com.spikes.umarells.shared.AppCompatActivityExt;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BuildingSiteDetailActivity extends AppCompatActivityExt
        implements OnMapReadyCallback,
        ActivityEasyCameraPicker.OnResult {

    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String TAG = BuildingSiteDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String buildingSiteId) {
        Intent startIntent = new Intent(context, BuildingSiteDetailActivity.class);
        startIntent.putExtra(EXTRA_ID, buildingSiteId);
        return startIntent;
    }

    @BindView(R.id.text_building_site_start)
    AppCompatTextView mTextStart;
    @BindView(R.id.text_building_site_end)
    AppCompatTextView mTextEnd;
    @BindView(R.id.text_building_site_address)
    AppCompatTextView mTextAddress;
    @BindView(R.id.recycler_gallery)
    RecyclerView mRecyclerGallery;
    @BindView(R.id.recycler_comments)
    RecyclerView mRecyclerComments;

    private GoogleMap mMap;
    private GalleryAdapter mGalleryAdapter;
    private TopCommentsAdapter mCommentsAdapter;
    private String mBuildingSiteId;
    private BuildingSite mBuildingSite;
    private ActivityEasyCameraPicker mEasyCameraPicker;
    private StorageReference mStorage;
    private DatabaseReference mPhotosReference;

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

            final LinearLayoutManager galleryLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
            galleryLayoutManager.setStackFromEnd(true);
            mRecyclerGallery.setLayoutManager(galleryLayoutManager);
            mRecyclerGallery.setHasFixedSize(true);
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(mRecyclerGallery);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mEasyCameraPicker) {
            mEasyCameraPicker.onDestroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getEasyCameraPicker().onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getEasyCameraPicker().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addBuildingSiteMarker();
    }

    @OnClick(R.id.fab_add_picture)
    void openCamera() {
        if(null != getUser()) {
            getEasyCameraPicker().openPicker(String.format(getString(R.string.photo_file_name), mBuildingSite.getName(), mGalleryAdapter.getItemCount()));
        }else {
            startAuthentication();
        }
    }

    @OnClick(R.id.text_see_comments)
    void seeAllComments(){
        startActivity(CommentsActivity.getStartIntent(this, mBuildingSiteId));
    }

    private void initDataSource(String buildingSiteId) {
        mBuildingSiteId = buildingSiteId;

        mStorage = FirebaseStorage.getInstance()
                .getReference()
                .child("building_sites_photos")
                .child(buildingSiteId);

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

        mPhotosReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_photos")
                .child(buildingSiteId);

        mGalleryAdapter = new GalleryAdapter(mPhotosReference);
        mRecyclerGallery.setAdapter(mGalleryAdapter);

        Query commentsQuery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_comments")
                .child(buildingSiteId);

        mCommentsAdapter = new TopCommentsAdapter(commentsQuery);
        mRecyclerComments.setAdapter(mCommentsAdapter);
    }

    private void fillBuildingSiteData() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        getSupportActionBar().setTitle(mBuildingSite.getName());
        mTextStart.setText(dateFormat.format(new Date(mBuildingSite.getStart())));
        mTextEnd.setText(dateFormat.format(mBuildingSite.getEnd()));
        mTextAddress.setText(mBuildingSite.getAddress());
    }


    private void addBuildingSiteMarker() {
        if (null != mMap && null != mBuildingSite) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(mBuildingSite.getLat(), mBuildingSite.getLng())));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mBuildingSite.getLat(), mBuildingSite.getLng() - 0.015)));
        }
    }

    private ActivityEasyCameraPicker getEasyCameraPicker() {
        if (mEasyCameraPicker == null) {
            mEasyCameraPicker = new ActivityEasyCameraPicker(
                    this,
                    getString(R.string.file_provider),
                    true
            );
            mEasyCameraPicker.setOnResult(this);
            mEasyCameraPicker.setCoordinatorLayout(ButterKnife.findById(BuildingSiteDetailActivity.this, R.id.coordinator));
        }
        return mEasyCameraPicker;
    }

    @Override
    public void onSuccess(File file) {
        StorageReference photoRef = mStorage.child(mBuildingSiteId).child(file.getName());
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(file));
        uploadTask.addOnSuccessListener(task -> {
            uploadPhotoUrl(task.getDownloadUrl().toString());
        });
        uploadTask.addOnFailureListener(e -> {
            //TODO manage error
        });
    }

    //TODO: move code to firebase cloud functions
    private void uploadPhotoUrl(String photoUrl) {
        String imageKey = mPhotosReference.push().getKey();

        Map<String, Object> photoUpdate = new HashMap<>();
        photoUpdate.put(imageKey, photoUrl);

        mPhotosReference.updateChildren(
                photoUpdate,
                (databaseError, databaseReference) -> {
                    if (null != databaseError) {
                        Log.e("Error", "Updating data", databaseError.toException());
                    } else {
                        //TODO
                    }
                }
        );
    }
}
