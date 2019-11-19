package com.example.myapplication;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;

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
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class NetworkClient {
    private static final String BASE_URL = "http://empathics.azurewebsites.net";
    private static final String TAG = "Empethics/NetworkClient";
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

    public String sessionId;
    User user = new User();


    public void healthCheck() {
        Log.d("client", "healthCheck");
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        Call call = uploadAPIs.healthCheck();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("client2", String.valueOf(response.code()));
                Log.d("ID52", String.valueOf(sessionId));

//                resultListener.onComplete(response.body());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("t", String.valueOf(t));
            }

        });
//        getResponseListener(new responseListener() {
//            @Override
//            public void onComplete(Object result) {
//                sessionID = result.toString();
//                Log.d("session0",sessionID);
//            }
//        });
//        return sessionID;
    }

    String server;
    Callback callback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            server = response.body().toString();
        }

        @Override
        public void onFailure(Call call, Throwable t) {

        }
    };

    public interface UploadAPIs {

//        @GET("/get_session_id")
//        Call<User> getUser();

        @GET("/get_session_id")
        Call<ResponseBody> getSessionID();

        @GET("/health_check")
        Call<ResponseBody> healthCheck();


        //        @POST("/post_senti_socre")
//        Call<ResponseBody> uploadScore(@Query("session_id") String session_id, @Query("sequence_id") String sequence_id, @Query("device_id") String device_id, @Query("score") String score);
        @FormUrlEncoded
        @POST("/post_senti_score")
        Call<ResponseBody> uploadScore(@Field("session_id") String session_id, @Field("sequence_id") String sequence_id, @Field("device_id") String device_id, @Field("score") String score);


        @Multipart
        @POST("/post_pic")
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part photo, @Part("session_id") RequestBody session_id, @Part("device_id") RequestBody device_id, @Part("sequence_id") RequestBody sequence_id, @Part("longitude") RequestBody longitude, @Part("latitude") RequestBody latitude, @Part("description") RequestBody description);

        @Multipart
        @POST("/post_mic")
        Call<ResponseBody> uploadAudio(@Part MultipartBody.Part audio, @Part("session_id") RequestBody session_id, @Part("description") RequestBody description);

    }

//    public void getUser(){
//        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
//        Call call = uploadAPIs.getUser();
//        call.enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                User user = response.body();
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t) {
//
//            }
//        });
//    }

    public void uploadImage(String sequence_Id, File file) {

        final File file2 = new File("/storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/2019_11_18_23_16_28.jpg");
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file2);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        RequestBody session_id = RequestBody.create(MediaType.parse("text/plain"), User.sessionId);
        RequestBody device_id = RequestBody.create(MediaType.parse("text/plain"), User.deviceId);
        RequestBody lontitude = RequestBody.create(MediaType.parse("text/plain"), "0");
        RequestBody latitude = RequestBody.create(MediaType.parse("text/plain"), "0");
        RequestBody sequence_id = RequestBody.create(MediaType.parse("text/plain"), sequence_Id);


        Call call = uploadAPIs.uploadImage(part, session_id, device_id, sequence_id, lontitude, latitude, description);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "upLoadImage");
                Log.d(TAG, String.valueOf(response.code()));

//                try {
//                    Log.d(TAG, String.valueOf(response.code()));
////                    Log.d(TAG, String.valueOf(response.body().string()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "fail in upload image");

            }
        });

    }

    public void uploadScore(String sequence_Id, String score_) {
        Log.d(TAG, "upload score");
        Log.d(TAG, score_);
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

//        RequestBody session_id = RequestBody.create(MediaType.parse("text/plain"), User.sessionId);
//        RequestBody sequence_id = RequestBody.create(MediaType.parse("text/plain"), sequence_Id);
//        RequestBody device_id = RequestBody.create(MediaType.parse("text/plain"), User.deviceId);
//        RequestBody score = RequestBody.create(MediaType.parse("text/plain"), score_);
        Call call = uploadAPIs.uploadScore(user.sessionId, sequence_Id, user.deviceId, score_);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "uploadScore response");
//                Log.d(TAG, String.valueOf(response.code()));
//                    Log.d(TAG, response.body().string());

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, "fail on upload score");

            }


        });

    }

    public void uploadAudio(String sequence_Id, File file) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);


        RequestBody fileReqBody = RequestBody.create(MediaType.parse("audio/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("audio", file.getName(), fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "audio-type");
        RequestBody sequence_id = RequestBody.create(MediaType.parse("text/plain"), sequence_Id);


        Call call = uploadAPIs.uploadAudio(part, sequence_id, description);
        Log.d(TAG, "upLoadAudio");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                //                    Log.d(TAG, String.valueOf(response.body().string()));
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();

            }
        });

    }


    public void getSessionID() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        Call call = uploadAPIs.getSessionID();
        Log.d(TAG, "getSessionID");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, String.valueOf(response.code()));
                try {
                    Log.d(TAG, "getSessionID");

                    sessionId = response.body().string();
                    user.setSessionId(sessionId);

//                    if(resultListener!=null){
//                        resultListener.onComplete(sessionId);
//                    }

                } catch (IOException e) {
                    Log.d(TAG, "getSessionID2");

                    e.printStackTrace();
                }
//                MainActivity.sessionId = sessionId;
//                MainActivity.klistener.onChanged(MainActivity.SessionID);

            }


            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, String.valueOf(t));
            }

        });

//        getResponseListener(new responseListener() {
//
//            @Override
//            public String onComplete(Object result) {
//                sessionID = result.toString();
//                Log.d("session0",sessionID);
//                SS[0] = sessionID;
//                return  sessionID;
//
//            }
//        });
//
    }

    public interface ResponseListener {
        void onComplete(String result);
    }

    public ResponseListener resultListener;

    public void setResponseListener(ResponseListener listener) {

        resultListener = listener;
    }

}
