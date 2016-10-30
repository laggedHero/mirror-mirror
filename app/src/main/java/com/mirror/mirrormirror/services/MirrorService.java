package com.mirror.mirrormirror.services;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by laggedhero on 10/29/16.
 */

public interface MirrorService {

    @Multipart
    @POST("/mirror/photo")
    Call<FeedbackMessage> sendPhoto(@Part MultipartBody.Part photo);
}
