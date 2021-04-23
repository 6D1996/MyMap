package com.example.mymap.api;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class CallCarRequest {

    private String userId="6D";
    private String vin="0423";
    private int parkingType=3;
    private String pathData;
    private String autoParkingReplyString="Initial";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public int getParkingType() {
        return parkingType;
    }

    public void setParkingType(int parkingType) {
        this.parkingType = parkingType;
    }

    public String getPathData() {
        return pathData;
    }

    public void setPathData(String pathData) {
        this.pathData = pathData;
    }

    public String callCarToAim(String pathData){
        this.setPathData(pathData);

        final String autoParkingRequestJson= JSON.toJSONString(this);//序列化
        Thread requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "postVideoRequest: "+autoParkingRequestJson);
                    OkHttpClient autoParkClient=new OkHttpClient();
                    Log.d(TAG, "postVideoRequest: new OkHttpClient()");
                    Request videoRequest= new Request.Builder()
                            .url("http://vehicleroadcloud.faw.cn:60443/backend/appBackend/callForCar")
                            .post(RequestBody.create(MediaType.parse("application/json"),autoParkingRequestJson))
                            .build();//创造HTTP请求
                    //执行发送的指令
                    Log.d(TAG, "postVideoRequest: 转换成功");
                    Response autoParkResponse = autoParkClient.newCall(videoRequest).execute();
                    String reply=autoParkResponse.body().string();
                    autoParkingReplyString=reply;
                    Log.d(TAG, "postVideoRequest: 返回结果"+autoParkingReplyString);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("POST失敗", "onClick: "+e.toString());
                }
            }
        });

        requestThread.start();
        while (requestThread.isAlive()){
            if("Initial".equals(autoParkingReplyString)){}
            else {
                Log.d(TAG, "AutoParkMethod: "+autoParkingReplyString);
            }
        }

        Log.d(TAG, "返回结果: "+autoParkingReplyString);
        return autoParkingReplyString;
    }


}
