package com.mps.esteban.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import java.net.URLEncoder;

/**
 * Created by cosmin on 24.11.2017.
 */

public class IntentManager {

    public void callIntent(Context mContext, String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        mContext.startActivity(phoneIntent);
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
        mContext.startActivity(intent);
    }

    public void openMaps() {
//        Intent addressIntent = new Intent(Intent.ACTION_VIEW,
//                Uri.parse(String.format("geo:0,0?q=%s",
//                        URLEncoder.encode(address.getText().toString()))));
//        startActivity(addressIntent);
    }
}
