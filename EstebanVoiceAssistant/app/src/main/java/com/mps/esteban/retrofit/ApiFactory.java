package com.mps.esteban.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cosmin on 15.12.2017.
 */

public class ApiFactory {

    private static ApiCall apiCall;

    public static final String weatherAPI = "weather";

    private static Retrofit getClient(String baseUrl, String type) {
        switch (type) {
            case weatherAPI:
                return new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(getHttpClient())
                        .build();
            default:
                return null;
        }
    }

    private static ApiCall getApiService(String baseUrlCloud){
        return new ApiCall(getClient(baseUrlCloud, ApiFactory.weatherAPI).create(ApiService.class));
    }

    public static final ApiCall createApiService(String baseUrlCloud){
        if(apiCall == null){
            apiCall = getApiService(baseUrlCloud);
        }
        return apiCall;
    }

    private static OkHttpClient getHttpClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);


        HttpLoggingInterceptor interceptor2 = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        return new OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(interceptor2).build();
    }

}
