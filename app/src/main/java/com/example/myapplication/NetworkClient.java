package com.example.myapplication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

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
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public class NetworkClient {
    private static final String BASE_URL = "http://empathics.azurewebsites.net";
    private static final String TAG = "Empethics/NetworkClient";
    //    private static final String BASE_URL =  "http://20.190.61.212:8000";
    private static Retrofit retrofit;

    private boolean imageResult = false;
    private boolean scoreResult = false;


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
    boolean finalResult = false;

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

    public interface UploadAPIs {

        @GET("/get_session_id")
        Call<ResponseBody> getSessionID();

        @GET("/health_check")
        Call<ResponseBody> healthCheck();

        @POST("/post_senti_score")
        Call<ResponseBody> uploadScore(@Body RequestBody body);

        @Multipart
        @POST("/post_pic_test")
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part photo, @Part("description") RequestBody description);

        @Multipart
        @POST("/post_pic")
        Call<ResponseBody> uploadImage2(@Part MultipartBody.Part photo, @Part("data") RequestBody body);

        @Multipart
        @POST("/post_mic")
        Call<ResponseBody> uploadAudio(@Part MultipartBody.Part audio, @Part("session_id") RequestBody session_id, @Part("description") RequestBody description);

        @POST("/post_ml")
        Call<ResponseBody> uploadML(@Body RequestBody body);

    }

    public void uploadML(String session_Id,Integer seq){
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        JSONObject request = new JSONObject();
        try{
            request.put("session_id",session_Id);
            request.put("seq",seq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.valueOf(request));
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),request.toString());
        Call call = uploadAPIs.uploadML(body);
        call.enqueue(new Callback<ResponseBody>(){

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG,"uploadML success");
                try {
                    String result = response.body().string();
                    Log.d(TAG, String.valueOf(response.code()));
                    Log.d(TAG,result);

                    if(resultListener!=null){
                        Log.d(TAG,"upload ML listener");
                        resultListener.onComplete(result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void uploadImage2(String sequence_Id, File file) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        JSONObject request = new JSONObject();
        try {
            request.put("session_id",user.sessionId);
            request.put("seq",sequence_Id);
            request.put("device_id",user.deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(request));

        RequestBody json = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(request));

        Call call = uploadAPIs.uploadImage2(part,json);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "upload image onResponse");
                try {
                    String result = response.body().string();
                    Log.d(TAG, String.valueOf(response.code()));
                    Log.d(TAG, result);

                    imageResult = true;
                    finalResult = scoreResult && imageResult;

                    if(finalResult && MLListener!=null){
                        MLListener.onComplete(true);
                        scoreResult = false;
                        imageResult = false;
                    }

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

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");


        Call call = uploadAPIs.uploadImage(part, description);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "upLoadImage");
                try {
//                Log.d(TAG, String.valueOf(response.code()));
                    String result = response.body().string();
//                    if(imageListener!=null){
//                        imageListener.onComplete(result);
//                    }
                    Log.d(TAG, String.valueOf(response.code()));
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

    public void uploadScore(String sequence_Id, final String score_) {
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

        Log.d(TAG, String.valueOf(request));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), request.toString());

        Call call = uploadAPIs.uploadScore(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "upload score onResponse ");
                scoreResult = true;
                finalResult = scoreResult && imageResult;
                if(finalResult && MLListener!=null){
                    MLListener.onComplete(true);
                    scoreResult = false;
                    imageResult = false;
                }
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
        void onComplete(boolean result);
    }

    public ResponseListener MLListener;

    public void setuploadMLListener(ResponseListener listener) {
        Log.d(TAG,"setMLListener");
        MLListener = listener;
    }

    public ResultListener resultListener;

    public interface ResultListener{
        void onComplete(String result);
    }
    public void setResultListener(ResultListener listener){
        Log.d(TAG,"setResultListener");
        resultListener = listener;
    }
}
