package com.mps.esteban.mvp;

/**
 * Created by cosmin on 07.07.2017.
 */

public abstract class BasePresenter<VIEW extends BaseContract.ContractView> {

    private final VIEW view;


    public BasePresenter(VIEW view) {
        this.view = view;
        injectDependencies();
    }


    public VIEW getView() {
        return view;
    }

    public abstract void injectDependencies();

}
