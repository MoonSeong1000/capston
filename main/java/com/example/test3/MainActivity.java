package com.example.test3;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TMapGpsManager.onLocationChangedCallback {

    private SQLiteDatabase mDb;

    String TAG = "placeautocomplete"; //자동검색

    Camera mCamera; //카메라 변수
    boolean isOnFlash = false; //불빛이 꺼져 있다는 뜻
    boolean isOnAlarm = false;

    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private TMapMarkerItem CurrentMarker;
    private TMapData tmapdata = null;

    private static String mApiKey = "<<tmap_yourkey>>"; //api key
    private static int mMarkerID;

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();

    private String address;
    private Double lat = null;
    private Double lon = null;
    private Double current_lat;
    private Double current_lon;

    final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    private int smsCount=0;

    @Override
    public void onLocationChange(Location location) {
        current_lat = location.getLatitude();
        current_lon = location.getLongitude();
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DictionaryDbHelper dbHelper = new DictionaryDbHelper(this);
        mDb = dbHelper.getReadableDatabase();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        onMap();
        //floating 버튼

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

        com.getbase.floatingactionbutton.FloatingActionButton action_light = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_light);
        action_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera = Camera.open(); //카메라 기반 객체 생성 및 카메라 접근
                if(isOnFlash==false) {
                    Snackbar.make(view, "라이트 ON", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //카메라의 상태를 갖고있는 파라미터 변수 생성
                    Camera.Parameters mCameraParameter = mCamera.getParameters();
                    //파라미터 변수를 이용해 조작값 지정(플래시)
                    mCameraParameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mCameraParameter);
                    mCamera.startPreview();
                    isOnFlash=true;
                }
                else {
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
                }else{
                    m.start();
                    isOnAlarm = true;
                }
            };
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //검색 fragment
        Places.initialize(getApplicationContext(), "google_your_apikey");
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_main);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment.a.setTextSize(14);
        ((View)findViewById(R.id.places_autocomplete_search_button)).setVisibility(View.GONE);
        autocompleteFragment.setHint("장소, 주소 검색");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //선택한 장소의 위도와 경도의 값을 end라는 변수에 넣음
                LatLng start = place.getLatLng();
                TMapPoint selectPoint = new TMapPoint(start.latitude, start.longitude);
                showMarker(selectPoint,place);
                tmapview.setCenterPoint(start.longitude, start.latitude, true);
                Toast.makeText(MainActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
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
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
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


    public void onMap() {
        mContext=this;
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapview);
        tmapdata = new TMapData();
        tmapview = new TMapView(this);

        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApiKey);

        addPoint();
        showMarkerPoint();

        /* 현재 보는 방향 */
        tmapview.setCompassMode(true);

        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);

        /* 줌레벨 */
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(MainActivity.this);
        tmapgps.setMinTime(10);
        tmapgps.setMinDistance(1);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
        }
        tmapgps.OpenGps();

        /*  화면중심을 단말의 현재위치로 이동 */
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        // 풍선 클릭시 할 행동입니다
        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback()
        {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                Toast.makeText(MainActivity.this, "클릭", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void addPoint() { //여기에 핀을 꼽을 포인트들을 배열에 add해주세요!

    }


    public void showMarkerPoint() {// 마커 찍는거 빨간색 포인트.
        for (int i = 0; i < m_mapPoint.size(); i++) {
            TMapPoint point = new TMapPoint(m_mapPoint.get(i).getLatitude(),
                    m_mapPoint.get(i).getLongtitude());
            TMapMarkerItem item1 = new TMapMarkerItem();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_here);
            //poi_dot은 지도에 꼽을 빨간 핀 이미지입니다

            item1.setTMapPoint(point);
            item1.setName(m_mapPoint.get(i).getName());
            item1.setVisible(item1.VISIBLE);

            item1.setIcon(bitmap);

            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_here);

            // 풍선뷰 안의 항목에 글을 지정합니다.
            item1.setCalloutTitle(m_mapPoint.get(i).getName());
            item1.setCalloutSubTitle("서울");
            item1.setCanShowCallout(true);
            item1.setAutoCalloutVisible(true);

            Bitmap bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_here);

            item1.setCalloutRightButtonImage(bitmap_i);

            String strID = String.format("pmarker%d", mMarkerID++);

            tmapview.addMarkerItem(strID, item1);
            mArrayMarkerID.add(strID);
        }
    }


    public void showMarker(TMapPoint point, final Place place) {
        final CharSequence name = place.getName();

        TMapMarkerItem marker_item = new TMapMarkerItem();
        marker_item.setTMapPoint(point);
        marker_item.setName((String) name);
        marker_item.setVisible(TMapMarkerItem.VISIBLE);

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.flag5);
        marker_item.setIcon(current_flag);
        marker_item.setCanShowCallout(true);
        marker_item.setCalloutTitle((String) name);

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.playbutton);
        marker_item.setCalloutRightButtonImage(right_bitmap);

        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                Toast.makeText(MainActivity.this, "클릭 완료", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle((String) name);
                builder.setMessage("도착지로 등록하시겠습니까? ");

                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent naviIntent = new Intent(getApplicationContext(), naviActivity.class);
                        naviIntent.putExtra("lat",place.getLatLng().latitude);
                        naviIntent.putExtra("lon",place.getLatLng().longitude);
                        naviIntent.putExtra("name",(String) name);
                        naviIntent.putExtra("now_lat",current_lat);
                        naviIntent.putExtra("now_lon",current_lon);
                        startActivity(naviIntent);
                    }
                });
                builder.setNegativeButton("아니요",null);
                builder.setNeutralButton("취소",null);
                builder.create().show();
            }
        });

        tmapview.addMarkerItem("select",marker_item);

       // tmapview.addMarkerItem("search_itemmarker_item.setVisible((TMapMarkerItem.VISIBLE));",marker_item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent naviIntent = new Intent(getApplicationContext(), naviActivity.class);
            naviIntent.putExtra("now_lat",current_lat);
            naviIntent.putExtra("now_lon",current_lon);
            startActivity(naviIntent);
        }

        return super.onOptionsItemSelected(item);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_location_search) {
            Intent naviIntent = new Intent(getApplicationContext(), naviActivity.class);
            naviIntent.putExtra("now_lat",current_lat);
            naviIntent.putExtra("now_lon",current_lon);
            startActivity(naviIntent);
        } else if (id == R.id.nav_message_setting) {
            Intent messageIntent = new Intent(getApplicationContext(), MessageSettingActivity.class);
            startActivity(messageIntent);
        }  else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
