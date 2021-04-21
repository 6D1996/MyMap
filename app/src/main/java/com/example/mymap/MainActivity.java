package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.example.mymap.api.CarInfoReceive;
import com.example.mymap.api.CarInfoRequest;
import com.example.mymap.api.DataResult;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mapView;
    private AMap aMap;
    private Button basicmap;
    private Button rsmap;
    private Button nightmap;
    private Button navimap;

    private CarInfoReceive carInfoReceive;
    private CarInfoRequest carInfoRequest;
    private DataResult dataResult;
    String carInfoString="Initial carInfoString";

    private CheckBox mStyleCheckbox;


    private CustomMapStyleOptions mapStyleOptions = new CustomMapStyleOptions();


    // 西南坐标
    private LatLng southwestLatLng = new LatLng(43.833394, 125.164146);
    // 东北坐标
    private LatLng northeastLatLng = new LatLng(43.854280, 125.300580);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();

        aMap.addMarker(new MarkerOptions().position(getCarLocation()));


        //地图初始位置设置
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(getCarLocation()));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15.5f));

        getCarLocation();

    }


    /**
     * Get car location lat lng
     *
     * @return the lat lng
     */
    public LatLng getCarLocation(){
        carInfoString=requestCarInfo();
        carInfoReceive=JSON.parseObject(carInfoString,CarInfoReceive.class);
        Log.d(TAG, "requestCarInfo:DataResults "+carInfoReceive.getDataResults());
        dataResult=JSON.parseObject(carInfoReceive.getDataResults(),DataResult.class);
        Log.d(TAG, "requestCarInfo: "+dataResult.getPosition3d());
        String []position=dataResult.getPosition3d().split(",");
        double longitude=0, latitude=0;
        longitude= Double.parseDouble(position[0]);
        latitude= Double.parseDouble(position[1]);
        Log.d(TAG, "getCarLocation: 经纬度（"+longitude+","+latitude+")");
        LatLng latLng = new LatLng(latitude,longitude);
        CoordinateConverter converter  = new CoordinateConverter(this.getBaseContext());
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
// sourceLatLng待转换坐标点 LatLng类型
        converter.coord(latLng);
// 执行转换操作
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    /**
     * 请求车辆信息
     */
    public String requestCarInfo(){
        carInfoRequest = new CarInfoRequest();
        //序列化
        final String addressRequestJson = JSON.toJSONString(carInfoRequest);
        carInfoRequest=JSON.parseObject(addressRequestJson,CarInfoRequest.class);
        Log.d(TAG, "requestCarInfo: "+carInfoRequest.toString());
        Thread requestCarInfoThread=new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "postAddressRequest: "+addressRequestJson);
                            OkHttpClient addressClient=new OkHttpClient();
                            String hostURL="http://vehicleroadcloud.faw.cn:60443/backend/appBackend/";
                            Request addressRequest= new Request.Builder()
                                    .url(hostURL+"vehicleCondition")
                                    .post(RequestBody.create(MediaType.parse("application/json"),addressRequestJson))
                                    .build();//创造HTTP请求
                            //执行发送的指令
                            Log.d(TAG, "run: 请求json："+addressRequestJson);
                            Response addressResponse = addressClient.newCall(addressRequest).execute();
                            carInfoString=addressResponse.body().string();
                            Log.d(TAG, "run: 接受到了carInfoString"+carInfoString);

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("POST失敗", "onClick: "+e.toString());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"請求地址失败！",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
        );
        requestCarInfoThread.start();
        while (requestCarInfoThread.isAlive()){}
        return carInfoString;
    }


    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        setMapCustomStyleFile(this);
        basicmap = (Button)findViewById(R.id.basicmap);
        basicmap.setOnClickListener(this);
        rsmap = (Button)findViewById(R.id.rsmap);
        rsmap.setOnClickListener(this);
        nightmap = (Button)findViewById(R.id.nightmap);
        nightmap.setOnClickListener(this);
        navimap = (Button)findViewById(R.id.navimap);
        navimap.setOnClickListener(this);

        mStyleCheckbox = (CheckBox) findViewById(R.id.check_style);

        mStyleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mapStyleOptions != null) {
                    // 设置自定义样式
                    mapStyleOptions.setEnable(b);
//					mapStyleOptions.setStyleId("your id");
                    aMap.setCustomMapStyle(mapStyleOptions);
                }
            }
        });

    }

    private void setMapCustomStyleFile(Context context) {
        String styleName = "style.data";
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(styleName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            if(mapStyleOptions != null) {
                // 设置自定义样式
                mapStyleOptions.setStyleData(b);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.basicmap:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
                break;
            case R.id.rsmap:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
                break;
            case R.id.nightmap:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
                break;
            case R.id.navimap:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);//导航地图模式
                break;
        }
        mStyleCheckbox.setChecked(false);
    }
}