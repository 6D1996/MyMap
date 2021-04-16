package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;

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

    private CheckBox mStyleCheckbox;


    private CustomMapStyleOptions mapStyleOptions = new CustomMapStyleOptions();


    // 西南坐标
    private LatLng southwestLatLng = new LatLng(43.854272, 125.300576);
    // 东北坐标
    private LatLng northeastLatLng = new LatLng(43.854280, 125.300580);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();

        aMap.addMarker(new MarkerOptions().position(southwestLatLng));
        aMap.addMarker(new MarkerOptions().position(northeastLatLng));

        aMap.moveCamera(CameraUpdateFactory.zoomTo(8f));

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