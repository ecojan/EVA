package com.mps.esteban.dagger;

import com.mps.esteban.activities.main.MainActivity;
import com.mps.esteban.activities.main.MainPresenter;
import com.mps.esteban.activities.youtube.YoutubeActivity;
import com.mps.esteban.activities.youtube.YoutubePresenter;
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

    void inject(YoutubeActivity youtubeActivity);

    void inject(YoutubePresenter youtubePresenter);

}
