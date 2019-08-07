<div>
<h1> 공공데이터와 A* 알고리즘을 이용한 <br> CCTV 안전 길 찾기 어플리케이션
</div>

## 1-1 프로젝트 개요

날이 갈수록 늘어나는 강력 범죄 발생률이 늘어나고 있다. 사회적 약자에 대한 범죄가 큰 사회적 문제로써 떠오르고 있다. <br>이에 대한 여러 보호 방안이 주어지고 있는데 그 중 하나인 안전 귀가에 대한 문제를 해결하기 위해서, 사용자에게 목적지까지의 '빠른' 경로로 제공해주는 기존의 어플리케이션과는 차별화된 
'안전'을 위한 프로그램을 개발하기로 하였다.  

<div>
 <img width="400" src="/readme_image/1.png"></img>
 <img width="400" src="/readme_image/2.png"></img>
</div>

<br>

## 1-2 사회적 약자의 안전한 귀가를 위해서?
사회적 약자의 안전한 귀가를 위해서 크게 4가지 기능을 제공한다.<br>
1) 안전 길찾기 <br>
  - 현재 위치를 GPS로 인식하고, CCTV의 위도,경도 값을 받아와서 목적지까지 CCTV가 있는 안전한 길로 안내<br>
2) 근처 CCTV, 지구대 표시 <br>
  - 근처 CCTV위치와 지구대의 위치를 확인하고, 위급한 상황시 지구대로 연락 가능<br>
3) 긴급 메세지<br> 
  - 보호자에게 현재 사용자의 위치를 제공해주고, 위급한 상황시 메시지 전송 가능<br>
4) 버튼 이벤트<br>
  - 알람 기능, 후레시 기능은 어플 내에서 간단한 조작으로 사용할 수 있고, 볼륨버튼을 3번 누르면 자동으로 메세지 전송<br>

<br>

## Flow Chart
<div>
 <img width="1000" height="370" src="/readme_image/flowchart.png"></img>
</div>

<br>

## 2 시스템 설계 / 주요 기능 개발 기술
### 1. 안전 길찾기
- 보행자 경로안내와 다중경유지를 제공하는 TMap API를 사용하였다. 도착지까지 CCTV가 있는 안전한 길로 경로를 제공해야 하기 때문에 길찾기 알고리즘에 제일 효율적인 A* 알고리즘을 활용하여 CCTV를 경유하는 안전한 길을 선택하도록 하였다. 

**1.1 맵 띄우기**
```java
    private static String mApiKey = "<<tmap_yourkey>>"; //api key
    
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
        })
```

<br>

**1.2 출발지와 도착지 선택**
<div>
 <img width="1000" height="370" src="/readme_image/5.png"></img>
</div>

<br>

**출발지 기본 값 : 현재 위치 (검색 가능)**

```java
    public void onLocationChange(Location location) {
        current_lat = location.getLatitude();
        current_lon = location.getLongitude();
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }
```

<br>

**도착지 검색 후 도착지의 위도와 경도의 값을 저장 후 마커 표시**

```java
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
```

<br>

**1.3 A star 알고리즘**
<div>
 <img width="1000" height="370" src="/readme_image/6.png"></img>
</div>

<br>

G값 : 출발지부터 경유지(CCTV)까지의 직선 거리 <br>
H값 : 경유지(CCTV)의 값에서 도착지 까지의 도보상 이동 거리<br>

간단히 **기술 메커니즘**을 설명하자면<br>
1) 출발지에서 가까운 cctv중 3개를 선택하여 G값과 추정값(H)를 더하여 가장 최솟값이 되는 cctv를 경유지로 선택<br>
2) 선택된 cctv를 다시 출발점으로 선택하여 1과 같은 방식으로 경유지 선택<br>
3) 도착지까지 선택된 경유지들을 passList에 저장 후 경로를 이음 <br>

<br>

**1.4 Heuristic(추정값) 구하기**
<div>
 <img width="1000" height="370" src="/readme_image/7.png"></img>
