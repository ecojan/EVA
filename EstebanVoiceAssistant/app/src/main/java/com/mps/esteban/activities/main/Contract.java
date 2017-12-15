package com.mps.esteban.activities.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.FaceDetector;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.mps.esteban.forms.FacebookDetails;
import com.mps.esteban.forms.ResponseWeather;
import com.mps.esteban.mvp.BaseContract;

import java.util.List;

/**
 * Created by cosmin on 30.11.2017.
 */

class Contract {

    public interface ContractView extends BaseContract.ContractView {
        void showPermissionsAlert(int permissionsType, final List<String> listPermissions);
        void showSettingsAlert();
        void setAddress(String address, double lat, double lon, String cityName);
        void processCommand(String command);
        void openCallIntent(String phoneNumber);
        void setSpeechInput(String input);
        Context getContext();
        void callCommand();
        void changeAddressVisibility(int visibility);
        void setFacebookDetails(FacebookDetails facebookDetails);
        void setWeatherValues(ResponseWeather responseWeather);
    }

    public interface ContractPresenter extends BaseContract.ContractPresenter {
        void processRequestedPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
        void processActivityResults(int requestCode, int resultCode, Intent data);
        void askForLocation();
        void askForTime(TextView resultData);
        BroadcastReceiver askForBattery(Context context, TextView resultData);
        void askForIpAddress(boolean IPv4, TextView resultData);
        void disconnectGoogleApiClient();
        void askForCallIntent();
        void getFacebookDetails();
        void getWeatherByCity(String cityName, String appid, String metric);
        void getWeatherByLatLon(double lat, double lon, String appid, String metric);
    }

}
