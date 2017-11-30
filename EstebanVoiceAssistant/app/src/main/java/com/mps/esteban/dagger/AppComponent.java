package com.mps.esteban.dagger;

import com.mps.esteban.activities.MainActivity;
import com.mps.esteban.activities.MainPresenter;
import com.mps.esteban.application.MyApplication;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by cosmin on 30.11.2017.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(MyApplication myApplication);

    void inject(MainActivity mainActivity);

    void inject(MainPresenter mainPresenter);

}
