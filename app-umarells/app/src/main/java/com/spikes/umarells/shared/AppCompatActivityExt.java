package com.spikes.umarells.shared;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import butterknife.ButterKnife;

/**
 * Created by Luca Rossi
 * (luca.rossi@alea.pro) on 21/06/2017.
 */

public abstract class AppCompatActivityExt extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    protected boolean mAuthCanceled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            setUser(user);
            if (null != user) {
                onAuthenticationSuccessful(user);
            } else {
                onAuthenticationFailed();
            }
        };
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setPostContentView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setPostContentView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setPostContentView();
    }

    private void setPostContentView() {
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachAuthListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachAuthListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RC_SIGN_IN:
                //We take action only if it's canceled,
                //the AuthListener will do the rest after onResume
                switch (resultCode) {
                    case RESULT_OK:
                        break;
                    case RESULT_CANCELED:
                        mAuthCanceled = true;
                        break;
                    default:
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    protected FirebaseUser getUser() {
        return mUser;
    }

    protected void attachAuthListener() {
        getFirebaseAuth().addAuthStateListener(getAuthStateListener());
    }

    protected void detachAuthListener() {
        if (null != mAuthStateListener) {
            getFirebaseAuth().removeAuthStateListener(getAuthStateListener());
        }
    }

    protected FirebaseAuth.AuthStateListener getAuthStateListener() {
        return mAuthStateListener;
    }

    protected void startAuthentication() {
        mAuthCanceled = false;
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                )
                        )
                        .build(),
                Constants.RC_SIGN_IN);
    }

    protected void signOut() {
        AuthUI.getInstance().signOut(this);
    }

    @CallSuper
    protected void onAuthenticationSuccessful(FirebaseUser user){
        setUser(user);
    }

    protected void onAuthenticationFailed(){
        if(isLoginRequired()) {
            if (!mAuthCanceled) {
                startAuthentication();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    this.finishAffinity();
                }
                System.exit(0);
            }
        }
    }

    protected boolean isLoginRequired(){
        return false;
    }

    private void setUser(FirebaseUser user) {
        mUser = user;
    }

}
