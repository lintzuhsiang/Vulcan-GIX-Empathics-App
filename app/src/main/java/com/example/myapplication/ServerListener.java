package com.example.myapplication;

import android.util.Log;

public class ServerListener {
    private static String TAG="Empethics/ServerListener";

    public static ResponseListener scoreListener;
    public static ResponseListener imageListener;
    public static ResponseListener finalListener;

    public ServerListener(){

    }
    public ServerListener(ResponseListener scoreListener,ResponseListener imageListener){
        ServerListener.scoreListener = scoreListener;
        ServerListener.imageListener = imageListener;
//        this.finalListener = finalListener;
    }

    public interface ResponseListener {
        void onComplete(String result);
    }

    public void setFinalResponseListener(ResponseListener listener) {
        Log.d(TAG,"setFinalResponseListener");
        finalListener = listener;
    }

    public void setScoreResponseListener(ResponseListener listener) {
        Log.d(TAG, String.valueOf("setScoreResponseListener is" +ServerListener.scoreListener));
        scoreListener = listener;
    }
    public void setImageResponseListener(ResponseListener listener) {
        Log.d(TAG,"setImageResponseListener");
        ServerListener.imageListener = listener;
    }

    public ResponseListener getScoreListener(){
        Log.d(TAG, String.valueOf(ServerListener.scoreListener));
        return scoreListener;
    }
    public ResponseListener getImageListener(){
        Log.d(TAG, String.valueOf(ServerListener.imageListener));
        return imageListener;
    }
}
