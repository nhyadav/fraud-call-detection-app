package com.aryan.callsecurity.Retrofit;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IUploadAPI {
    @Multipart
    @POST("/api/upload/")
    Call<String> UploadFile(@Part MultipartBody.Part file);
}
