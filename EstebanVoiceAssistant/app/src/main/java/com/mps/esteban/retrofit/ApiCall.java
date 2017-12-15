package com.mps.esteban.retrofit;

/**
 * Created by cosmin on 15.12.2017.
 */

public class ApiCall {

    private final ApiService apiService;

    public ApiCall(ApiService apiService) {
        this.apiService = apiService;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
