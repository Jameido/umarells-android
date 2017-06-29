package com.spikes.umarells.shared;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.spikes.umarells.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcello Della Mea
 * (marcello.dellamea@alea.pro) on 05/10/2016.
 */

public class PositionManager implements LocationListener {

    private static final String TAG = PositionManager.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int MIN_TIME = 1000 * 15;
    private static final int MIN_DISTANCE = 50;

    public static final int PERMISSION_LOCATION = 100;
    protected int mPermissionCode = PERMISSION_LOCATION;

    private OnPositionListener mOnPositionListener;
    private Location mLastKnownLocation = null;
    private LocationManager mLocationManager;
    private Activity mActivity;
    private long mMinTime = MIN_TIME;
    private float mMinDistance = MIN_DISTANCE;
    private CoordinatorLayout mCoordinatorLayout;
    private OnPermissionResult mOnPermissionResult = new OnPermissionResult() {
        @Override
        public void onPermissionsGranted() {
            requestPositionUpdates();
        }

        @Override
        public void onPermissionsDenied(String[] permissions) {
            checkPermissions();
        }
    };

    public PositionManager(Activity activity) {
        mActivity = activity;
        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, getLastKnownLocation())) {
            mLastKnownLocation = location;
            if (mOnPositionListener != null) {
                mOnPositionListener.onPositionChanged(mLastKnownLocation);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void setOnPositionListener(OnPositionListener listener) {
        mOnPositionListener = listener;
    }

    public void setMinTime(long minTime) {
        mMinTime = minTime;
    }

    public void setMinDistance(float minDistance) {
        mMinDistance = minDistance;
    }

    @SuppressWarnings({"MissingPermission"})
    public void requestPositionUpdates() {
        if (checkPermissions() && mLocationManager != null) {
            if (mLocationManager.getProviders(true).contains(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mMinTime, mMinDistance, this);
            else if (mLocationManager.getProviders(true).contains(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, this);
        }
    }

    public void removePositionUpdates() {
        if (mLocationManager != null)
            mLocationManager.removeUpdates(this);
    }

    @SuppressWarnings({"MissingPermission"})
    public Location getLastKnownLocation() {
        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || PermissionsCompat.isPermissionGranted(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) &&
                mLastKnownLocation == null &&
                mLocationManager != null) {
            if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (mLastKnownLocation == null && mLocationManager != null && mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }

        return mLastKnownLocation;
    }

    public void setCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        mCoordinatorLayout = coordinatorLayout;
    }

    public void setOnPermissionResult(OnPermissionResult onPermissionResult) {
        mOnPermissionResult = onPermissionResult;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != mPermissionCode) {
            return;
        }

        if (mOnPermissionResult == null) {
            return;
        }

        List<String> permissionsDenied = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsDenied.add(permissions[i]);
            }
        }

        if (permissionsDenied.size() > 0) {
            checkPermissions();
            mOnPermissionResult.onPermissionsDenied(permissionsDenied.toArray(new String[permissionsDenied.size()]));
        } else {
            mOnPermissionResult.onPermissionsGranted();
        }
    }

    private boolean checkPermissions() {
        final List<String> permissionsList = new ArrayList<>();
        boolean rationalePermissions = false;
        String permissionsMessage = "";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!PermissionsCompat.isPermissionGranted(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionsMessage += mActivity.getString(R.string.position_permissions_rationale);
                rationalePermissions = mActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (permissionsList.size() > 0) {
                if (rationalePermissions) {
                    showSnackBar(
                            permissionsMessage,
                            Snackbar.LENGTH_INDEFINITE,
                            mActivity.getString(android.R.string.ok),
                            view -> requestPermissions(permissionsList.toArray(new String[permissionsList.size()]))
                    );
                } else {
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]));
                }
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestPermissions(String[] permissions) {
        mActivity.requestPermissions(permissions, mPermissionCode);
    }

    private void showSnackBar(String message, int duration, String action, View.OnClickListener actionListener) {
        if (mCoordinatorLayout == null) {
            Log.e(TAG, "Unable to open snackbar: Coordinator layout not set");
            return;
        }
        Snackbar mSnackbar = Snackbar.make(mCoordinatorLayout, message, duration);
        TextView tv = (TextView) mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (action != null && actionListener != null) {
            mSnackbar.setAction(action, actionListener);
            mSnackbar.setActionTextColor(ContextCompat.getColor(mActivity, android.R.color.white));
        }
        mSnackbar.show();
    }

    public interface OnPositionListener {
        void onPositionChanged(Location location);
    }


    public interface OnPermissionResult {
        void onPermissionsGranted();

        void onPermissionsDenied(String[] permissions);
    }
}
