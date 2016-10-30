package com.mirror.mirrormirror.services;

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
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .addConverterFactory(MoshiConverterFactory.create());

            mirrorService = builder.build().create(MirrorService.class);
        }

        return mirrorService;
    }
}
