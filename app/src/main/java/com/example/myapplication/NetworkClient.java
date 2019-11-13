package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Body;

public class NetworkClient {
    private static final String BASE_URL = "http://empathics.azurewebsites.net";
//    private static final String BASE_URL =  "http://20.190.61.212:8000";
    private static Retrofit retrofit;
    public static Retrofit getRetrofitClient(NetworkClient context) {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
    public interface UploadAPIs{
    @GET("/health_check")
    Call<ResponseBody> uploadScore();
    @Multipart
        @POST("/post_pic")
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part photo, @Part("description") RequestBody description);
    @Multipart
    @POST("/post_mic")
    Call<ResponseBody> uploadAudio(@Part MultipartBody.Part audio, @Part("description") RequestBody description);

    }

    public void uploadImage(File file){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);


        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image",file.getName(),fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),"image-type");

        Call call = uploadAPIs.uploadImage(part,description);
        Log.d("client","upLoadToServer");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    Log.d("client", String.valueOf(response.body().string()));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();

            }
        });

    }

    public void uploadScore(String score){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
//        RequestBody ReqBody = RequestBody.create()
        Call call = uploadAPIs.uploadScore();
        call.enqueue(new Callback(){
            @Override
            public void onResponse(Call call,Response response){
//                Log.d("client", response.body().string());
                Log.d("client", String.valueOf(response.code()));
            }
            @Override
            public void onFailure(Call call, Throwable t){
                t.printStackTrace();
            }

        });

    }

    public void uploadAudio(File file){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);


        RequestBody fileReqBody = RequestBody.create(MediaType.parse("audio/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("audio",file.getName(),fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),"audio-type");

        Call call = uploadAPIs.uploadAudio(part,description);
        Log.d("client","upLoadToServer2");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    Log.d("client", String.valueOf(response.body().string()));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();

            }
        });

    }

}
