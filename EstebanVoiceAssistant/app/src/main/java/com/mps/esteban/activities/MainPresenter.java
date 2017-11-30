package com.mps.esteban.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mps.esteban.application.MyApplication;
import com.mps.esteban.mvp.BasePresenter;
import com.mps.esteban.utils.IntentManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Created by cosmin on 30.11.2017.
 */

public class MainPresenter extends BasePresenter<Contract.ContractView> implements Contract.ContractPresenter, LocationListener  {

    @Inject IntentManager intentManager;
    @Inject Handler handler;

    private LocationRequest gmsRequest;
    private String disabledProviders = "";
    private GoogleApiClient googleApiClient;
    private LocationManager geoLocationService;
    private boolean providerIsEnabled = false;
    private boolean permissionGranted = false;

    public MainPresenter(Contract.ContractView view) {
        super(view);
    }

    @Override
    public void injectDependencies() {
        MyApplication.getAppComponent().inject(this);
    }

    @Override
    public void askForLocation() {
        geoLocationService = (LocationManager) getView().getContext().getSystemService(Context.LOCATION_SERVICE);
        providerIsEnabled = geoLocationService.isProviderEnabled(LocationManager.GPS_PROVIDER);

        gmsRequest = new LocationRequest();
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        gmsRequest.setPriority(priority);
        gmsRequest.setFastestInterval(10);
        gmsRequest.setInterval(10);

        googleApiClient = new GoogleApiClient.Builder(getView().getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, gmsRequest, new com.google.android.gms.location.LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    locationChanged(location);
                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();

        googleApiClient.connect();
        getView().changeAddressVisibility(View.VISIBLE);
        checkAndAskPermissions();
    }

    @Override
    public void disconnectGoogleApiClient() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    private void permissionGrantedFlow() {
        locationChanged(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
        geoLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
        geoLocationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, this);
        geoLocationService.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10, 0, this);
    }

    @Override
    public void processRequestedPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case IntentManager.ALL_PERMISSIONS_RESULT:
                permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionGranted) {
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        getView().showSettingsAlert();
                    }
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void processActivityResults(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IntentManager.REQ_VOICE_SEARCH: {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    getView().setSpeechInput(result.get(0));
                    getView().changeAddressVisibility(View.GONE);

                    //TODO result.get(0) = what the user says (e.g. "Open Camera")
                    //TODO Split it and handle each word to do a certain action

                    if (result.get(0).equals("give me my location")) {
                        askForLocation();
                    } else {
                        intentManager.searchOnGoogle(getView().getContext(), result.get(0));
                    }
                }
                break;
            }
            case IntentManager.REQ_PERMISSION_LOCATION:
                if (isPermissionGranted()) {
                    permissionGranted = true;
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        getView().showSettingsAlert();
                    }
                }
                break;
            case IntentManager.REQ_SETTINGS_LOCATION:
                if (isPermissionGranted()
                        && geoLocationService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    providerIsEnabled = true;
                    permissionGrantedFlow();
                } else {
                    getView().showSettingsAlert();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) {
        if (s.equals("gps")) {
            disabledProviders = "";
            providerIsEnabled = true;
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        disabledProviders = disabledProviders.isEmpty() ? s : disabledProviders + " " + s;
        if (disabledProviders.equals("network gps")
                || disabledProviders.equals("gps network")
                || disabledProviders.equals("gps")) {
            providerIsEnabled = false;
        }
    }

    private void locationChanged(Location location) {
        if (!disabledProviders.equals("gps") && location != null) {
            getAddress(location.getLatitude(), location.getLongitude());
        }
    }

    public void checkAndAskPermissions() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listOfPermissions = new ArrayList<String>();

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (hasCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!listOfPermissions.isEmpty()) {
            permissionGranted = false;
            getView().showPermissionsAlert(listOfPermissions);
        } else {
            permissionGranted = true;

            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            if(providerIsEnabled) {
                permissionGrantedFlow();
            } else {
                getView().showSettingsAlert();
            }
        }
    }

    private boolean isPermissionGranted() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listOfPermissions = new ArrayList<>();

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (hasCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        return listOfPermissions.isEmpty();
    }

    private void getAddress(final double lat, final double lon) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    List<Address> addresses;
                    Geocoder geocoder = new Geocoder(getView().getContext(), Locale.getDefault());
                    addresses = geocoder.getFromLocation(lat, lon, 1);

                    final String addressLine = addresses.get(0).getAddressLine(0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getView().setAddress(addressLine);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
