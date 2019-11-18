package com.example.myapplication;

import android.util.Log;

public class User {
    public static String sessionId;
    public static String deviceId;
    private static String TAG = "Empethics";
    public User(){

    }

    public String getSessionID(){
        return sessionId;
    }
    public String getDeviceId(){
        return  deviceId;
    }
    public void setDeviceId(String deviceID){
        deviceId = deviceID;
        Log.d(TAG,"deviceId");
        Log.d(TAG,deviceId);
    }
    public void setSessionId(String sessionID){
        sessionId = sessionID;
        Log.d(TAG,"sessionId");
        Log.d(TAG,sessionId);

    }
}
