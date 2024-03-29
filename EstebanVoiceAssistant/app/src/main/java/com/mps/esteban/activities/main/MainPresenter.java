package com.mps.esteban.activities.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mps.esteban.application.MyApplication;
import com.mps.esteban.forms.FacebookDetails;
import com.mps.esteban.forms.ResponseWeather;
import com.mps.esteban.mvp.BasePresenter;
import com.mps.esteban.retrofit.ApiCall;
import com.mps.esteban.utils.IntentManager;
import com.mps.esteban.utils.PrefUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by cosmin on 30.11.2017.
 */

public class MainPresenter extends BasePresenter<Contract.ContractView> implements Contract.ContractPresenter, LocationListener {

    @Inject Handler handler;
    @Inject ApiCall apiCall;

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
        checkAndAskPermissionsForLocation();
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
            case IntentManager.REQ_PERMISSION_LOCATION:
                permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionGranted) {
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        getView().showSettingsAlert();
                    }
                }
                break;
            case IntentManager.REQ_PERMISSION_CONTACTS:
                permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionGranted) {
                    getView().callCommand();
                } else {
                    askForCallIntent();
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

                    getView().processCommand(result.get(0));
                }
                break;
            }
            case IntentManager.REQ_PERMISSION_LOCATION:
                if (isPermissionGranted(IntentManager.REQ_PERMISSION_LOCATION)) {
                    permissionGranted = true;
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        getView().showSettingsAlert();
                    }
                }
                break;
            case IntentManager.REQ_PERMISSION_CONTACTS:
                if (isPermissionGranted(IntentManager.REQ_PERMISSION_CONTACTS)) {
                    getView().callCommand();
                } else {
                    askForCallIntent();
                }
                break;
            case IntentManager.REQ_SETTINGS_LOCATION:
                if (isPermissionGranted(IntentManager.REQ_PERMISSION_LOCATION)
                        && geoLocationService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    providerIsEnabled = true;
                    permissionGrantedFlow();
                } else {
                    getView().showSettingsAlert();
                }
                break;
            case IntentManager.RESULT_ACTION_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    getNumber(getView().getContext(), data.getData());
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

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

    @Override
    public void askForCallIntent() {
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.READ_CONTACTS);
        int hasCallPhonePermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.CALL_PHONE);

        List<String> listOfPermissions = new ArrayList<String>();

        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.READ_CONTACTS);
        }

        if (hasCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.CALL_PHONE);
        }

        if (!listOfPermissions.isEmpty()) {
            getView().showPermissionsAlert(IntentManager.REQ_PERMISSION_CONTACTS, listOfPermissions);
        } else {
            getView().callCommand();
        }

//        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, resultValue);
    }

    @Override
    public void askForTime(TextView resultData) {
        Date currentTime = Calendar.getInstance().getTime();
        String theTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(currentTime);
        resultData.setText(theTime);
    }

    @Override
    public BroadcastReceiver askForBattery(Context context, final TextView resultData) {
        BroadcastReceiver batteryInfo = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                resultData.setText(String.valueOf(level) + "%");
            }
        };

        return batteryInfo;
    }

    @Override
    public void askForIpAddress(boolean IPv4, TextView resultData) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (IPv4) {
                            if (isIPv4) {
                                resultData.setText(sAddr);
                                return;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                resultData.setText(delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase());
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        resultData.setText("No ip address");
        return;
    }

    @Override
    public void getFacebookDetails() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        final Bundle parameters1 = new Bundle();
        parameters1.putString("fields", "id,name,email,friends");
//        final Bundle parameters2 = new Bundle();
//        parameters2.putString("limit", "500");
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* make the API call */
                new GraphRequest(
                        accessToken,
                        "/me",
                        parameters1,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* handle the result */
                                if (response != null && response.getError() == null) {
                                    JSONObject responseJson = response.getJSONObject();
                                    final FacebookDetails facebookDetails = new FacebookDetails(responseJson);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setFacebookDetails(facebookDetails);
                                        }
                                    });
                                }
                            }
                        }
                ).executeAsync();

