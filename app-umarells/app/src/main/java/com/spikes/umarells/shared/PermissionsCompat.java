package com.spikes.umarells.shared;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by Luca Rossi
 * (luca.rossi@alea.pro) on 02/05/2017.
 */

public class PermissionsCompat {

    public static boolean isPermissionGranted(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
