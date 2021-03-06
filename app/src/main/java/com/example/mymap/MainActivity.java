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

    // ????????????
    private LatLng carLatLng = new LatLng(43.83601111412616,125.16247997144413);
    // ????????????
    private LatLng destinyLatLng = new LatLng(43.854280, 125.300580);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// ?????????????????????

        init();

        carLatLng=getCarLocation();
        aMap.addMarker(new MarkerOptions().position(carLatLng));


//        //????????????????????????
//        aMap.moveCamera(CameraUpdateFactory.changeLatLng(getCarLocation()));
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(15.5f));

        android.graphics.Point paramPoint = new Point();
        aMap.getProjection().fromScreenLocation(paramPoint);

    }

    private void setUpMap() {
        aMap.setOnMarkerClickListener((AMap.OnMarkerClickListener) this);
        addMarkersToMap();// ??????????????????marker
        aMap.setOnMapClickListener(this);// ???amap?????????????????????????????????
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        // ?????????????????????
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
     * ??????????????????marker
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
     * ???marker???????????????????????????
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (aMap != null) {
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
        }

        boolean markerInPolygon = polygon.contains(marker.getPosition());
        if(markerInPolygon){showMyDialog(marker);}
        else {
            Toast.makeText(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }


        return true;
    }

    private void showMyDialog(Marker marker) {
        //?????????
        View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.my_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setView(myView).create();
        TextView Title = myView.findViewById(R.id.title);
        TextView Context = myView.findViewById(R.id.content);
        Title.setText("???????????????");
        Context.setText("????????????????????????????????????????????????????????????????????????\n?????????????????????????????????????????????????????????");
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
                    Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }else if("0070200".equals(callCarReply.getCode())) {
                    Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_LONG).show();
//?????????????????????
                    Intent intent=new Intent(MainActivity.this, RMTPPlayerActivity.class);
                     startActivity(intent);

//                Intent intent=new Intent(MainActivity.this, MainActivity.class);
//                startActivity(intent);
                dialog.dismiss();}
                }
                else {
                    Log.d(TAG, "onClick: ??????????????????");
                    Intent intent=new Intent(MainActivity.this, RMTPPlayerActivity.class);
                    startActivity(intent);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "????????????",Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "getCarLocation: ????????????"+longitude+","+latitude+")");
        LatLng latLng = new LatLng(latitude,longitude);
        CoordinateConverter converter  = new CoordinateConverter(this.getBaseContext());
        // CoordType.GPS ?????????????????????
        converter.from(CoordinateConverter.CoordType.GPS);
// sourceLatLng?????????????????? LatLng??????
        converter.coord(latLng);
// ??????????????????
        LatLng desLatLng = converter.convert();
/*        double[] desLatLngArr = GpsUtil.gps84_To_Gcj02(latitude, longitude);
        LatLng desLatLng = new LatLng(desLatLngArr[0],desLatLngArr[1]);*/
        return desLatLng;
    }

    /**
     * ??????????????????
     */
    public String requestCarInfo(){
        carInfoRequest = new CarInfoRequest();
        //?????????
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
                                    .build();//??????HTTP??????
                            //?????????????????????
                            Log.d(TAG, "run: ??????json???"+addressRequestJson);
                            Response addressResponse = addressClient.newCall(addressRequest).execute();
                            carInfoString=addressResponse.body().string();
                            Log.d(TAG, "run: ????????????carInfoString"+carInfoString);

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("POST??????", "onClick: "+e.toString());
                            CarInfoReceive carInfoReceive1=new CarInfoReceive();
                            DataResult dataResult1 = new DataResult();
                            carInfoReceive1.setDataResults(JSON.toJSONString(dataResult1));
                            carInfoString=JSON.toJSONString(carInfoReceive1);
                            Log.d(TAG, "run: ??????carInfoString"+carInfoString);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"?????????????????????",Toast.LENGTH_LONG).show();
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
     * ?????????AMap??????
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
                    // ?????????????????????
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
                // ?????????????????????
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
     * ??????????????????
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * ??????????????????
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * ??????????????????
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * ??????????????????
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
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ??????????????????
                break;
            case R.id.rsmap:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// ??????????????????
                break;
            case R.id.nightmap:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);//??????????????????
                break;
            case R.id.navimap:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);//??????????????????
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
        Log.d(TAG, "onMapClick: ???????????????"+latLng.toString());
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
        Log.d(TAG, "getAddress: ????????????");
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 10,
                GeocodeSearch.AMAP);// ???????????????????????????Latlng????????????????????????????????????????????????????????????????????????????????????GPS???????????????
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        geocoderSearch.getFromLocationAsyn(query);// ?????????????????????????????????

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "??????";
                Log.d(TAG, "onRegeocodeSearched: "+addressName);
            } else {
                Log.d(TAG, "onRegeocodeSearched: ????????????");
            }
        } else {
            Log.d(TAG, "onRegeocodeSearched: ????????????");
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }
}