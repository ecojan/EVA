package com.mps.esteban.activities.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import java.util.Arrays;
import java.util.List;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mps.esteban.R;
import com.mps.esteban.application.MyApplication;
import com.mps.esteban.forms.Coordinates;
import com.mps.esteban.forms.FacebookDetails;
import com.mps.esteban.forms.ResponseWeather;
import com.mps.esteban.mvp.BaseActivity;
import com.mps.esteban.utils.IntentManager;
import com.mps.esteban.utils.PrefUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<Contract.ContractPresenter> implements Contract.ContractView {

    private static final String TAG = "MainActivity";

    @BindView(R.id.txtSpeechInput)
    TextView txtSpeechInput;

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.temperature)
    TextView temperature;
    @BindView(R.id.pressure)
    TextView pressure;
    @BindView(R.id.wind)
    TextView wind;

    @BindView(R.id.addressValue)
    TextView addressValue;
    @BindView(R.id.profile_name)
    TextView profile_name;
    @BindView(R.id.email_address)
    TextView email_address;
    @BindView(R.id.number_of_friends)
    TextView number_of_friends;
    @BindView(R.id.weatherContainer)
    View weatherContainer;
    @BindView(R.id.facebook_container)
    View facebook_container;
    @BindView(R.id.activity_main_mexican_btn)
    ImageButton mexicanBtn;

    @Inject
    IntentManager intentManager;

    Coordinates cityCoordinatesWeather = null;
    AccessToken mAccessToken;
    AccessTokenTracker accessTokenTracker;
    CallbackManager mCallbackManager;

    @Override
    public void setupToolbar() {
    }

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
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //you will get access token here
                        mAccessToken = loginResult.getAccessToken();


                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                mAccessToken = currentAccessToken;
            }
        };

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));
//        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_notifications"));
    }

    @OnClick(value = {R.id.activity_main_mexican_btn, R.id.addressValue, R.id.weatherContainer})
    public void onSelectEntry(View view) {
        switch (view.getId()) {
            case R.id.activity_main_mexican_btn:
                intentManager.promptSpeechInput(this);
                break;
            case R.id.addressValue:
                intentManager.openMapWithCurrentLocation(this, addressValue.getText().toString());
                break;
            case R.id.weatherContainer:
                if (cityCoordinatesWeather != null) {
                    intentManager.openMapsDirections(this, cityCoordinatesWeather.getLat(), cityCoordinatesWeather.getLon());
                }
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
    public void setAddress(String address, double lat, double lon, String cityName) {

        if (PrefUtils.getSharedPreference(this, "facebookFlag", false)) {
            return;
        } else if (PrefUtils.getSharedPreference(this, "weatherFlag", false)) {
            facebook_container.setVisibility(View.GONE);
            addressValue.setVisibility(View.GONE);
            getPresenter().getWeatherByLatLon(lat, lon, getString(R.string.weather_appid), "metric");
        } else {
            if (addressValue != null) {
                if (address != null && !address.isEmpty()) {
                    addressValue.setText(address);
                } else {
                    addressValue.setText("Address not detected yet!");
                }
            }
        }
    }

    @Override
    public void processCommand(String command) {
        addressValue.setVisibility(View.GONE);
        weatherContainer.setVisibility(View.GONE);
        facebook_container.setVisibility(View.GONE);
        PrefUtils.setSharedPreference(this, "weatherFlag", false);
        PrefUtils.setSharedPreference(this, "facebookFlag", false);
        switch (command.toLowerCase().split(" ")[0]) {
            case "get":
            case "give":
                switch (command.toLowerCase().substring(command.indexOf(' ') + 1)) {
                    case "me the weather for my location":
                        PrefUtils.setSharedPreference(this, "weatherFlag", true);
                    case "me my location":
                        getPresenter().askForLocation();
                        break;
                    case "me the time":
                        getPresenter().askForTime(txtSpeechInput);
                        break;
                    case "me the battery":
                        this.registerReceiver(getPresenter().askForBattery(this, txtSpeechInput),
                                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        break;
                    case "me the ip address":
                        /*true for IPv4, false for IPv6*/
                        getPresenter().askForIpAddress(true, txtSpeechInput);
                        break;
                    case "me my facebook details":
                        PrefUtils.setSharedPreference(this, "facebookFlag", true);
                        getPresenter().getFacebookDetails();
                        break;
                    default:
                        intentManager.searchOnGoogle(this, command);
                        break;
                }
                break;
            case "weather":
                if (command.toLowerCase().substring(command.indexOf(' ') + 1) != null &&
                        !command.toLowerCase().substring(command.indexOf(' ') + 1).isEmpty()) {
                    getPresenter().getWeatherByCity(command.toLowerCase().substring(command.indexOf(' ') + 1), getString(R.string.weather_appid), "metric");
                }
                break;
            case "shutdown":
                shutDown();
                break;
            case "open":
                switch (command.toLowerCase().substring(command.indexOf(' ') + 1).split(" ",2)[0]) {
                    case "camera":
                        intentManager.openCamera(this);
                        break;
                    case "sms":
                        int i = command.split(" ").length;

                        if (i != 2) {
                            String s = command.toLowerCase().split(" ",3)[2];
                            intentManager.sendMessage(this,s);
                        } else {
                            intentManager.openSMS(this);
                        }
                        break;
                    case "music":
                        intentManager.openMusicPlayer(this);
                        break;
                    case "dialer":
                    case "dialler":
                        intentManager.openDialer(this);
                        break;
                    case "email":
                        intentManager.goToEmail(this);
                        break;
                    case "maps":
                        intentManager.openGoogleMaps(this);
                        break;
                    case "facebook":
                        intentManager.openFacebook(this);
                        break;
                    case "youtube":
                        intentManager.openYoutubeIntent(this, null);
                        break;
                    default:
                        intentManager.searchOnGoogle(this, command);
                        break;
                }
                break;
            case "call":
                PrefUtils.setSharedPreference(this, PrefUtils.COMMAND, command);
                getPresenter().askForCallIntent();
                break;
            case "youtube":
                switch (command.toLowerCase().split(" ")[1]) {
                    case "search":
                        intentManager.openYoutubeIntent(this, command.toLowerCase()
                                .substring(command.indexOf(" ", command.indexOf(" ") + 1)));
                        break;
                    case "video":
                        intentManager.openYoutubeActivity(this);
                        break;
                }
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
            openCallIntent(command.toLowerCase().substring(command.indexOf(' ') + 1));
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
        facebook_container.setVisibility(View.GONE);
        weatherContainer.setVisibility(View.GONE);
        addressValue.setVisibility(visibility);
        addressValue.setText("Address not detected yet!");
    }

    @Override
    public void setFacebookDetails(FacebookDetails facebookDetails) {
        facebook_container.setVisibility(View.VISIBLE);
        addressValue.setVisibility(View.GONE);
        weatherContainer.setVisibility(View.GONE);
        profile_name.setText(facebookDetails.getName());
        email_address.setText(facebookDetails.getEmail());
        number_of_friends.setText("Friends: " + String.valueOf(facebookDetails.getNumber_of_friends()));
    }

    @Override
    public void setWeatherValues(ResponseWeather responseWeather) {
        facebook_container.setVisibility(View.GONE);
        addressValue.setVisibility(View.GONE);
        weatherContainer.setVisibility(View.VISIBLE);

        if (responseWeather != null) {
            cityCoordinatesWeather = responseWeather.getCoordinates();
            description.setText("Description: " + responseWeather.getWeather().get(0).getDescription());
            temperature.setText("Temperature: " + String.valueOf(responseWeather.getMain().getTemp()) + " °C");
            pressure.setText("Pressure: " + String.valueOf(responseWeather.getMain().getPressure()) + " hPa");
            wind.setText("Wind: " + String.valueOf(responseWeather.getWind().getSpeed()) + " m/s");
        } else {
            cityCoordinatesWeather = null;
            description.setText("Description: Not found");
            temperature.setText("Temperature: Not found");
            pressure.setText("Pressure: Not found");
            wind.setText("Wind: Not found");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPresenter().processActivityResults(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    public void shutDown() {
        this.finish();
        System.exit(0);
    }
}
