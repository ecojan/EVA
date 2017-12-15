package com.mps.esteban.activities.youtube;

import android.os.Handler;

import com.mps.esteban.application.MyApplication;
import com.mps.esteban.mvp.BasePresenter;

import javax.inject.Inject;

/**
 * Created by cosmin on 13.12.2017.
 */

public class YoutubePresenter extends BasePresenter<Contract.ContractView> implements Contract.ContractPresenter {

    @Inject Handler handler;

    public YoutubePresenter(Contract.ContractView view) {
        super(view);
    }

    @Override
    public void injectDependencies() {
        MyApplication.getAppComponent().inject(this);
    }
}