//                new GraphRequest(
//                        AccessToken.getCurrentAccessToken(),
//                        "/me/taggable_friends",
//                        parameters2,
//                        HttpMethod.GET,
//                        new GraphRequest.Callback() {
//                            public void onCompleted(GraphResponse response) {
//                                /* handle the result */
//                                if (response != null) {
//
//                                }
//                            }
//                        }
//                ).executeAsync();
            }
        }).start();
    }

    @Override
    public void getWeatherByCity(final String cityName, final String appid, final String metric) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                apiCall.getApiService()
                        .getWeatherByCity(cityName, appid, metric)
                        .enqueue(new Callback<ResponseWeather>() {
                            @Override
                            public void onResponse(Call<ResponseWeather> call, final Response<ResponseWeather> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setWeatherValues(response.body());
                                        }
                                    });
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setWeatherValues(null);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseWeather> call, Throwable t) {
                                Log.d("weather: ", "api failed");
                            }
                        });
            }
        }).start();
    }

    @Override
    public void getWeatherByLatLon(final double lat, final double lon, final String appid, final String metric) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiCall.getApiService()
                        .getWeatherByCurrentLocation(lat, lon, appid, metric)
                        .enqueue(new Callback<ResponseWeather>() {
                            @Override
                            public void onResponse(Call<ResponseWeather> call, final Response<ResponseWeather> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setWeatherValues(response.body());
                                        }
                                    });
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setWeatherValues(null);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseWeather> call, Throwable t) {
                                Log.d("weather: ", "api failed");
                            }
                        });
            }
        }).start();
    }

    private void locationChanged(Location location) {
        if (!disabledProviders.equals("gps") && location != null) {
            getAddress(getView().getContext(), location.getLatitude(), location.getLongitude());
        }
    }

    private void checkAndAskPermissionsForLocation() {
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
            getView().showPermissionsAlert(IntentManager.REQ_PERMISSION_LOCATION, listOfPermissions);
        } else {
            permissionGranted = true;

            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            locationChanged(geoLocationService.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            if (providerIsEnabled) {
                permissionGrantedFlow();
            } else {
                getView().showSettingsAlert();
            }
        }
    }

    private boolean isPermissionGranted(int requestType) {

        List<String> listOfPermissions = new ArrayList<>();

        switch (requestType) {
            case IntentManager.REQ_PERMISSION_LOCATION:
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocation = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }

                if (hasCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                    listOfPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                break;
            case IntentManager.REQ_PERMISSION_CONTACTS:
                int hasReadContactsPermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.READ_CONTACTS);
                int hasCallPhonePermission = ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.CALL_PHONE);

                if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    listOfPermissions.add(Manifest.permission.READ_CONTACTS);
                }

                if (hasCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    listOfPermissions.add(Manifest.permission.CALL_PHONE);
                }
                break;
        }

        return listOfPermissions.isEmpty();
    }

    private void getNumber(final Context mContext, final Uri contactUri) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Cursor s = mContext.getContentResolver().query(contactUri, null,
                        null, null, null);

                if (s != null && s.moveToFirst()) {

                    String id = s.getString(s.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String hasPhone = s.getString(s.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = mContext.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null);
                        if (phones != null && phones.moveToFirst()) {
                            final String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            phones.close();
                            s.close();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getView().openCallIntent(cNumber);
                                }
                            });
                        }
                    }
                }
            }
        }).start();
    }

    private void getAddress(final Context mContext, final double lat, final double lon) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final List<Address> addresses;
                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    addresses = geocoder.getFromLocation(lat, lon, 1);

                    final String addressLine = addresses.get(0).getAddressLine(0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getView().setAddress(addressLine, lat, lon, addresses.get(0).getLocality());
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

}
