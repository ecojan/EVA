package com.mps.esteban.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.mps.esteban.mvp.BaseContract;

import java.util.List;

/**
 * Created by cosmin on 30.11.2017.
 */

class Contract {

    public interface ContractView extends BaseContract.ContractView {
        void showPermissionsAlert(final List<String> listPermissions);
        void showSettingsAlert();
        void setAddress(String address);
        void setSpeechInput(String input);
        Context getContext();
        void changeAddressVisibility(int visibility);
    }

    public interface ContractPresenter extends BaseContract.ContractPresenter {
        void processRequestedPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
        void processActivityResults(int requestCode, int resultCode, Intent data);
        void askForLocation();
        void disconnectGoogleApiClient();
    }

}
