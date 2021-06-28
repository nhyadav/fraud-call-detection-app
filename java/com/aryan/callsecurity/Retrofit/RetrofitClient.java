package com.aryan.callsecurity.Retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
private  static  Retrofit retrofitClient = null;
public static  Retrofit getClient(){
//    if(retrofitClient == null){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .writeTimeout(60,TimeUnit.SECONDS)
                .build();
        retrofitClient = new Retrofit.Builder().baseUrl("http://192.168.43.128:5000")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create()).build();

    return retrofitClient;
}
}
