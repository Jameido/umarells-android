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


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.spikes.easyphotopicker.utils.CameraUtils;
import com.spikes.umarells.R;
import com.spikes.umarells.shared.AppCompatActivityExt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.log.Loggers;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.selector.FlashSelectors;
import io.fotoapparat.parameter.selector.FocusModeSelectors;
import io.fotoapparat.parameter.selector.LensPositionSelectors;
import io.fotoapparat.parameter.selector.SelectorFunction;
import io.fotoapparat.parameter.selector.Selectors;
import io.fotoapparat.parameter.selector.SizeSelectors;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraActivity extends AppCompatActivityExt {

    public static Intent getStartIntent(Context context){
        Intent startIntent = new Intent(context, CameraActivity.class);
        return startIntent;
    }


    public static final int PERMISSION_CAMERA_STORAGE = 345;

    @BindView(R.id.camera_view)
    private CameraView mCameraView;
    private Fotoapparat mFrontFotoapparat;
    private Fotoapparat mBackFotoapparat;
    private FotoapparatSwitcher mFotoapparatSwitcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mFrontFotoapparat = createFotoapparat(LensPositionSelectors.front());
        mBackFotoapparat = createFotoapparat(LensPositionSelectors.back());

        mFotoapparatSwitcher = FotoapparatSwitcher.withDefault(mBackFotoapparat);

        findViewById(R.id.button_change_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });

        findViewById(R.id.button_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        startCameraView();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFotoapparatSwitcher.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_CAMERA_STORAGE) {
            return;
        }

        startCameraView();
    }

    private void startCameraView(){
        Pair<String[], String> missingPermissions = CameraUtils.checkPermissions(CameraActivity.this);
        if (missingPermissions.first.length == 0) {
            mFotoapparatSwitcher.start();
        } else {
            askPermissions(missingPermissions);
        }
    }

    private boolean canSwitchCameras() {
        return mFrontFotoapparat.isAvailable() == mBackFotoapparat.isAvailable();
    }

    private Fotoapparat createFotoapparat(SelectorFunction<LensPosition> position) {
        return Fotoapparat
                .with(CameraActivity.this)
                .into(mCameraView)           // view which will draw the camera preview
                .lensPosition(position)       // we want back/front camera
                .photoSize(SizeSelectors.biggestSize())   // we want to have the biggest photo possible
                .focusMode(Selectors.firstAvailable(  // (optional) use the first focus mode which is supported by device
                        FocusModeSelectors.continuousFocus(),
                        FocusModeSelectors.autoFocus(),        // in case if continuous focus is not available on device, auto focus will be used
                        FocusModeSelectors.fixed()             // if even auto focus is not available - fixed focus mode will be used
                ))
                .flash(Selectors.firstAvailable(      // (optional) similar to how it is done for focus mode, this time for flash
                        FlashSelectors.autoRedEye(),
                        FlashSelectors.autoFlash(),
                        FlashSelectors.torch(),
                        FlashSelectors.off()
                ))
                .logger(Loggers.loggers(            // (optional) we want to log camera events in 2 places at once
                        Loggers.logcat()           // ... in logcat
                ))
                .build();
    }

    private void switchCamera() {
        if(canSwitchCameras()) {
            if (mFotoapparatSwitcher.getCurrentFotoapparat() == mFrontFotoapparat) {
                mFotoapparatSwitcher.switchTo(mBackFotoapparat);
            } else {
                mFotoapparatSwitcher.switchTo(mFrontFotoapparat);
            }
        }
    }

    private void takePicture() {
        PhotoResult photoResult = mFotoapparatSwitcher.getCurrentFotoapparat().takePicture();

        File postcardResult = new File(
                getExternalFilesDir("photos"),
                "postcard.jpg"
        );

        photoResult.saveToFile(postcardResult);
        //TODO decide what to do
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askPermissions(final Pair<String[], String> missingPermissions) {
        if (TextUtils.isEmpty(missingPermissions.second)) {
            showPermissionsSnackBar(missingPermissions);
        } else {
            requestPermissions(missingPermissions.first, PERMISSION_CAMERA_STORAGE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showPermissionsSnackBar(final Pair<String[], String> missingPermissions) {
        Snackbar mSnackbar = Snackbar.make(ButterKnife.findById(CameraActivity.this, R.id.coordinator), missingPermissions.second, Snackbar.LENGTH_INDEFINITE);
        TextView tv = (TextView) mSnackbar.getView().findViewById(com.spikes.easyphotopicker.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        mSnackbar.setAction(android.R.string.ok,
                view -> {
                    requestPermissions(missingPermissions.first, PERMISSION_CAMERA_STORAGE);
                });
        mSnackbar.setActionTextColor(ContextCompat.getColor(CameraActivity.this, android.R.color.white));
        mSnackbar.show();
    }
}
