package com.mps.esteban;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity{

    private static final int REQ_VOICE_SEARCH = 1;
    private static final int ALL_PERMISSIONS_RESULT = 101;
    private static final int REQ_SETTINGS_LOCATION = 102;
    private static final int REQ_PERMISSION_LOCATION = 103;
    private TextView txtSpeechInput;
    private TextView addressValue;
    private ImageButton mexicanBtn;

    private Location location;
    private boolean canGetLocation = true;
    private GoogleApiClient googleApiClient;
    private LocationManager geoLocationService;
    private LocationRequest gmsRequest;
    private String disabledProviders = "";
    private boolean providerIsEnabled = false;
    private boolean permissionGranted = false;
    private LocationListener locationChangedListener = new LocationListener() {
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
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        addressValue = (TextView) findViewById(R.id.addressValue);
        mexicanBtn = (ImageButton) findViewById(R.id.activity_main_mexican_btn);


        /*Hide the Status Bar*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mexicanBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        addressValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String map = "http://maps.google.co.in/maps?q=" + addressValue.getText().toString();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                startActivity(i);
            }
        });

    }

    private void askForLocation() {
        geoLocationService = (LocationManager) getSystemService(LOCATION_SERVICE);
        providerIsEnabled = geoLocationService.isProviderEnabled(LocationManager.GPS_PROVIDER);

        gmsRequest = new LocationRequest();
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        gmsRequest.setPriority(priority);
        gmsRequest.setFastestInterval(10);
        gmsRequest.setInterval(10);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

        checkAndAskPermissions();
    }

    private void locationChanged(Location location) {
        if (!disabledProviders.equals("gps")) {
            getAddress(location.getLatitude(), location.getLongitude());
        }
    }

    private void getAddress(final double lat, final double lon) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    List<Address> addresses;
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(lat, lon, 1);

                    String addressLine = addresses.get(0).getAddressLine(0);
                    return addressLine;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String address) {
                if (address != null && !address.isEmpty()) {
                    addressValue.setText(address);
                } else {
                    addressValue.setText("Address not detected yet!");
                }
            }
        }.execute();
    }

    public void checkAndAskPermissions() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listOfPermissions = new ArrayList<String>();

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (hasCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!listOfPermissions.isEmpty()) {
            permissionGranted = false;
            showPermissionsAlert(listOfPermissions);
        } else {
            permissionGranted = true;

            if(providerIsEnabled) {
                permissionGrantedFlow();
            } else {
                geoLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationChangedListener);
                geoLocationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationChangedListener);
                geoLocationService.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10, 0, locationChangedListener);
                showSettingsAlert();
            }
        }
    }

    private boolean isPermissionGranted() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listOfPermissions = new ArrayList<String>();

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (hasCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listOfPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        return listOfPermissions.isEmpty();
    }

    public void showPermissionsAlert(final List<String> listPermissions) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Location permission is not Enabled!");
        alertDialog.setMessage(getString(R.string.location_permission_question));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(MainActivity.this, listPermissions.toArray(new String[listPermissions.size()]), ALL_PERMISSIONS_RESULT);
                } else {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivityForResult(intent, REQ_SETTINGS_LOCATION);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQ_PERMISSION_LOCATION);
                }
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                permissionGranted = false;
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Location GPS is not Enabled!");
        alertDialog.setMessage(getString(R.string.location_settings_question));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, REQ_SETTINGS_LOCATION);
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                permissionGranted = false;
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    private void searchOnGoogle(String query){

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        startActivity(intent);

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_VOICE_SEARCH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_VOICE_SEARCH: {
                if (resultCode == RESULT_OK && data != null) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                    //TODO result.get(0) = what the user says (e.g. "Open Camera")
                    //TODO Split it and handle each word to do a certain action

                    if (result.get(0).equals("give me my location")) {
                        askForLocation();
                    } else {
                        searchOnGoogle(result.get(0));
                    }
                }
                break;
            }
            case REQ_PERMISSION_LOCATION:
                if (isPermissionGranted()) {
                    permissionGranted = true;
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        showSettingsAlert();
                    }
                }
                break;
            case REQ_SETTINGS_LOCATION:
                if (isPermissionGranted()
                        && geoLocationService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    providerIsEnabled = true;
                    permissionGrantedFlow();
                } else {
                    geoLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationChangedListener);
                    geoLocationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationChangedListener);
                    geoLocationService.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10, 0, locationChangedListener);
                    showSettingsAlert();
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionGranted) {
                    if (providerIsEnabled) {
                        permissionGrantedFlow();
                    } else {
                        showSettingsAlert();
                    }
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void permissionGrantedFlow() {
        LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        geoLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationChangedListener);
        geoLocationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationChangedListener);
        geoLocationService.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10, 0, locationChangedListener);
    }

}
