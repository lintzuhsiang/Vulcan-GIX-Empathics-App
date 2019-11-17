package com.example.myapplication;

public class User {
    public static String sessionId;
    public static String deviceId;

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
    }
    public void setSessionId(String sessionID){
        sessionId = sessionID;
    }
}
