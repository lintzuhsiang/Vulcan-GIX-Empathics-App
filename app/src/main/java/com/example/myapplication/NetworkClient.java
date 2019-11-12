package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import java.io.File;

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
//    private static final String BASE_URL = "http://empathics.azurewebsites.net";
    private static final String BASE_URL =  "http://52.183.116.154:8000";
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

    }

    public void uploadImage(File file){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

//        File file = new File(filePath);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image",file.getName(),fileReqBody);

//        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*")," /storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/2019_10_30_19_46_42.jpg");
//        MultipartBody.Part part = MultipartBody.Part.createFormData("image","2019_10_30_19_46_42.jpg");

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"),"image-type");

        Call call = uploadAPIs.uploadImage(part,description);
        Log.d("client","upLoadToServer");
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {

//                NetworkClient client = (NetworkClient) response.body();
                Log.d("client", String.valueOf(response.body()));
                Log.d("client", String.valueOf(response.code()));

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
}
