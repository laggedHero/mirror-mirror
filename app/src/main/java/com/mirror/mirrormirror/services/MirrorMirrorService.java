package com.mirror.mirrormirror.services;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by laggedhero on 10/29/16.
 */

public class MirrorMirrorService {

    private static final String ENDPOINT = "https://hackathon-mirrormirror.herokuapp.com";

    private static MirrorService mirrorService;

    public static MirrorService getService() {
        if (mirrorService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create());

            mirrorService = builder.build().create(MirrorService.class);
        }

        return mirrorService;
    }
}
