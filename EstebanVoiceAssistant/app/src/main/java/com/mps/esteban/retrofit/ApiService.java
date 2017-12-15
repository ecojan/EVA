package com.mps.esteban.retrofit;

import com.mps.esteban.forms.ResponseWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by cosmin on 15.12.2017.
 */

public interface ApiService {

    @GET("/data/2.5/weather")
    Call<ResponseWeather> getWeatherByCity(@Query("q") String cityName, @Query("appid") String appid, @Query("units") String units);

    @GET("/data/2.5/weather")
    Call<ResponseWeather> getWeatherByCurrentLocation(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid, @Query("units") String units);

}
