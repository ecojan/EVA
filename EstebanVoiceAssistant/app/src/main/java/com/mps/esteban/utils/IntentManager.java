package com.mps.esteban.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.mps.esteban.R;
import com.mps.esteban.activities.youtube.YoutubeActivity;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * Created by cosmin on 24.11.2017.
 */

public class IntentManager {
    public static final int REQ_VOICE_SEARCH = 1;
    public static final int ALL_PERMISSIONS_RESULT = 101;
    public static final int REQ_SETTINGS_LOCATION = 102;
    public static final int REQ_PERMISSION_LOCATION = 103;
    public static final int REQ_PERMISSION_CONTACTS = 105;
    public static final int RESULT_ACTION_PICK = 104;
    public static final int RECOVERY_REQUEST = 1;

    public void callIntent(Context mContext, String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        mContext.startActivity(phoneIntent);
    }

    public void pickContent(Activity mActivity) {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mActivity.startActivityForResult(i, RESULT_ACTION_PICK);
    }

    public void goToEmail(Context mContext) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
//        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {email.getText().toString()});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Email Subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "My email content");
        mContext.startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public void openLocationSettings(Context mContext) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Activity mActivity = (Activity) mContext;
        mActivity.startActivityForResult(intent, IntentManager.REQ_SETTINGS_LOCATION);
    }

    public void promptSpeechInput(Context mContext) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mContext.getString(R.string.speech_prompt));
        Activity mActivity = (Activity) mContext;
        try {
            mActivity.startActivityForResult(intent, REQ_VOICE_SEARCH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(mContext, mContext.getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    public void openAppDetails(Context mContext, int permissionType) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        Activity mActivity = (Activity) mContext;
        switch (permissionType) {
            case REQ_PERMISSION_LOCATION:
                mActivity.startActivityForResult(intent, REQ_PERMISSION_LOCATION);
                break;
            case REQ_PERMISSION_CONTACTS:
                mActivity.startActivityForResult(intent, REQ_PERMISSION_CONTACTS);
                break;
        }
    }

    public void requestPermissions(Activity mActivity, List<String> listPermissions, int requestType) {
        switch (requestType) {
            case REQ_PERMISSION_LOCATION:
                PrefUtils.setSharedPreference(mActivity, PrefUtils.FIRST_TIME_LOCATION_PERMISSION, false);
                ActivityCompat.requestPermissions(mActivity,
                        listPermissions.toArray(new String[listPermissions.size()]),
                        REQ_PERMISSION_LOCATION);
                break;
            case REQ_PERMISSION_CONTACTS:
                PrefUtils.setSharedPreference(mActivity, PrefUtils.FIRST_TIME_CONTACTS_PERMISSION, false);
                ActivityCompat.requestPermissions(mActivity,
                        listPermissions.toArray(new String[listPermissions.size()]),
                        REQ_PERMISSION_CONTACTS);
                break;
        }

    }

    public void searchOnGoogle(Context mContext, String query){
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        mContext.startActivity(intent);
    }

    public void openMapWithCurrentLocation(Context mContext, String addressValue) {
        String map = "http://maps.google.co.in/maps?q=" + addressValue;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        mContext.startActivity(i);
    }

//    public void openMaps(Context mContext, String address) {
//        Intent addressIntent = new Intent(Intent.ACTION_VIEW,
//                Uri.parse(String.format("geo:0,0?q=%s",
//                        URLEncoder.encode(address))));
//        mContext.startActivity(addressIntent);
//    }

    public void openYoutubeActivity(Context mContext) {
        Intent intent = new Intent(mContext, YoutubeActivity.class);
        mContext.startActivity(intent);
    }

    public void openYoutubeIntent(Context mContext, String query) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", query);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
