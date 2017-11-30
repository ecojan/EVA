package com.mps.esteban.dagger;

import android.content.Context;
import android.os.Handler;

import com.mps.esteban.utils.IntentManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by cosmin on 30.11.2017.
 */

@Module
public class AppModule {

    private final IntentManager intentManager;
    private final Handler handler;

    public AppModule() {
        intentManager = new IntentManager();
        handler = new Handler();
    }

    @Provides
    @Singleton
    IntentManager providesIntentManager(){
        return intentManager;
    }

    @Provides
    @Singleton
    Handler providesHandler(){
        return handler;
    }

}
