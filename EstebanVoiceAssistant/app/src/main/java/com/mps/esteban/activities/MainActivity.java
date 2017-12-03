package com.mps.esteban.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mps.esteban.R;
import com.mps.esteban.application.MyApplication;
import com.mps.esteban.mvp.BaseActivity;
import com.mps.esteban.utils.IntentManager;
import com.mps.esteban.utils.PrefUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<Contract.ContractPresenter> implements Contract.ContractView{

    @BindView(R.id.txtSpeechInput) TextView txtSpeechInput;
    @BindView(R.id.addressValue) TextView addressValue;
    @BindView(R.id.activity_main_mexican_btn) ImageButton mexicanBtn;

    @Inject IntentManager intentManager;


    @Override
    public void setupToolbar() { }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public Contract.ContractPresenter bindPresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void injectDependencies() {
        MyApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @OnClick(value = {R.id.activity_main_mexican_btn, R.id.addressValue})
    public void onSelectEntry(View view) {
        switch (view.getId()) {
            case R.id.activity_main_mexican_btn:
                intentManager.promptSpeechInput(this);
                break;
            case R.id.addressValue:
                intentManager.openMapWithCurrentLocation(this, addressValue.getText().toString());
                break;
            default:
                break;
        }
    }

    @Override
    public void showPermissionsAlert(final int permissionsType, final List<String> listPermissions) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final boolean shouldShowRequestPermissionRationale;
        final int requestType;
        switch (permissionsType) {
            case IntentManager.REQ_PERMISSION_LOCATION:
                alertDialog.setTitle("Location permission is not Enabled!");
                alertDialog.setMessage(getString(R.string.location_permission_question));
                requestType = IntentManager.REQ_PERMISSION_LOCATION;
                shouldShowRequestPermissionRationale = PrefUtils.getSharedPreference(this, PrefUtils.FIRST_TIME_LOCATION_PERMISSION, true)
                        || (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
                break;
            case IntentManager.REQ_PERMISSION_CONTACTS:
                alertDialog.setTitle("Contacts permission is not Enabled!");
                alertDialog.setMessage(getString(R.string.contacts_permission_question));
                requestType = IntentManager.REQ_PERMISSION_CONTACTS;
                shouldShowRequestPermissionRationale = PrefUtils.getSharedPreference(this, PrefUtils.FIRST_TIME_CONTACTS_PERMISSION, true)
                        || (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)
                        && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CALL_PHONE));
                break;
            default:
                shouldShowRequestPermissionRationale = true;
                requestType = IntentManager.REQ_PERMISSION_LOCATION;
                break;
        }
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (shouldShowRequestPermissionRationale) {
                    intentManager.requestPermissions(MainActivity.this, listPermissions, requestType);
                } else {
                    intentManager.openAppDetails(MainActivity.this, requestType);
                }
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Location GPS is not Enabled!");
        alertDialog.setMessage(getString(R.string.location_settings_question));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                intentManager.openLocationSettings(MainActivity.this);
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void setAddress(String address) {
        if (addressValue != null) {
            if (address != null && !address.isEmpty()) {
                addressValue.setText(address);
            } else {
                addressValue.setText("Address not detected yet!");
            }
        }
    }

    @Override
    public void processCommand(String command) {
        switch(command.toLowerCase().split(" ")[0]) {
            case "get":
            case "give":
                switch (command.toLowerCase().substring(command.indexOf(' ')+1)) {
                    case "me my location":
                        getPresenter().askForLocation();
                        break;
                    default:
                        break;
                }
                break;
            case "call":
                PrefUtils.setSharedPreference(this, PrefUtils.COMMAND, command);
                getPresenter().askForCallIntent();
                break;
            default:
                intentManager.searchOnGoogle(this, command);
                break;
        }
    }

    @Override
    public void callCommand() {
        String command = PrefUtils.getSharedPreference(this, PrefUtils.COMMAND, "");
        if (!command.isEmpty() && command.toLowerCase().split(" ").length <= 1) {
            intentManager.pickContent(this);
        } else {
            openCallIntent(command.toLowerCase().substring(command.indexOf(' ')+1));
        }
    }

    @Override
    public void openCallIntent(String phoneNumber) {
        intentManager.callIntent(this, phoneNumber);
    }

    @Override
    public void setSpeechInput(String input) {
        txtSpeechInput.setText(input);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void changeAddressVisibility(int visibility) {
        addressValue.setVisibility(visibility);
        addressValue.setText("Address not detected yet!");
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPresenter().processActivityResults(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getPresenter().processRequestedPermissions(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getPresenter().disconnectGoogleApiClient();
    }
}
