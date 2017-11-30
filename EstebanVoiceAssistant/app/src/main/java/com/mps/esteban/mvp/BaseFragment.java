package com.mps.esteban.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by avlad on 5/19/2017.
 */

public abstract class BaseFragment<PRESENTER extends BaseContract.ContractPresenter> extends Fragment {

    private Unbinder unbinder;
    private PRESENTER presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (inflater == null){
            inflater = LayoutInflater.from(container.getContext());
        }
        View view = inflater.inflate(bindLayout(),container,false);
        unbinder = ButterKnife.bind(this,view);
        presenter = bindPresenter();
        injectDependencies();
        onCreateFragmentView();
        return view;
    }



    public abstract int bindLayout();

    @Override
    public void onDestroyView() {
        if(unbinder != null){
            unbinder.unbind();
        }
        super.onDestroyView();

    }

    public abstract void onCreateFragmentView();

    public abstract void injectDependencies();

    public abstract PRESENTER bindPresenter();

    public PRESENTER getPresenter() {
        return presenter;
    }


}
