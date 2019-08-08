package com.example.test3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.JsonParser;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class naviActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TMapGpsManager.onLocationChangedCallback {

    private static final double DEGLEN = 110.25;
    String url = "https://api2.sktelecom.com/tmap/routes/pedestrian?version=1&appkey=<<tmap_yourkey>>";
    private Toolbar toolbar;

    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private TMapMarkerItem CurrentMarker;
    private TMapData tmapdata = null;

    private static String mApiKey = "<<tmap_yourkey>>"; //api key

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();
    private ArrayList<TMapPoint> statePoint = new ArrayList<TMapPoint>();
    ArrayList<DB.CCTV> finalList = new ArrayList<>();
    ArrayList<TMapPoint> finalList2 = new ArrayList<>();

    ArrayList<TMapPoint> realfinalList = new ArrayList<>();
    ArrayList<DB.CCTV>  inrangeList = new ArrayList<>();

    int statefind;
    TextView dis_time; // 거리계산 보여주기 위한 변수
    private double distances;  // 거리
    private String getDistance;

    private String address;
    private Double lat = null;
    private Double lon = null;

    private Button cctv_view;
    private Button searchList_button;

    //타이머 변수
    CountDownTimer countDownTimer;
    private int countTimer;
    private boolean isClick = true;

    //현재위치 변수
    private String current_name;
    private double current_lat;
    private double current_lon;

    //검색창 구현을 위한 변수
    String TAG = "placeautocomplete"; //자동검색
    TextView txtView; //자동검색
    public TMapPoint current_point;
    public TMapPoint start_point;
    public TMapPoint end_point;
    public TMapPoint end_point2;
    public TMapPoint list_start_point;
    public TMapPoint list_end_point;
    private SQLiteDatabase mDb2;


    private String list_startName;
    private String list_endName;
    private Double list_startLat;
    private Double list_startLon;
    private Double list_endLat;
    private Double list_endLon;

    boolean search_state1 = false;  //출발위치로 검색
    boolean search_state2 = false;  //도착위치로 검색
    boolean search_state3 = true;   //현재위치 받은상태
    boolean search_state4 = false;  //도착위치 받은상태

    String currentAddress = "현재 위치 확인 불가";
    String nowAddress= "현재 위치 확인 불가";
    Camera mCamera; //카메라 변수
    boolean isOnFlash = false; //불빛이 꺼져 있다는 뜻
    boolean isOnAlarm = false;

    final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    private SQLiteDatabase mDb;

    private String start_name;
    private String end_name;
    private String c_name;
    private String e_name;
    private int smsCount=0;

    @Override
    public void onLocationChange(Location location) {
        Log.d("현재 값 : ", address + " " + location.getLatitude() + " " + location.getLongitude());
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_main);

        //도착지 받앗을 시
        Intent intent = getIntent();
        double g_lat = (Double) intent.getDoubleExtra("lat", 0);
        double g_lon = (Double) intent.getDoubleExtra("lon", 0);
        current_lat = (Double) intent.getDoubleExtra("now_lat",0);
        current_lon = (Double) intent.getDoubleExtra("now_lon",0);
        end_point2 = new TMapPoint(g_lat, g_lon);

        DictionaryDbHelper dbHelper = new DictionaryDbHelper(this);
        mDb = dbHelper.getReadableDatabase();
        Search_DictionaryDbHelper dbHelper2 = new Search_DictionaryDbHelper(this);
        mDb2 = dbHelper2.getWritableDatabase();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mContext = this;

        CoordinatorLayout linearLayout = (CoordinatorLayout) findViewById(R.id.navi_mapview);
        tmapdata = new TMapData();
        tmapview = new TMapView(this);

        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApiKey);

        //addPoint();
        //showMarkerPoint();

        /* 줌레벨 */
        tmapview.setZoomLevel(16);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(naviActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
        }
        tmapgps.OpenGps();


        //현재위치 네임

        tmapview.setIconVisibility(true);
        /*  화면중심을 단말의 현재위치로 이동 */
        tmapview.setTrackingMode(true);
        /* 현재 보는 방향 */
        tmapview.setCompassMode(true);
        tmapview.setSightVisible(true);


        cctv_view = findViewById(R.id.cctv_view);
        searchList_button = findViewById(R.id.searchList_button);
        dis_time = (TextView)findViewById(R.id.distime);


        com.getbase.floatingactionbutton.FloatingActionButton action_policeView = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.police_button);
        action_policeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<DBPOLICE.POLICE> polices = new ArrayList<>();
                DBPOLICE.POLICE police = new DBPOLICE.POLICE();
                polices = DBPOLICE.setDB().searchPolice(current_lat-4,current_lat+4,current_lon-4,current_lon+4);
                if(polices.size() > 0) {
                    for (int j = 0; j < polices.size(); j++) {
                        police = polices.get(j);
                        policeMarker(tmapview, police.getAddr(), police.getManageOffice(), police.getTellNum(), police.getLatitude(), police.getLongitude());
                    }
                }
                else
                    Snackbar.make(v, "근처에 등록된 경찰서가 없습니다.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }
        });


        //floating 버튼
        com.getbase.floatingactionbutton.FloatingActionButton action_light = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_light);
        action_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera = Camera.open(); //카메라 기반 객체 생성 및 카메라 접근
                if (isOnFlash == false) {
                    Snackbar.make(view, "라이트 ON", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //카메라의 상태를 갖고있는 파라미터 변수 생성
                    Camera.Parameters mCameraParameter = mCamera.getParameters();
                    //파라미터 변수를 이용해 조작값 지정(플래시)
                    mCameraParameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mCameraParameter);
                    mCamera.startPreview();
                    isOnFlash = true;
                } else {
                    Snackbar.make(view, "라이트 OFF", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Camera.Parameters mCameraParameter = mCamera.getParameters();
                    mCameraParameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(mCameraParameter);
                    mCamera.startPreview();

                    isOnFlash = false;
                }
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton action_message = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_message);
        action_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "문자", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String nowAddress = "현재 위치 확인 불가";
                List<Address> address = null;
                try {

                    if (geocoder != null) {
                        address = geocoder.getFromLocation(current_lat, current_lon, 1); //주소변환
                        if (address != null && address.size() > 0) { // 주소 글짜르기
                            String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                            nowAddress = currentLocationAddress;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String sms = "위급 상황입니다. 현재위치는 " + nowAddress + " 입니다.";// 현재 위도 경도를 주소로 변환
                String url = "http://map.naver.com/?dlevel=8&lat="+current_lat+"&lng="+current_lon;
                String selectQuery = "SELECT * FROM " + DictionaryContract.DictionaryEntry.TABLE_NAME;
                Cursor cursor2 = mDb.rawQuery(selectQuery, null);
                while (cursor2.moveToNext()) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(cursor2.getString(2), null, sms, null, null);
                        smsManager.sendTextMessage(cursor2.getString(2), null, url, null, null);
                        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton action_alram = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_alram);
        action_alram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "알람", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                MediaPlayer m = MediaPlayer.create(mContext , R.raw.siren );

                if(m.isPlaying()){
                    // 재생중이면 실행될 작업 (정지)
                    m.stop();
                    try {
                        m.prepare();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    m.seekTo(0);
                    isOnAlarm = false;
//                    button.setText(R.string.start); //버튼

                }else{
                    m.start();
                    isOnAlarm = true;

//                    button.setText(R.string.stop); //버튼
                }
//                if(m.isPlaying()){
//     button.setText("STOP");    //플로팅버튼?
//                    m.pause();
//                }
//                else
//                {
//
//                    m.seekTo(0); // 맨 처음 부터 재생하도록 이동하는 것입니다.
//     button.setText("PLAY");            //////////플로팅버튼?
//                    m.start();
//                }
//
//            }
            };
        });




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //자동검색
        Places.initialize(getApplicationContext(), "google_your_apiKey");
        PlacesClient placesClient = Places.createClient(this);

        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment.a.setTextSize(14);
        // ((View)findViewById(R.id.places_autocomplete_search_button)).setVisibility(View.GONE);
        autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        autocompleteFragment.setHint("출발지를 입력하시오");


        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        // Specify the types of place data to return.
        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment2.a.setTextSize(14);
        autocompleteFragment2.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        autocompleteFragment2.setHint("도착지를 입력하시오");

        current_point = new TMapPoint(current_lat, current_lon);
        String settext1 = currentSetting(current_lat, current_lon, "현위치 : " + currentAddress);
        autocompleteFragment.setText(settext1);

        if(g_lat == 0) {
            Bundle extras = getIntent().getExtras();
            if(extras != null)
            {
                if(extras.getInt("state")==1)
                {
                    list_startName = extras.getString("start_name");
                    list_endName = extras.getString("end_name");
                    list_startLat = extras.getDouble("start_lat");
                    list_startLon = extras.getDouble("start_lon");
                    list_endLat = extras.getDouble("end_lat");
                    list_endLon = extras.getDouble("end_lon");
                    autocompleteFragment.setText(list_startName);
                    autocompleteFragment2.setText(list_endName);
                    list_start_point = new TMapPoint(list_startLat, list_startLon);
                    list_end_point = new TMapPoint(list_endLat, list_endLon);
                    find_basic_step(list_startLat,list_startLon,list_endLat,list_endLon);
                    findCctvView(list_startLat,list_startLon,list_endLat,list_endLon);
                    tmapview.setCenterPoint(list_startLon, list_startLat, true);
                    statefind = 6;
                }
            }
        }

        if (g_lat != 0 && g_lon != 0) {
            //nowAddress 설정
            search_state4 = true;
            String settext2 = currentSetting(g_lat, g_lon, nowAddress);
            end_point2 = new TMapPoint(g_lat, g_lon);
            autocompleteFragment2.setText(settext2);
            if(search_state3 == true && search_state4 == true) {
                find_basic_step(current_lat, current_lon, g_lat, g_lon);
                findCctvView(current_lat, current_lon, g_lat, g_lon);
                statefind = 1;
            }
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //선택한 장소의 위도와 경도의 값을 end라는 변수에 넣음
                LatLng start = place.getLatLng();
                start_name = place.getName();
                start_point = new TMapPoint(start.latitude, start.longitude);
                showMarker(start_point);

                tmapview.setCenterPoint(start.longitude, start.latitude, true);
                Toast.makeText(naviActivity.this, "출발지 검색 완료", Toast.LENGTH_LONG).show();
                search_state1 = true;
                search_state3 =false;
                if(search_state1 == true && search_state4==true && search_state3 == false) {
                    find_basic_step(start_point.getLatitude(), start_point.getLongitude(), end_point2.getLatitude(), end_point2.getLongitude());
                    findCctvView(start_point.getLatitude(), start_point.getLongitude(), end_point2.getLatitude(), end_point2.getLongitude());
                    statefind = 2;
                }
                if (search_state1 == true && search_state2 == true && search_state3 == false) {
                    addNewData(start_name, end_name, Double.toString(start_point.getLatitude()), Double.toString(start_point.getLongitude()), Double.toString(end_point.getLatitude()), Double.toString(end_point.getLongitude()));
                    find_basic_step(start_point.getLatitude(), start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    findCctvView(start_point.getLatitude(), start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    statefind = 3;
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //선택한 장소의 위도와 경도의 값을 end라는 변수에 넣음

                LatLng end = place.getLatLng();
                end_name = place.getName();
                end_point = new TMapPoint(end.latitude, end.longitude);

                showMarker(end_point);
                tmapview.setCenterPoint(end.longitude, end.latitude, true);
                Toast.makeText(naviActivity.this, "도착지 검색 완료", Toast.LENGTH_LONG).show();
                search_state2 = true;
                search_state4 = false;
                if (search_state3 == true && search_state2 && search_state4==false) {
                    find_basic_step(current_point.getLatitude(), current_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    findCctvView(current_point.getLatitude(), current_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    statefind = 4;
                }
                if (search_state1 == true && search_state2 == true && search_state4==false) {
                    addNewData(start_name, end_name, Double.toString(start_point.getLatitude()), Double.toString(start_point.getLongitude()), Double.toString(end_point.getLatitude()), Double.toString(end_point.getLongitude()));
                    find_basic_step(start_point.getLatitude(), start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    findCctvView(start_point.getLatitude(), start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                    statefind = 5;
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        cctv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statePoint.clear();
                finalList.clear();
                realfinalList.clear();
                inrangeList.clear();
                switch(statefind){
                    case 1:
                        find_basic_step2(current_lat, current_lon, end_point2.getLatitude(), end_point2.getLongitude());
                        findCctvView2(current_lat, current_lon, end_point2.getLatitude(), end_point2.getLongitude());
                        break;
                    case 2:
                        find_basic_step2(start_point.getLatitude(),start_point.getLongitude(), end_point2.getLatitude(), end_point2.getLongitude());
                        findCctvView2(start_point.getLatitude(),start_point.getLongitude(), end_point2.getLatitude(), end_point2.getLongitude());
                        break;
                    case 3:
                        find_basic_step2(start_point.getLatitude(),start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                        findCctvView2(start_point.getLatitude(),start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                        break;
                    case 4:
                        find_basic_step2(current_lat, current_lon, end_point.getLatitude(), end_point.getLongitude());
                        findCctvView2(current_lat, current_lon, end_point.getLatitude(), end_point.getLongitude());
                        break;
                    case 5:
                        find_basic_step2(start_point.getLatitude(),start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                        findCctvView2(start_point.getLatitude(),start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
                        break;
                    case 6:
                        find_basic_step2(list_startLat, list_startLon, list_endLat, list_endLon);
                        findCctvView2(list_startLat, list_startLon, list_endLat, list_endLon);
                        break;

                }
//                find_basic_step2(start_point.getLatitude(),start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
//                findCctvView2(start_point.getLatitude(), start_point.getLongitude(), end_point.getLatitude(), end_point.getLongitude());
            }
        });

        searchList_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent listintent = new Intent(getApplicationContext(), SearchList_Activity.class);
                startActivity(listintent);
            }
        });
    }



    private void policeMarker(TMapView tmapView, String addr, String manageOffice, String tellNum, Double latitude, Double longitude) {
        String name = addr;
        String tel = "";
        StringTokenizer st = new StringTokenizer(tellNum);
        while(st.hasMoreTokens()) {
            String temp = st.nextToken();
            if(temp.equals("-"))
                continue;
            tel = tel + temp;
        }

        TMapPoint tpoint = new TMapPoint(latitude, longitude);
        TMapMarkerItem item = new TMapMarkerItem();

        item.setTMapPoint(tpoint);
        item.setName(name);
        item.setVisible(TMapMarkerItem.VISIBLE);

        item.setCanShowCallout(true);
        item.setCalloutTitle(name);
        item.setCalloutSubTitle(tellNum);

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.poi_here);
        item.setCalloutRightButtonImage(right_bitmap);

        final String finalTel = tel;
        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(naviActivity.this);
                alert.setTitle("Police");
                alert.setMessage("전화번호 : " + finalTel);
                alert.setPositiveButton("전화걸기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + finalTel));
                        startActivity(intent);
                    }
                });
                alert.show();
            }
        });

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.policemarker);
        item.setIcon(current_flag);

        item.setPosition(0.5f, 0.5f);
        tmapView.addMarkerItem(name, item);
    }


    private String currentSetting(double g_lat, double g_lon, String g_Address) {
        List<Address> address = null;
        String tempAddress = "";
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(g_lat, g_lon, 1);
                if (address != null && address.size() > 0) {
                    String currentLocationAddress = address.get(0).getAddressLine(0);
                    StringTokenizer st = new StringTokenizer(currentLocationAddress);
                    while (st.hasMoreTokens()) {
                        String temp = st.nextToken();
                        if (temp.equals("대한민국"))
                            continue;
                        tempAddress = tempAddress + temp;
                        tempAddress = tempAddress + " ";
                        g_Address = tempAddress;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return g_Address;
    }
    // <-------------------------Oncreate 끝------------------------> //


    // cctv 마커.
    public void cctvMarker(TMapView tmapView, final String address, final String management, final String tel, double lat, double lon) {
        String name = address;
        TMapPoint tpoint = new TMapPoint(lat, lon);
        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
//        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        tItem.setCalloutSubTitle(Double.toString(lat) + "&&" + Double.toString(lon));

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.poi_here);
        tItem.setCalloutRightButtonImage(right_bitmap);


        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                AlertDialog.Builder alert = new AlertDialog.Builder(naviActivity.this);
                alert.setTitle("CCTV");
                alert.setMessage("주소 : " + address + "\n관리기관명 : " + management + "\n관리기관 전화번호 : " + tel);
                alert.setPositiveButton("뒤로가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        });

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.locationf);
        tItem.setIcon(current_flag);

        tItem.setPosition(0.5f, 0.5f);
        tmapView.addMarkerItem(name, tItem);
    }


    ///////////////////////// temp메소드 지울것 ////////////////////////////////
    public void cctvMarker2(TMapView tmapView, final String address, final String management, final String tel, double lat, double lon) {
        String name = address;
        TMapPoint tpoint = new TMapPoint(lat, lon);
        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
//        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        tItem.setCalloutSubTitle(Double.toString(lat) + "&&" + Double.toString(lon));

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.poi_here);
        tItem.setCalloutRightButtonImage(right_bitmap);

        //tItem.setAutoCalloutVisible(true);  // 풍선뷰 자동으로 활성화

        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                AlertDialog.Builder alert = new AlertDialog.Builder(naviActivity.this);
                alert.setTitle("CCTV");
                alert.setMessage("주소 : " + address + "\n관리기관명 : " + management + "\n관리기관 전화번호 : " + tel);
                alert.setPositiveButton("뒤로가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        });

        if(overlat(finalList,lat,lon) || overlat2(inrangeList,lat,lon))
        {
            Bitmap current_flag2 = BitmapFactory.decodeResource(getResources(), R.drawable.cctvmarker);
            tItem.setIcon(current_flag2);
        }
        else {
            Bitmap current_flag1 = BitmapFactory.decodeResource(getResources(), R.drawable.locationf);
            tItem.setIcon(current_flag1);
        }

        tItem.setPosition(0.5f, 0.5f);
        tmapView.addMarkerItem(name, tItem);
    }

    public boolean overlat(ArrayList<DB.CCTV> finalList, double lat, double lon) {
        for(DB.CCTV c : finalList) {
            if(c.getLongitude() == lon && c.getLatitude() == lat)
            {
                return true;
            }
        }
        return false;
    }

    private boolean overlat2(ArrayList<DB.CCTV> inrangeList, double lat, double lon) {
        for(DB.CCTV c : inrangeList) {
            if(c.getLongitude() == lon && c.getLatitude() == lat)
            {
                return true;
            }
        }
        return false;
    }

    public void showMarker(TMapPoint point) {
        TMapMarkerItem marker_item = new TMapMarkerItem();
        marker_item.setTMapPoint(point);
        marker_item.setName("검색값");
        marker_item.setVisible((TMapMarkerItem.VISIBLE));
        tmapview.addMarkerItem("search_item", marker_item);
    }


    private void find_basic_step(double current_lat, double current_lon, double g_lat, double g_lon) {
        final TMapData tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, new TMapPoint(current_lat, current_lon), new TMapPoint(g_lat, g_lon), new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                tMapPolyLine.setLineWidth(10);
                tmapview.addTMapPath(tMapPolyLine);
                double distance = tMapPolyLine.getDistance();
                double time = distance;
                double times = time / 60;
                distances = distance / 1000;
                getDistance = String.format("%,.2f", distances);
                dis_time.setText(" : " + (int) times + " 분 / " + getDistance + "km ");
            }
        });
    }

    public void findCctvView(double start_lat, double start_lon, double end_lat, double end_lon) {
        ArrayList<DB.CCTV> cctvs = new ArrayList<>();
        DB.CCTV cctv = new DB.CCTV();
        tmapview.removeAllMarkerItem();
        if (start_lat < end_lat) {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
            }
        } else {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.0005, start_lat + 0.0005, start_lon - 0.0005, end_lon + 0.0005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
            }
        }

        for (int i = 0; i < cctvs.size(); i++) {
            cctv = cctvs.get(i);
            cctvMarker(tmapview, cctv.getAddr(), cctv.getManageOffice(), cctv.getTellNum(), cctv.getLatitude(), cctv.getLongitude());
        }
    }

    /////////////temp 메소드 지울것!!///////////////////////
    public void find_basic_step2(double start_lat, double start_lon, double end_lat, double end_lon) {
        ArrayList<DB.CCTV> cctvs = new ArrayList<>();
        DB.CCTV cctv = new DB.CCTV();
        tmapview.removeAllMarkerItem();
        if (start_lat < end_lat) {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.0005, end_lat + 0.0005, start_lon - 0.0005, end_lon + 0.0005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.0005, end_lat + 0.0005, start_lon - 0.0005, end_lon + 0.0005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.0005, end_lat + 0.0005, end_lon - 0.0005, start_lon + 0.0005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.0005, end_lat + 0.0005, end_lon - 0.0005, start_lon + 0.0005);
            }
        } else {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.0005, start_lat + 0.0005, start_lon - 0.0005, end_lon + 0.0005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.0005, start_lat + 0.0005, start_lon - 0.0005, end_lon + 0.0005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.0005, start_lat + 0.0005, end_lon - 0.0005, start_lon + 0.0005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.0005, start_lat + 0.0005, end_lon - 0.0005, start_lon + 0.0005);
            }
        }

        cctvs.add(new DB.CCTV(null, null, null, end_lat, end_lon));
        for(DB.CCTV c : cctvs) {
            double d_heuristic = distance(c.getLatitude(), c.getLongitude(), end_lat, end_lon);
            c.setD_heuristic(d_heuristic * 1000); //휴리스틱 값 셋
            Log.d("좌표 : ", Double.toString(c.getLatitude()) + "," + Double.toString(c.getLongitude()));
            Log.d("휴리스틱 : ", Double.toString(c.getD_heuristic()));
            Log.d("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ", "ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        }

        int count=0;

        TMapPoint setting_start = new TMapPoint(start_lat, start_lon);
        TMapPoint setting_end = new TMapPoint(end_lat, end_lon);

        while(true) {
            if(count==10)
                break;

            final TMapPoint[] tmp_start = {setting_start};
            final TMapPoint[] tmp_end = {end_point};

            final ArrayList<DB.CCTV> sortCctvs = cctvs;

            //출발지(tmp_start)에서 가장 가까운 포인터순으로 정렬
            Collections.sort(cctvs, new Comparator<DB.CCTV>() {
                @Override
                public int compare(DB.CCTV c1, DB.CCTV c2) {
                    if ((distance(c2.getLatitude(), c2.getLongitude(), tmp_start[0].getLatitude(), tmp_start[0].getLongitude())) > (distance(c1.getLatitude(), c1.getLongitude(), tmp_start[0].getLatitude(), tmp_start[0].getLongitude())))
                        return -1;
                    else if ((distance(c2.getLatitude(), c2.getLongitude(), tmp_start[0].getLatitude(), tmp_start[0].getLongitude())) < (distance(c1.getLatitude(), c1.getLongitude(), tmp_start[0].getLatitude(), tmp_start[0].getLongitude())))
                        return 1;
                    return 0;
                }
            });

            ArrayList<DB.CCTV> sortList = new ArrayList<>();
            int a = -1;
            for (; ; ) {
                a++;
                if (statePoint.size() == 0 || addchecking(cctvs.get(a).getLatitude(), statePoint))
                    sortList.add(cctvs.get(a));
                if (sortList.size() == 3)
                    break;
            }

            for (int i = 0; i < 3; i++) {
                ContentValues values = new ContentValues();
                values.put("startX", tmp_start[0].getLongitude());
                values.put("startY", tmp_start[0].getLatitude());
                values.put("endX", sortList.get(i).getLongitude());
                values.put("endY", sortList.get(i).getLatitude());
                values.put("startName", "%eb%ac%b8");
                values.put("endName", "%eb%ac%b8");
                A th1 = new A(url, values, sortList.get(i));
                try {
                    th1.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                th1.start();
                try {
                    th1.join();    // th1의 작업이 끝날 때까지 기다린다.
                } catch (InterruptedException e) {
                }
                Log.d("count : ", Integer.toString(i));
            }

            //출발지(tmp_start)에서 가장 가까운 포인터순으로 정렬
            Collections.sort(sortList, new Comparator<DB.CCTV>() {
                @Override
                public int compare(DB.CCTV c1, DB.CCTV c2) {
                    if (c2.getMap_distance() + c2.getD_heuristic() > c1.getMap_distance() + c1.getD_heuristic())
                        return -1;
                    else if (c2.getMap_distance() + c2.getD_heuristic() < c1.getMap_distance() + c1.getD_heuristic())
                        return 1;
                    return 0;
                }
            });

            int k = -1;

            Log.d("선택된 마커", Double.toString(sortList.get(0).getLatitude()));
            if(sortList.get(0).getLatitude() == end_lat && sortList.get(0).getLongitude()==end_lon)
                break;
            setting_start = new TMapPoint(sortList.get(0).getLatitude(), sortList.get(0).getLongitude());
            statePoint.add(new TMapPoint(sortList.get(0).getLatitude(), sortList.get(0).getLongitude()));
            finalList.add(sortList.get(0));
            finalList2.add(new TMapPoint(sortList.get(0).getLatitude(), sortList.get(0).getLongitude()));
            realfinalList.add(new TMapPoint(sortList.get(0).getLatitude(), sortList.get(0).getLongitude()));
            sortList.clear();
            count++;
        }
    }

//        for (int i = 0; i < cctvs.size(); i++) {
//            cctv = cctvs.get(i);
//            cctvMarker2(tmapview, cctv.getAddr(), cctv.getManageOffice(), cctv.getTellNum(), cctv.getLatitude(), cctv.getLongitude());
//        }

    public void findCctvView2(double start_lat, double start_lon, double end_lat, double end_lon) {
        ArrayList<DB.CCTV> cctvs = new ArrayList<>();
        final DB.CCTV[] cctv = {new DB.CCTV()};
        tmapview.removeAllMarkerItem();
        if (start_lat < end_lat) {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(start_lat - 0.005, end_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
            }
        } else {
            if (start_lon < end_lon) {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, start_lon - 0.005, end_lon + 0.005);
            } else {
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
                cctvs = DB.setDB().navirouteCCTV(end_lat - 0.005, start_lat + 0.005, end_lon - 0.005, start_lon + 0.005);
            }
        }

        while(true) {
            if(realfinalList.size()>=6)
            {
                int n = (int) (Math.random() * 10000)%(realfinalList.size()-1);
                if(n==0)
                    continue;
                realfinalList.remove(n);
            }
            else
                break;
        }


        final TMapData tmapdata = new TMapData();
        final ArrayList<DB.CCTV> finalCctvs = cctvs;


        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, new TMapPoint(start_lat,start_lon), new TMapPoint(end_lat, end_lon), realfinalList, 0, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                ArrayList<DB.CCTV> cctvs2 = new ArrayList<>();
                tMapPolyLine.setLineColor(Color.RED);
                tMapPolyLine.setLineWidth(10);
                tmapview.addTMapPath(tMapPolyLine);
                final double distance = tMapPolyLine.getDistance();
                final double time = distance;
                final double times = time / 60;
                distances = distance / 1000;
                getDistance = String.format("%,.2f", distances);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dis_time.setText(" : " + (int) times + " 분 / " + getDistance + "km ");
                        countTimer = (int)time;

                        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        countDownTimer = new CountDownTimer(((long) time) * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                countTimer--;
                                int min = (int) countTimer / 60;
                                int second = (int) countTimer % 60;
                                dis_time.setText(" : " + String.valueOf(min) + " 분 "+ String.valueOf(second) + " 초");
//                                                        alertMsg.setText(countTimer);
//                                                            Log.e("countTimer", countTimer + "");

                                if (min==0 || min==5 || min==10 || min==15 || countTimer == 0) {
                                    List<Address> address = null;
                                    try {

                                        if (geocoder != null) {
                                            address = geocoder.getFromLocation(current_lat, current_lon, 1); //주소변환
                                            if (address != null && address.size() > 0) { // 주소 글짜르기
                                                String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                                                nowAddress = currentLocationAddress;
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String sms = "위급 상황입니다. 현재위치는 " + nowAddress + " 입니다.";// 현재 위도 경도를 주소로 변환
                                    String url = "http://map.naver.com/?dlevel=8&lat="+current_lat+"&lng="+current_lon;
                                    String selectQuery = "SELECT * FROM " + DictionaryContract.DictionaryEntry.TABLE_NAME;
                                    Cursor cursor2 = mDb.rawQuery(selectQuery, null);
                                    while (cursor2.moveToNext()) {
                                        Log.d("값은 : ", cursor2.getString(2));
                                        try {
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(cursor2.getString(2), null, sms, null, null);
                                            smsManager.sendTextMessage(cursor2.getString(2), null, url, null, null);

                                            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    }

                                    countDownTimer.onFinish();
                                    return;
                                }
                            }
                            @Override
                            public void onFinish() {
                                cancel();
                            }
                        }.start();
                    }
                });


                ArrayList<TMapPoint> ar = tMapPolyLine.getLinePoint();
                for(TMapPoint w : ar) {
                    cctvs2 = DB.setDB().navirouteCCTV(w.getLatitude()-0.0008, w.getLatitude()+0.0008, w.getLongitude()-0.0008, w.getLongitude()+0.0008);
                }

                for(DB.CCTV w : cctvs2)
                {
                    inrangeList.add(w);
                }

                for (int i = 0; i < finalCctvs.size(); i++) {
                    cctv[0] = finalCctvs.get(i);
                    cctvMarker2(tmapview, cctv[0].getAddr(), cctv[0].getManageOffice(), cctv[0].getTellNum(), cctv[0].getLatitude(), cctv[0].getLongitude());
                }
            }
        });
    }



    private boolean addchecking(double latitude, ArrayList<TMapPoint> statePoint) {
        for (TMapPoint w : statePoint) {
            if (latitude == w.getLatitude())
                return false;

        }
        return true;
    }

    private double distance(double tmp_lat, double tmp_lon, double end_lat, double end_lon) {
        //직선거리계산
        return DEGLEN * Math.sqrt(Math.pow(end_lat - tmp_lat, 2) + Math.pow((end_lon - tmp_lon) * Math.cos(Math.toRadians(tmp_lat)), 2));
    }

    private long addNewData(String start_name, String end_name, String start_lat, String start_lon, String end_lat, String end_lon) {
        ContentValues cv = new ContentValues();
        cv.put(Search_DictionaryContract.DictionaryEntry.START_NAME, start_name);
        cv.put(Search_DictionaryContract.DictionaryEntry.END_NAME, end_name);
        cv.put(Search_DictionaryContract.DictionaryEntry.START_LAT, start_lat);
        cv.put(Search_DictionaryContract.DictionaryEntry.START_LON, start_lon);
        cv.put(Search_DictionaryContract.DictionaryEntry.END_LAT, end_lat);
        cv.put(Search_DictionaryContract.DictionaryEntry.END_LON, end_lon);
        return mDb2.insert(Search_DictionaryContract.DictionaryEntry.TABLE_NAME,null,cv);
    }
    public class HttpRequest extends Thread {
        private String url;
        private ContentValues values;
        private DB.CCTV cctv;

        public A(String url, ContentValues values, DB.CCTV cctv) {
            this.url = url;
            this.values = values;
            this.cctv = cctv;
        }

        public void run() {
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            String result = requestHttpURLConnection.request(url, values);
            try {
                JsonParser jsonParser = new JsonParser();
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("features");
                JSONObject featuresObj = (JSONObject) jsonArray.get(0);
                JSONObject propertiesObj = featuresObj.getJSONObject("properties");
                String distance = propertiesObj.getString("totalDistance");
                cctv.setMap_distance(Double.parseDouble(distance));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_location_search) {
            Intent safeNavigation = new Intent(getApplicationContext(), naviActivity.class);
            startActivity(safeNavigation);
        } else if (id == R.id.nav_message_setting) {
            Intent messageIntent = new Intent(getApplicationContext(), MessageSettingActivity.class);
            startActivity(messageIntent);
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                smsCount = smsCount+1;
                if(smsCount==3)
                {
                    smsCount=0;
                    String nowAddress = "현재 위치 확인 불가";
                    List<Address> address = null;
                    try {

                        if (geocoder != null) {
                            address = geocoder.getFromLocation(current_lat, current_lon, 1); //주소변환
                            if (address != null && address.size() > 0) { // 주소 글짜르기
                                String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                                nowAddress = currentLocationAddress;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String sms = "위급 상황입니다. 현재위치는 " + nowAddress + " 입니다.";// 현재 위도 경도를 주소로 변환
                    String url = "http://map.naver.com/?dlevel=8&lat="+current_lat+"&lng="+current_lon;
                    String selectQuery = "SELECT * FROM " + DictionaryContract.DictionaryEntry.TABLE_NAME;
                    Cursor cursor2 = mDb.rawQuery(selectQuery, null);
                    while (cursor2.moveToNext()) {
                        Log.d("값은 : ", cursor2.getString(2));
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(cursor2.getString(2), null, sms, null, null);
                            smsManager.sendTextMessage(cursor2.getString(2), null, url, null, null);

                            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }

                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}