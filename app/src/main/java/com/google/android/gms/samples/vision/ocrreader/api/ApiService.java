package com.google.android.gms.samples.vision.ocrreader.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("")
    @Multipart
    Call<String> uploadImage(@Part MultipartBody.Part image);

    @POST
    @FormUrlEncoded
    Call<String> addReceipt(@Field("total") String totalAmount, @Field("UUID") String UUID);

}
