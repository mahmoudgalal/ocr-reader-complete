package com.google.android.gms.samples.vision.ocrreader.api;

import com.google.android.gms.samples.vision.ocrreader.exception.ErrorRequestException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppRetrofitManager {
    private static Retrofit mRetrofit;
    private static ApiService mApiService;

    public static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            synchronized (AppRetrofitManager.class) {
                if (mRetrofit == null) {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .build();
                    mRetrofit = new Retrofit.Builder()
                            .baseUrl("http://192.168.1.4:9000/api/") // url here
                            .addConverterFactory(GsonConverterFactory.create(new Gson()))
                            .client(okHttpClient)
                            .build();
                    mApiService = mRetrofit.create(ApiService.class);
                }
            }
        }
        return mRetrofit;
    }

    private static OkHttpClient getRequestHeader(int readTime, int connectTime) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(readTime, TimeUnit.SECONDS)
                .connectTimeout(connectTime, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .method(chain.request().method(), chain.request().body())
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
        return okHttpClient;
    }

    public static <T> T performRequest(Call<T> request) throws IOException, ErrorRequestException {
        Response<T> response = request.execute();

        if (response == null || !response.isSuccessful() || response.errorBody() != null)
            throw new ErrorRequestException(response);

        return response.body();
    }

    public static ApiService getApiService() {
        if (mApiService == null) {
            getRetrofit();
        }
        return mApiService;
    }
}
