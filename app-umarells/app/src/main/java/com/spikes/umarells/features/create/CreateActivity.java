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

package com.spikes.umarells.features.create;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatTextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spikes.easylocationmanager.ActivityEasyLocationManager;
import com.spikes.easylocationmanager.EasyLocationManager;
import com.spikes.jodatimeutils.JodaDatePickerDialog;
import com.spikes.jodatimeutils.JodaDateRangePickerDialog;
import com.spikes.umarells.R;
import com.spikes.umarells.shared.AppCompatActivityExt;
import com.spikes.umarells.shared.Constants;

import butterknife.BindView;
import butterknife.OnClick;

public class CreateActivity extends AppCompatActivityExt
        implements OnMapReadyCallback, EasyLocationManager.OnLocationChangedListener {

    public static Intent getStartIntent(Context context) {
        Intent startIntent = new Intent(context, CreateActivity.class);
        return startIntent;
    }

    @BindView(R.id.edit_name)
    TextInputEditText mEditName;
    @BindView(R.id.edit_description)
    TextInputEditText mEditDescription;
    @BindView(R.id.text_dates)
    AppCompatTextView mTextDates;

    private GoogleMap mMap;
    private ActivityEasyLocationManager mEasyLocationManager;
    private Marker mUserMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
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
        LatLng currentPosition;
        Location lastKnownLocation = mEasyLocationManager.getLastKnownLocation();
        if (null != lastKnownLocation) {
            currentPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            currentPosition = new LatLng(Constants.DEF_LAT, Constants.DEF_LNG);
        }

        moveUserMarker(currentPosition);
    }

    @Override
    public void onLocationChanged(Location location) {
        mEasyLocationManager.removeLocationUpdates();
        if (null != mMap) {
            moveUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }


    @Override
    protected boolean isLoginRequired() {
        return true;
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

    @OnClick(R.id.text_dates)
    void openDatePicker() {
        new JodaDateRangePickerDialog(CreateActivity.this, (startDate, endDate) -> {
            mTextDates.setText(startDate.toDate().toString());
        }).show();
    }
}
