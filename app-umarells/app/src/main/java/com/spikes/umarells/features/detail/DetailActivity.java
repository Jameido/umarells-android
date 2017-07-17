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
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
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
import com.spikes.umarells.features.reviews.ReviewsActivity;
import com.spikes.umarells.features.reviews.TopReviewsAdapter;
import com.spikes.umarells.features.gallery.GalleryActivity;
import com.spikes.umarells.models.BuildingSite;
import com.spikes.umarells.shared.AppCompatActivityExt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivityExt
        implements OnMapReadyCallback {

    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String TAG = DetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String buildingSiteId) {
        Intent startIntent = new Intent(context, DetailActivity.class);
        startIntent.putExtra(EXTRA_ID, buildingSiteId);
        return startIntent;
    }


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_name)
    AppCompatTextView mTextName;
    @BindView(R.id.text_description)
    AppCompatTextView mTextDescription;
    @BindView(R.id.text_start)
    AppCompatTextView mTextStart;
    @BindView(R.id.text_end)
    AppCompatTextView mTextEnd;
    @BindView(R.id.text_address)
    AppCompatTextView mTextAddress;
    @BindView(R.id.image_detail)
    AppCompatImageView mImageDetail;
    @BindView(R.id.recycler_reviews)
    RecyclerView mRecyclerReviews;

    private GoogleMap mMap;
    private TopReviewsAdapter mReviewsAdapter;
    private String mBuildingSiteId;
    private BuildingSite mBuildingSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

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

    @OnClick(R.id.text_see_reviews)
    void seeAllReviews() {
        startActivity(ReviewsActivity.getStartIntent(this, mBuildingSiteId));
    }

    @OnClick(R.id.image_detail)
    void openGallery() {
        startActivity(GalleryActivity.getStartIntent(this, mBuildingSiteId, mBuildingSite.getName()));
    }

    private void initDataSource(String buildingSiteId) {
        mBuildingSiteId = buildingSiteId;

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

        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_photos")
                .child(buildingSiteId)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String url = "";
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            url = childSnapshot.getValue(String.class);
                        }
                        Glide.with(DetailActivity.this)
                                .load(url)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .centerCrop()
                                .thumbnail(0.4f)
                                .dontAnimate()
                                .into(mImageDetail);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        Query reviewsQuery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_reviews")
                .child(buildingSiteId)
                .limitToFirst(3);

        mReviewsAdapter = new TopReviewsAdapter(reviewsQuery);
        mRecyclerReviews.setAdapter(mReviewsAdapter);
    }

    private void fillBuildingSiteData() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        mTextName.setText(mBuildingSite.getName());
        mTextDescription.setText(mBuildingSite.getDescription());
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
}
