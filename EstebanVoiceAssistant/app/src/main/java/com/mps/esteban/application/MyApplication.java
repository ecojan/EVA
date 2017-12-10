package com.mps.esteban.application;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.mps.esteban.dagger.AppComponent;
import com.mps.esteban.dagger.AppModule;
import com.mps.esteban.dagger.DaggerAppComponent;

/**
 * Created by cosmin on 03.10.2017.
 */

public class MyApplication extends Application {

    private static AppComponent appComponent;

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        createComponents(this);
        context = this;
    }

    private void createComponents(Context context){
        appComponent = DaggerAppComponent.builder().appModule(new AppModule()).build();
        appComponent.inject(this);
    }


    public static AppComponent getAppComponent(){
        return appComponent;
    }
}
