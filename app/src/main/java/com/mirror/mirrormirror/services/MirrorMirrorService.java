package com.mirror.mirrormirror.services;

import retrofit2.Retrofit;

/**
 * Created by laggedhero on 10/29/16.
 */

public class MirrorMirrorService {

    private static final String ENDPOINT = "http://www.google.com";

    private static MirrorService mirrorService;

    public static MirrorService getService() {
        if (mirrorService == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(ENDPOINT);

            mirrorService = builder.build().create(MirrorService.class);
        }

        return mirrorService;
    }
}