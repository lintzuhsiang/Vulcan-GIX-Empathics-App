package com.example.myapplication;

import android.util.Log;

import org.json.JSONException;
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
//        @FormUrlEncoded
        @POST("/post_senti_score")
        Call<ResponseBody> uploadScore(@Body RequestBody body);

        @Multipart
        @POST("/post_pic_test")
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part photo, @Part("description") RequestBody description);

//        @Multipart
//        @POST("/post_pic")
//        Call<ResponseBody> uploadImage2(@Part MultipartBody.Part photo, @Body RequestBody body);


        @Multipart
        @POST("/post_pic")
        Call<ResponseBody> uploadImage2(@Part MultipartBody.Part photo, @Part("session_id") RequestBody session_id, @Part("device_id") RequestBody device_id, @Part("seq") RequestBody sequence_id, @Part("description") RequestBody description);

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

    public void uploadImage2(String sequence_Id, File file) {

        final File file2 = new File("/storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/2019_11_18_23_16_28.jpg");
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        RequestBody session_id = RequestBody.create(MediaType.parse("text/plain"), User.sessionId);
        RequestBody device_id = RequestBody.create(MediaType.parse("text/plain"), User.deviceId);
        RequestBody sequence_id = RequestBody.create(MediaType.parse("text/plain"), sequence_Id);

//
//        JSONObject request = new JSONObject();
//        try {
//            request.put("session_id",user.sessionId);
//            request.put("seq",sequence_Id);
//            request.put("device_id",user.deviceId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, String.valueOf(request));
//
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"),request.toString());


//        Call call = uploadAPIs.uploadImage2(part,body);
        Call call = uploadAPIs.uploadImage2(part, session_id, device_id, sequence_id, description);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "upLoadImage");
//                Log.d(TAG, String.valueOf(response.code()));

                try {
                    Log.d(TAG, String.valueOf(response.code()));
                    Log.d(TAG, String.valueOf(response.body().string()));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "fail in upload image");

            }
        });

    }

    public void uploadImage(String sequence_Id, File file) {

        final File file2 = new File("/storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/2019_11_18_23_16_28.jpg");
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
//        RequestBody session_id = RequestBody.create(MediaType.parse("text/plain"), User.sessionId);
//        RequestBody device_id = RequestBody.create(MediaType.parse("text/plain"), User.deviceId);

//        RequestBody sequence_id = RequestBody.create(MediaType.parse("text/plain"), sequence_Id);


        Call call = uploadAPIs.uploadImage(part, description);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "upLoadImage");
//                Log.d(TAG, String.valueOf(response.code()));

                try {
                    Log.d(TAG, String.valueOf(response.code()));
                    Log.d(TAG, String.valueOf(response.body().string()));
                    Log.d(TAG, "upLoadImage ends2");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "upLoadImage ends3");

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "fail in upload image");

            }
        });
        Log.d(TAG, "upLoadImage ends");

    }

    public void uploadScore(String sequence_Id, String score_) {
        Log.d(TAG, "upload score");
        Log.d(TAG, score_);
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        JSONObject request = new JSONObject();
        try {
            request.put("session_id", user.sessionId);
            request.put("seq", sequence_Id);
            request.put("sentiment_score", score_);
            request.put("device_id", user.deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, String.valueOf(request));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), request.toString());
//        Log.d(TAG, String.valueOf(body));

        Call call = uploadAPIs.uploadScore(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, String.valueOf(response.code()));
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

                // Log.d(TAG, String.valueOf(response.body().string()));
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
//

            }


            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, String.valueOf(t));
            }

        });

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
