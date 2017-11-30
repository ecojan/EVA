package com.mps.esteban.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by avlad on 5/19/2017.
 */

public abstract class BaseActivity<PRESENTER extends BaseContract.ContractPresenter> extends AppCompatActivity {

    private Unbinder unbinder;
    private PRESENTER presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());
        unbinder = ButterKnife.bind(this);
        presenter = bindPresenter();
        injectDependencies();
        setupToolbar();
    }


    @Override
    protected void onDestroy() {
        if(unbinder != null){
            unbinder.unbind();
        }
        super.onDestroy();
    }

    public abstract void setupToolbar();

    @LayoutRes
    public abstract int bindLayout();

    public abstract PRESENTER bindPresenter();

    public PRESENTER getPresenter() {
        return presenter;
    }

    public abstract void injectDependencies();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