</div>

<br>

**HttpUrlConnection로 tmap server와 통신을 하여 tmap api에서 제공하는 출발지와 도착지를 파라미터로 하는 도보상 이동 거리를 요청. 
response값이 json형식으로 나오기 때문에 Parsing 과정을 통해 Heuristic 얻는다**

<br>

```java
    public class HttpRequest extends Thread {
        private String url;
        private ContentValues values;
        private DB.CCTV cctv;

        public HttpRequest(String url, ContentValues values, DB.CCTV cctv) {
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
```

```java
public class RequestHttpURLConnection {

    public String request(String _url, ContentValues _params) {
        HttpURLConnection urlConn = null;
        StringBuffer sbParams = new StringBuffer();
        Log.d("_url : ",_url);

        //1.스트링 버퍼에 파라미터 연결
        if (_params == null)
            sbParams.append("");
        else {
            boolean isAnd = false;
            String key;
            String value;
            for (Map.Entry<String, Object> parameter : _params.valueSet()) {
                key = parameter.getKey();
                value = parameter.getValue().toString();
                if (isAnd)
                    sbParams.append("&");
                sbParams.append(key).append("=").append(value);
                if (!isAnd)
                    if (_params.size() >= 2)
                        isAnd = true;
            }
        }

        //HttpUrlConnection을 통해 web 데이터 가져오기.
        try{
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();
            // [2-1]. urlConn 설정.
            urlConn.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : GET/POST.
            urlConn.setRequestProperty("Accept-Charset", "utf-8"); // Accept-Charset 설정.
            urlConn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded");

            // [2-2]. parameter 전달 및 데이터 읽어오기.
            String strParams = sbParams.toString(); //sbParams에 정리한 파라미터들을 스트링으로 저장. 예)id=id1&pw=123;
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8")); // 출력 스트림에 출력.
            os.flush(); // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행.
            os.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.

            // [2-3]. 연결 요청 확인.
            // 실패 시 null을 리턴하고 메서드를 종료.
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            // [2-4]. 읽어온 결과물 리턴.
            // 요청한 URL의 출력물을 BufferedReader로 받는다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            // 출력물의 라인과 그 합에 대한 변수.
            String line;
            String page = "";

            // 라인을 받아와 합친다.
            while ((line = reader.readLine()) != null){
                page += line;
            }
            return page;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConn!=null)
                urlConn.disconnect();
        }
        return null;
    }
}
```

<br>

**1.5 G값 구하기**
<div>
 <img width="1000" height="370" src="/readme_image/8.png"></img>
</div>

<br>

**도착지 까지의 직선거리. 사용자가 눈에 보이는 것은 2차원의 지도형식이지만 실제로 지구는 둥글기 때문에 평면상의 직선거리로 하면 실제 거리와 차이가 많이 나기 때문에 위도와 경도를 이용한 공식을 사용하여 값을 구해야 한다.**

<br>

```java
    private double distance(double start_lat, double start_lon, double end_lat, double end_lon) {
        //직선거리계산
        return DEGLEN * Math.sqrt(Math.pow(end_lat - start_lat, 2) + Math.pow((end_lon - start_lon) * Math.cos(Math.toRadians(start_lat)), 2));
    }
```

<br>

**1.6 G+H=F값이 작은 순으로 정렬**

```java
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
```

<br>

**1.7 선택된 cctv가 있는 배열인 passList에 있는 값을 이용해 경로 도출**
<div>
 <img width="1000" height="370" src="/readme_image/9.png"></img>
</div>

```java
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, new TMapPoint(start_lat,start_lon), new TMapPoint(end_lat, end_lon), passList, 0, new TMapData.FindPathDataListenerCallback() {
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
                
                //경로상의 cctv 마커 표시
                for (int i = 0; i < finalCctvs.size(); i++) {
                    cctv[0] = finalCctvs.get(i);
                    cctvMarker2(tmapview, cctv[0].getAddr(), cctv[0].getManageOffice(), cctv[0].getTellNum(), cctv[0].getLatitude(), cctv[0].getLongitude());
                }
            }
        });
```

