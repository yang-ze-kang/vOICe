package com.example.lbstest;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.widget.EditText;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
/**
 * create by chentravelling@163.com
 */
public class MainActivity extends Activity implements OnGetGeoCoderResultListener {
    static MainActivity mainActivity;
    String position;
    private final static String TAG = MainActivity.class.getSimpleName();
    /**
     * 全局变量
     */
    private static final String APP_FOLDER_NAME = "lbstest";    //app在SD卡中的目录名
    boolean isFirstLoc = true;                                  //是否首次定位
    GeoCoder mSearch = null;                                    //地理编码模块
    /**
     * UI相关
     */
    private RelativeLayout popuInfoView = null;                 //点击marker后弹出的窗口
    private String mSDCardPath = null;
    /**
     * 百度地图相关
     */
    private LocationClient locationClient;                      //定位SDK核心类
    private MapView mapView;                                    //百度地图控件
    private BaiduMap baiduMap;                                  //百度地图对象
    private LatLng myLocation;                                  //当前定位信息
    private LatLng clickLocation;                               //长按地址信息
    private BDLocation currentLocation;                         //当前定位信息[最好使用这个]
    private PoiSearch poiSearch;                                //POI搜索模块
    private SuggestionSearch suggestionSearch = null;           //模糊搜索模块
    private MySensorEventListener mySensorEventListener;        //传感器
    private float lastX = 0.0f;                                 //传感器返回的方向
    private LatLng startPt;
    private LatLng endPt;
    private WalkNaviLaunchParam walkParam;
    private BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    private BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMapCustomFile(this, "custom.json");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        position = intent.getStringExtra("yang");
        //界面初始化：控件初始化
        initView();
        //初始化百度地图相关
        initBaiduMap();
        //初始化传感器
        initSensor();
        startPt = new LatLng(40.057038, 116.307899);
        endPt = new LatLng(40.057038, 116.307899);
        initOverlay();
    }
    private Marker mStartMarker;
    private Marker mEndMarker;
    public void initOverlay() {
        MarkerOptions ooA = new MarkerOptions().position(startPt).icon(bdStart)
                .zIndex(9).draggable(true);
        mStartMarker = (Marker) (baiduMap.addOverlay(ooA));
        mStartMarker.setDraggable(true);
        MarkerOptions ooB = new MarkerOptions().position(endPt).icon(bdEnd)
                .zIndex(5);
        mEndMarker = (Marker) (baiduMap.addOverlay(ooB));
        mEndMarker.setDraggable(true);
        baiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                if (marker == mStartMarker) {
                    startPt = marker.getPosition();
                } else if (marker == mEndMarker) {
                    endPt = marker.getPosition();
                }
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startPt);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endPt);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);

            }
            public void onMarkerDragStart(Marker marker) {
            }
        });
    }

    /**
     * 初始化方向传感器
     */
    private void initSensor() {
        //方向传感器监听
        mySensorEventListener = new MySensorEventListener(this);
        //增加监听：orientation listener
        mySensorEventListener.setOnOrientationListener(new MySensorEventListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                //将获取的x轴方向赋值给全局变量
                lastX = x;
            }
        });
        //开启监听
        mySensorEventListener.start();
    }
    /**
     * 初始化百度地图相关模块
     */
    private void initBaiduMap() {
        /*****************************************************
         * 地图模块
         *****************************************************/
        //百度地图map
        baiduMap = mapView.getMap();
        //增加监听:Marker click listener
        baiduMap.setOnMarkerClickListener(new OnMarkerClickListener());
        MapView.setMapCustomEnable(true);
        /*****************************************************
         * 定位模块
         *****************************************************/
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        //定位服务客户端
        locationClient = new LocationClient(this);
        //注册监听
        locationClient.registerLocationListener(new MyLocationListenner());
        //定位配置信息
        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型,国测局经纬度坐标系:gcj02;  百度墨卡托坐标系:bd09;  百度经纬度坐标系:bd09ll
        option.setCoorType("bd09ll");
        //定位请求时间间隔 1秒
        option.setScanSpan(1000);
        //设备方向
        option.setNeedDeviceDirect(true);
        //是否需要地址信息
        option.setIsNeedAddress(true);
        //是否需要地址语义化信息
        option.setIsNeedLocationDescribe(true);
        locationClient.setLocOption(option);
        //开启定位
        locationClient.start();
        MyLocationConfiguration confit = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
        baiduMap.setMyLocationConfiguration(confit);
        //增加监听：长按地图
        baiduMap.setOnMapLongClickListener(new OnMapLongClickListener());
        //增加监听：map click listener ,主要监听poi点击
        baiduMap.setOnMapClickListener(new OnMapClickListener());
        /******************************************************
         * 地理编码模块
         ******************************************************/
        //地理编码模块
        mSearch = GeoCoder.newInstance();
        //增加监听：地理编码查询结果
        mSearch.setOnGetGeoCodeResultListener(this);
        /******************************************************
         * POI搜索模块
         ******************************************************/
        //POI搜索模块
        poiSearch = PoiSearch.newInstance();
        //增加监听：POI搜索结果
        poiSearch.setOnGetPoiSearchResultListener(new PoiSearchListener());
        //模糊搜索
        suggestionSearch = SuggestionSearch.newInstance();
        //增加监听：模糊搜索查询结果
        suggestionSearch.setOnGetSuggestionResultListener(new SuggestionResultListener());
        initDirs();

    }
    private void setMapCustomFile(Context context, String fileName) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        String moduleName = null;
        try {
            inputStream = context.getAssets().open("customConfigDir/" + fileName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            moduleName = context.getFilesDir().getAbsolutePath();
            File file = new File(moduleName + "/" + fileName);
            if (file.exists()) file.delete();
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            //将自定义样式文件写入本地
            fileOutputStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//设置自定义样式文件
        MapView.setCustomMapStylePath(moduleName + "/" + fileName);
    }
    /**
     * 界面初始化
     **/
    private void initView() {
        //百度地图view
        mapView = (MapView) findViewById(R.id.bmapView);
        //去掉baidu logo
        mapView.removeViewAt(1);
    }
    /**
     * 反向搜索
     *
     * @param latLng
     */
    public void reverseSearch(LatLng latLng) {
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(latLng));
    }
    /**
     * 监听正向地理编码和反向地理编码搜索结果
     *
     * @param result
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        baiduMap.clear();
        baiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_gcoding)));
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        baiduMap.clear();
        baiduMap.addOverlay(
                new MarkerOptions()
                        .position(result.getLocation())                                     //坐标位置
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))  //图标
                        .title(result.getAddress())                                         //标题
        );
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));

        /**
         * 弹出InfoWindow，显示信息
         */
        BDLocation bdLocation = new BDLocation();
        bdLocation.setLatitude(result.getLocation().latitude);
        bdLocation.setLongitude(result.getLocation().longitude);
        bdLocation.setAddrStr(result.getAddress());
        poputInfo(bdLocation, result.getAddress());
    }
    /**
     * 弹出InfoWindow，显示信息
     */
    public void poputInfo(final BDLocation bdLocation, final String address) {
        /**
         * 获取弹窗控件
         */
        popuInfoView = (RelativeLayout) findViewById(R.id.id_marker_info);
        TextView addrNameView = (TextView) findViewById(R.id.addrName);
        if (addrNameView != null)
            addrNameView.setText(address);
        popuInfoView.setVisibility(View.VISIBLE);
        /**
         * 进入导航部分
         */
        //起点
        startPt = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //终点
        endPt = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
    }
    public void startNavigation() {
        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
        walkStartNode.setLocation(startPt);
        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
        walkEndNode.setLocation(endPt);
        walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
        walkParam.extraNaviMode(0);
        startWalkNavi();
    }
    public void showToastMsg(final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        Log.d(TAG, "startWalkNavi");
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d(TAG, "WalkNavi engineInitSuccess");
                    routePlanWithWalkParam();
                }
                @Override
                public void engineInitFail() {
                    Log.d(TAG, "WalkNavi engineInitFail");
                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "startBikeNavi Exception");
            e.printStackTrace();
        }
    }
    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(TAG, "WalkNavi onRoutePlanStart");
            }
            @Override
            public void onRoutePlanSuccess() {
                Log.d(TAG, "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WNaviGuideActivity.class);
                startActivity(intent);
            }
            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d(TAG, "WalkNavi onRoutePlanFail");
            }
        });
    }
    /**
     * 初始化SD卡，在SD卡路径下新建文件夹：App目录名，文件中包含了很多东西，比如log、cache等等
     *
     * @return
     */
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
    @Override
    protected void onStop() {
        mySensorEventListener.stop();
        super.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onResume() {
        mapView.onResume();
        baiduMap.clear();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        locationClient.stop();
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        bdEnd.recycle();
        bdStart.recycle();
        super.onDestroy();
    }
    private class PoiSearchListener implements OnGetPoiSearchResultListener {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null
                    || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                //成功在传入的搜索city中搜索到POI
                //对result进行一些应用
                //一般都是添加到地图中，然后绑定一些点击事件
                //官方Demo的处理如下：
                baiduMap.clear();
                PoiOverlay overlay = new PoiOverlay(baiduMap);
                baiduMap.setOnMarkerClickListener(overlay);
                //MyPoiOverlay extends PoiOverlay;PoiOverlay extends OverlayManager
                //看了这三个class之间的关系后瞬间明白咱自己也可以写overlay，重写OverlayManager中的一些方法就可以了
                //比如重写了点击事件，这个方法真的太好，对不同类型的图层可能有不同的点击事件，百度地图3.4.0之后就支持设置多个监听对象了，只是本人还没把这个方法彻底掌握...
                overlay.setData(poiResult); //图层数据
                overlay.addToMap();         //添加到地图中(添加的都是marker)
                overlay.zoomToSpan();       //保证能显示第一个marker
                //targetlocation =poiResult.getAllPoi().get(0).location;
                return;
            }
            if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                String strInfo = "在本市没有找到结果";
                Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_SHORT)
                        .show();
            }
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            if (poiDetailResult == null
                    || poiDetailResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            if (poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) {
                //成功在传入的搜索city中搜索到POI
                //对result进行一些应用
                //一般都是添加到地图中，然后绑定一些点击事件
                //官方Demo的处理如下：
                baiduMap.clear();
                baiduMap.addOverlay(
                        new MarkerOptions()
                                .position(poiDetailResult.location)                                     //坐标位置
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))
                                .title(poiDetailResult.getAddress())//标题

                );
                endPt = poiDetailResult.location;
                startNavigation();
                //将该POI点设置为地图中心
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(poiDetailResult.location));
                Toast.makeText(MainActivity.this, "定位成功", Toast.LENGTH_LONG).show();
                return;
            }
            if (poiDetailResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            }

        }
        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        }
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    }
    private class OnMarkerClickListener implements BaiduMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            BDLocation bdLocation = new BDLocation();
            bdLocation.setAddrStr(marker.getTitle());
            bdLocation.setLatitude(marker.getPosition().latitude);
            bdLocation.setLongitude(marker.getPosition().longitude);
            //弹出信息
            poputInfo(bdLocation, marker.getTitle());

            return false;
        }
    }
    private class SuggestionResultListener implements OnGetSuggestionResultListener {
        @Override
        public void onGetSuggestionResult(final SuggestionResult suggestionResult) {
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                return;
            }
            SuggestionResult.SuggestionInfo info = suggestionResult.getAllSuggestions().get(0);
            poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(info.uid));
        }
    }
    /**
     * 重写map poi click:监听地图中已标记的POI点击事件
     */
    private class OnMapClickListener implements BaiduMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
        }
        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            String POIName = mapPoi.getName();//POI点名称
            LatLng POIPosition = mapPoi.getPosition();//POI点坐标
            //下面就是自己随便应用了
            //根据POI点坐标反向地理编码
            reverseSearch(POIPosition);
            //添加图层显示POI点
            baiduMap.clear();
            baiduMap.addOverlay(
                    new MarkerOptions()
                            .position(POIPosition)                                     //坐标位置
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))
                            .title(POIName)                                         //标题
            );
            //将该POI点设置为地图中心
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(POIPosition));
            return true;
        }
    }
    /**
     * 重写map long click:长按地图选点,进行反地理编码,查询该点信息
     */
    private class OnMapLongClickListener implements BaiduMap.OnMapLongClickListener {
        @Override
        public void onMapLongClick(LatLng latLng) {
            clickLocation = latLng;
            reverseSearch(latLng);
        }
    }
    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }
            //Toast.makeText(VisualProcess.this, "定位结果编码："+location.getLocType(), Toast.LENGTH_LONG).show();
            if (!isFirstLoc) {
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(lastX)//该参数由传感器提供
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                baiduMap.setMyLocationData(locData);
            }
            if (isFirstLoc) {
                isFirstLoc = false;
                //city.setText(location.getCity());
                myLocation = new LatLng(location.getLatitude(),
                        location.getLongitude());
                currentLocation = location;
                startPt = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(myLocation)
                        .zoom(18.0f)
                        .build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                baiduMap.setMapStatus(mMapStatusUpdate);
            }
            if (endPt.latitude == 40.057038) {
                suggestionSearch
                        .requestSuggestion(new SuggestionSearchOption()
                                .city(currentLocation.getCity())
                                .keyword(position));
            }
        }
    }
    /**
     * 重写onRequestPermissionsResult方法
     * 获取动态权限请求的结果,再开启录音
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            // Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (OneShot.finishAll) {
            Intent intent = new Intent(this, OneShot.class);
            startActivity(intent);
            finish();
        }
    }
}


