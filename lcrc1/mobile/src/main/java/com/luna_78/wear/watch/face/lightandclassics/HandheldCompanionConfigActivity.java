package com.luna_78.wear.watch.face.lightandclassics;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;

public class HandheldCompanionConfigActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, APhoneService.class));

        setContentView(R.layout.empty);
    }


    //GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {

    }


    //GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {

    }


    //GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    //DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        dataEvents.release();
    }
}