<br>

### 2. 데이터 베이스
- 경찰서와 cctv의 데이터 베이스를 공공데이터포털에서 제공해주는 값으로 넣어두고, 사용자는 보호자의 연락처를 등록해놓을 때 Android Studio에서 제공하는 SQLiteDB에 저장하여서 사용한다.

<br>

**2.1 데이터베이스 불러오기**
- 어플 최초 실행한다면 또는 데이터베이스가 업데이트가 되었다면 데이터베이스를 불러와서 저장하는 역할을 실행하고, 아닐 경우 그대로 사용.
```java
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB.setDB().isCheckDB(SplashActivity.this,"/data/data/" + getPackageName() + "/databases","finalcctv.db");
        DBPOLICE.setDB().isCheckDB(SplashActivity.this,"/data/data/" + getPackageName() + "/databases","POLICE.db");

        checkPermissions();
}
```
<br>

**2.2 데이터베이스 설계**

<br>

**CCTV DTO**
```java
 public static class CCTV {
        String addr;
        String manageOffice;
        String tellNum;
        double latitude;
        double longitude;
        double d_heuristic;
        double map_distance;
        boolean state=false;

        public CCTV(){}
        public CCTV(String addr,
             String manageOffice,
             String tellNum,
             double latitude,
             double longitude) {
            this.addr = addr;
            this.manageOffice = manageOffice;
            this.tellNum = tellNum;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getAddr() {
            return addr;
        }

        public String getManageOffice() {
            return manageOffice;
        }

        public String getTellNum() {
            return tellNum;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setD_heuristic(double distance) {d_heuristic = distance;}

        public double getD_heuristic() {return d_heuristic;}

        public void setMap_distance(double m_distance) {map_distance = m_distance;}

        public double getMap_distance() {return map_distance;}
    }
```
**POLICE DTO**
```java
public static class POLICE {
        String name;
        String manageOffice;
        String kind;
        String tellNum;
        String addr;
        Double latitude;
        Double longitude;
        public POLICE(){}
        public POLICE(String manageOffice, String tellNum, String addr, double latitude, double longitude) {
            this.name = name;
            this.addr = addr;
            this.kind = kind;
            this.manageOffice = manageOffice;
            this.tellNum = tellNum;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        public String getName() { return name; }
        public String getManageOffice() { return manageOffice; }
        public String getKind() { return kind; }
        public String getTellNum() { return tellNum; }
        public String getAddr() { return addr; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }
```

<br>

**2.3 보호자 등록**
<div>
 <img width="1000" height="370" src="/readme_image/4.png"></img>
</div>

```java
public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WAITLIST_TABLE = "CREATE TABLE " + DictionaryContract.DictionaryEntry.TABLE_NAME + " (" +
                DictionaryContract.DictionaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DictionaryContract.DictionaryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DictionaryContract.DictionaryEntry.COLUMN_phoneNum + " TEXT NOT NULL, " +
                DictionaryContract.DictionaryEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";

        // 쿼리 실행
        sqLiteDatabase.execSQL(SQL_CREATE_WAITLIST_TABLE);
    }
```

<br>

### 3. 긴급메시지 및 버튼 이벤트

<br>

**3.1 긴급메시지**
- 문자 버튼 클릭시 데이터베이스에 보호자로 등록된 번호로 현재위치를 알려주고 지도에 정확한 위치가 표시됨.

<br>

<div>
 <img width="1000" height="370" src="/readme_image/10.png"></img>
</div>

```java
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
```

<br>

**OUTPUT**
<div>
 <img width="1000" height="370" src="/readme_image/11.png"></img>
</div>

<br>

**3.2 버튼이벤트**
- 볼륨버튼에 대한 조작 권한을 얻어서 위급한 상황시 볼륨down버튼을 3번 누르면 보호자에게 문자 전송
```java
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
```


