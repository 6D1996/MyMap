 package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.mymap.api.CallCarReply;
import com.example.mymap.api.CallCarRequest;
import com.example.mymap.api.CarInfoReceive;
import com.example.mymap.api.CarInfoRequest;
import com.example.mymap.api.DataResult;
import com.example.mymap.api.Destination;
import com.ip.ipsearch.Util.GpsUtil;

import org.jetbrains.annotations.NotNull;

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
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, AMap.OnMarkerClickListener, AMap.OnMapClickListener, GeocodeSearch.OnGeocodeSearchListener {

    private MapView mapView;
    private AMap aMap;
    private Button basicmap;
    private Button rsmap;
    private Button nightmap;
    private Button navimap;
    private MarkerOptions markerOption;
    private GeocodeSearch geocoderSearch;
    private Marker marker;
    private Polygon polygon;

    private CarInfoReceive carInfoReceive;
    private CarInfoRequest carInfoRequest;
    private DataResult dataResult;
    String carInfoString="Initial carInfoString";

    private CheckBox mStyleCheckbox;

    private CallCarRequest callCarRequest;
    private CallCarReply callCarReply;
    private Destination destination=new Destination();


    private CustomMapStyleOptions mapStyleOptions = new CustomMapStyleOptions();

    // 车辆坐标
    private LatLng carLatLng = new LatLng(43.83601111412616,125.16247997144413);
    // 目标坐标
    private LatLng destinyLatLng = new LatLng(43.854280, 125.300580);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();

        carLatLng=getCarLocation();
        aMap.addMarker(new MarkerOptions().position(carLatLng));


//        //地图初始位置设置
//        aMap.moveCamera(CameraUpdateFactory.changeLatLng(getCarLocation()));
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(15.5f));

        android.graphics.Point paramPoint = new Point();
        aMap.getProjection().fromScreenLocation(paramPoint);

    }

    private void setUpMap() {
        aMap.setOnMarkerClickListener((AMap.OnMarkerClickListener) this);
        addMarkersToMap();// 往地图上添加marker
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        // 绘制一个长方形
        PolygonOptions pOption = new PolygonOptions();
        pOption.add(new LatLng(43.83604303740284,125.1615908191709));
        pOption.add(new LatLng(43.83589309459136,125.1616444633503));
        pOption.add(new LatLng(43.83588438822299,125.16169140200725));
        pOption.add(new LatLng(43.835763466308755,125.16175175170903));
        pOption.add(new LatLng(43.83607399329118,125.16326451756757));
        pOption.add(new LatLng(43.83622103354154,125.16319343902988));

        polygon = aMap.addPolygon(pOption.strokeWidth(4)
                .strokeColor(Color.argb(50, 54, 58, 90))
                .fillColor(Color.argb(50,  155,  0,  255)));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.83598692981427,125.1624303505782), 18.6f));
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        if (marker != null) {
            marker.remove();
        }
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(destinyLatLng)
                .draggable(true);
        marker=aMap.addMarker(markerOption);

    }

    /**
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (aMap != null) {
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
        }

        boolean markerInPolygon = polygon.contains(marker.getPosition());
        if(markerInPolygon){showMyDialog(marker);}
        else {
            Toast.makeText(MainActivity.this, "叫车目的地不在可用区域内", Toast.LENGTH_SHORT).show();
        }


        return true;
    }

    private void showMyDialog(Marker marker) {
        //对话框
        View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.my_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setView(myView).create();
        TextView Title = myView.findViewById(R.id.title);
        TextView Context = myView.findViewById(R.id.content);
        Title.setText("确认叫车？");
        Context.setText("自动叫车功能是由云端计算机控制车辆自动到达目的地\n该功能有一定风险，一切后果将由车主承担");
        ImageButton Confirm = myView.findViewById(R.id.confirm);
        ImageButton cancel = myView.findViewById(R.id.cancel);
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callCarRequest= new CallCarRequest();
                double[] desLatLngArr = GpsUtil.gps84_To_Gcj02(marker.getPosition().latitude, marker.getPosition().longitude);
                LatLng marker_wgs84 = new LatLng(desLatLngArr[0],desLatLngArr[1]);
                destination.setLatitude(desLatLngArr[0]);
                destination.setLongitude(desLatLngArr[1]);
                if(carLatLng.longitude<marker.getPosition().latitude){
                    destination.setHeading(90);
                }else {
                    destination.setHeading(-90);
                }
                String callCarString=callCarRequest.callCarToAim(JSON.toJSONString(destination));
                if(callCarString.startsWith("{")){
                callCarReply=JSON.parseObject(callCarString,CallCarReply.class);
                if (callCarReply.getStatus()==500) {
                    Toast.makeText(MainActivity.this, "叫车失败", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }else if("0070200".equals(callCarReply.getCode())) {
                    Toast.makeText(MainActivity.this, "叫车开始", Toast.LENGTH_LONG).show();
//进入播放器界面
                    Intent intent=new Intent(MainActivity.this, RMTPPlayerActivity.class);
                     startActivity(intent);

//                Intent intent=new Intent(MainActivity.this, MainActivity.class);
//                startActivity(intent);
                dialog.dismiss();}
                }
                else {
                    Log.d(TAG, "onClick: 请求叫车失败");
                    Intent intent=new Intent(MainActivity.this, RMTPPlayerActivity.class);
                    startActivity(intent);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "取消成功",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(1000,650);
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
/*        double[] desLatLngArr = GpsUtil.gps84_To_Gcj02(latitude, longitude);
        LatLng desLatLng = new LatLng(desLatLngArr[0],desLatLngArr[1]);*/
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
                            CarInfoReceive carInfoReceive1=new CarInfoReceive();
                            DataResult dataResult1 = new DataResult();
                            carInfoReceive1.setDataResults(JSON.toJSONString(dataResult1));
                            carInfoString=JSON.toJSONString(carInfoReceive1);
                            Log.d(TAG, "run: 失败carInfoString"+carInfoString);
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
            setUpMap();
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
    protected void onSaveInstanceState(@NotNull Bundle outState) {
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
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        mStyleCheckbox.setChecked(false);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }
        Log.d(TAG, "onMapClick: 打了一個點"+latLng.toString());
        LatLonPoint latLonPoint= new LatLonPoint(latLng.latitude, latLng.longitude);
        getAddress(latLonPoint);
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(latLng)
                .draggable(true);
        aMap.addMarker(markerOption);
    }

    private void getAddress(final LatLonPoint latLonPoint) {
//        showDialog();
        Log.d(TAG, "getAddress: 獲取位置");
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 10,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
                Log.d(TAG, "onRegeocodeSearched: "+addressName);
            } else {
                Log.d(TAG, "onRegeocodeSearched: 搜索失敗");
            }
        } else {
            Log.d(TAG, "onRegeocodeSearched: 搜索失敗");
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }
}