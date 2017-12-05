package com.mps.esteban.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.mps.esteban.mvp.BaseContract;

import java.util.List;

/**
 * Created by cosmin on 30.11.2017.
 */

class Contract {

    public interface ContractView extends BaseContract.ContractView {
        void showPermissionsAlert(int permissionsType, final List<String> listPermissions);
        void showSettingsAlert();
        void setAddress(String address);
        void processCommand(String command);
        void openCallIntent(String phoneNumber);
        void setSpeechInput(String input);
        Context getContext();
        void callCommand();
        void changeAddressVisibility(int visibility);
    }

    public interface ContractPresenter extends BaseContract.ContractPresenter {
        void processRequestedPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
        void processActivityResults(int requestCode, int resultCode, Intent data);
        void askForLocation();
        void askForTime(TextView resultData);
        BroadcastReceiver askForBattery(Context context, TextView resultData);
        void askForIpAddress(boolean IPv4, TextView resultData);
        void openCamera(Activity activity);
        void openSMS(Activity activity);
        void openMusicPlayer(Activity activity);
        void openDialer(Activity activity);
        void disconnectGoogleApiClient();
        void askForCallIntent();
    }

}
