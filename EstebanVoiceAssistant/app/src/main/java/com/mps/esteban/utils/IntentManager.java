package com.mps.esteban.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
    public static final int REQUEST_IMAGE_CAPTURE = 7;
    public static String FACEBOOK_URL = "https://www.facebook.com/YourPageName";

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f";
            } else { //older versions of fb app
                return "fb://page";
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

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

    public void openGoogleMaps(Context mContext) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(mapIntent);
    }

    public void openFacebook(Context mContext) {
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        String facebookUrl = getFacebookPageURL(mContext);
        facebookIntent.setData(Uri.parse(facebookUrl));
        mContext.startActivity(facebookIntent);
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

    public void openMapsDirections(Context mContext, float lat, float lon) {
        String geolocation = String.valueOf(lat) + "," + String.valueOf(lon);
        Intent addressIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(String.format("google.navigation:q=%s",
                        URLEncoder.encode(geolocation))));
        mContext.startActivity(addressIntent);
    }

    public void openYoutubeActivity(Context mContext) {
        Intent intent = new Intent(mContext, YoutubeActivity.class);
        mContext.startActivity(intent);
    }

    public void openYoutubeIntent(Context mContext, String query) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        if (query != null)
            intent.putExtra("query", query);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void openSMS(Activity activity) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setData(Uri.parse("sms:"));
        activity.getApplicationContext().startActivity(sendIntent);
    }

    public void sendMessage(Activity activity, String s) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", s);
        activity.getApplicationContext().startActivity(sendIntent);
    }

    public void openMusicPlayer(Activity activity) {
        Intent musicIntent = new Intent("android.intent.action.MUSIC_PLAYER");
        musicIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        musicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(musicIntent);
    }

    public void openDialer(Activity activity) {
        Intent dialerIntent = new Intent(Intent.ACTION_DIAL);
        dialerIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        dialerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(dialerIntent);
    }

    public void openCamera(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
