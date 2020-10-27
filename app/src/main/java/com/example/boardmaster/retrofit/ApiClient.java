package com.example.boardmaster.retrofit;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //private static String localhost = "192.168.1.102"; //leiligheten
    private static  String localhost = "10.24.90.220"; //skolen
    //private static  String localhost = "10.0.0.21"; //bestemor
    //private static String localhost ="10.0.0.107"; //mamma
    private static String baseUrl = "http://" + localhost + ":8080/MobileApp/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient.Builder().addInterceptor(interceptor).build())
                    .build();

        return retrofit;
    }
}
