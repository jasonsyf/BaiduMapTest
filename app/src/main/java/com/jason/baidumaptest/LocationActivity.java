package com.jason.baidumaptest;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


import java.util.List;

public class LocationActivity extends AppCompatActivity {
//    public BDLocationListener myListener = new MyLocationListener();
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // 定位相关

    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener=new MyLocationListener() {
        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            centerToMyLocation();
        }
    };
    private boolean isFirstIn = true;
    private double mLatitude;
    private double mLongitude;

    // 自定义定位图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    private MyLocationConfiguration.LocationMode mLocationMode;

    public LocationActivity() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        // 开启方向传感器
        myOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // 停止方向传感器
        myOrientationListener.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);
        initView();
        //注册监听函数
        initLocation();
    }

    private void initView() {
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        // 改变显示的比例尺
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate after = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(after);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void initLocation() {
        mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(mLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        // 初始化图标
        mIconLocation = BitmapDescriptorFactory
                .fromResource(R.mipmap.ic_launcher);
        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mCurrentX = x;
                    }
                });
    }

    // 定位到我的位置
    private void centerToMyLocation() {
        LatLng latlng = new LatLng(mLatitude, mLongitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
        mBaiduMap.animateMapStatus(msu);

    }

    /**
     * 实现定位监听器BDLocationListener，重写onReceiveLocation方法；
     *
     * @author chenyufeng
     *
     */
    private abstract class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurrentX)//
                    .accuracy(location.getRadius())//
                    .latitude(location.getLatitude())//
                    .longitude(location.getLongitude())//
                    .build();

            mBaiduMap.setMyLocationData(data);

            // 设置自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(
                    mLocationMode, true, mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            // 更新经纬度
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            if (isFirstIn) {
                LatLng latlng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn = false;

                Toast.makeText(LocationActivity.this, location.getAddrStr(),
                        Toast.LENGTH_SHORT).show();
                Log.i("TAG",
                        location.getAddrStr() + "\n" + location.getAltitude()
                                + "" + "\n" + location.getCity() + "\n"
                                + location.getCityCode() + "\n"
                                + location.getCoorType() + "\n"
                                + location.getDirection() + "\n"
                                + location.getDistrict() + "\n"
                                + location.getFloor() + "\n"
                                + location.getLatitude() + "\n"
                                + location.getLongitude() + "\n"
                                + location.getNetworkLocationType() + "\n"
                                + location.getProvince() + "\n"
                                + location.getSatelliteNumber() + "\n"
                                + location.getStreet() + "\n"
                                + location.getStreetNumber() + "\n"
                                + location.getTime() + "\n");

                // 弹出对话框显示定位信息；
                AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
                builder.setTitle("为您获得的定位信息：");
                builder.setMessage("当前位置：" + location.getAddrStr() + "\n"
                        + "城市编号：" + location.getCityCode() + "\n" + "定位时间："
                        + location.getTime() + "\n" + "当前纬度："
                        + location.getLatitude() + "\n" + "当前经度："
                        + location.getLongitude());
                builder.setPositiveButton("确定", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }// if
        }// onReceiveLocation()
    }// MyLocationListener;
}
