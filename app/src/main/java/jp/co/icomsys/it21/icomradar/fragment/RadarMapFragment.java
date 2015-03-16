package jp.co.icomsys.it21.icomradar.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.icomsys.it21.icomradar.R;
import jp.co.icomsys.it21.icomradar.activity.MainActivity;

public class RadarMapFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private MapView mapView;

    private GoogleApiClient mLocationClient;
    private FusedLocationProviderApi mFusedLocationProviderApi = LocationServices.FusedLocationApi;

    private static final String ARG_SECTION_NUMBER = "section_number";

//    private LocationManager mLocationManager;
    private Location mLocation;

    // アイコムシステックの緯度経度
    private static final double ICOM_LATITUDE = 35.64649742;
    private static final double ICOM_LONGITUDE = 139.748530399999996;

    private static final LatLng mIcomLatLng = new LatLng(ICOM_LATITUDE, ICOM_LONGITUDE);


    // 更新時間(目安)
    private static final int LOCATION_UPDATE_MIN_TIME = 500;
    // 更新距離(目安)
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 0;

    private static final int TIMER_PERIOD = 500;

    private Marker mMarkerIcom;
    private Marker mMarkerPresent;

    private Timer   mTimer   = null;
    private Handler mHandler = new Handler();
    private int mVisiblePresentCnt = 0;

    private float mZoom = 19;
    private boolean mVisiblePresent = true;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RadarMapFragment newInstance(int sectionNumber) {
        RadarMapFragment fragment = new RadarMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RadarMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

//        TextView textView = (TextView) view.findViewById(R.id.section_label);
//        textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

        MapsInitializer.initialize(getActivity());

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

//        //GPSサービスの取得
//        mLocationManager = (LocationManager)getActivity().getSystemService(Service.LOCATION_SERVICE);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMyLocationEnabled(false);

//        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.dragonball);
//        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(R.drawable.dragonball))
//                .position(mIcomLatLng, image.getWidth(),image.getHeight());
//        map.addGroundOverlay(newarkMap);

        mMarkerIcom = map.addMarker(
                new MarkerOptions()
                        .position(mIcomLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dragonball))
                        .anchor(0.5f, 0.5f)
                        .alpha(0.5f));

        movePresentLocation(mapView.getMap());
    }

    @Override
    public void onResume() {

        startLocate();

//        //GPSから現在地の情報取得の開始
//        if(mLocationManager != null) {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    LOCATION_UPDATE_MIN_TIME,
//                    LOCATION_UPDATE_MIN_DISTANCE,
//                    this);
//            //GPSから最後に取得した情報を初期値として設定
//            mLocation = mLocationManager.getLastKnownLocation("gps");
//        }

        startLocate();

        // タイマー実行
        mTimer = new Timer(false);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        movePresentLocation(mapView.getMap());
                        mVisiblePresentCnt += 1;
                        if (mVisiblePresentCnt == 2) {
                            mVisiblePresent = false;
                            mMarkerPresent.setVisible(mVisiblePresent);
                        } else if(mVisiblePresentCnt == 3) {
                            mVisiblePresent = true;
                            mMarkerPresent.setVisible(mVisiblePresent);
                            mVisiblePresentCnt = 0;
                        }
                    }
                });
            }
        }, 100, TIMER_PERIOD);

        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {

        mTimer.cancel();

        stopLocate();

        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // 現在地取得
    public Location getLocation(){
        return mLocation;
    }

    @Override
    public void onLocationChanged(final Location location) {

        Log.d("change location", String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
        mLocation = location;
    }

    public void setZoom(float zoom) {

        if (mapView != null) {
            GoogleMap map = mapView.getMap();
            mZoom = zoom;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, mZoom));
        }
    }

    public void setMapType(int type) {

        GoogleMap map = mapView.getMap();
        map.setMapType(type);
    }

    private void movePresentLocation(GoogleMap map) {

        LatLng latlng = mIcomLatLng;
        if (mLocation != null) {
            latlng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, mZoom));
        if (mMarkerPresent != null) {
            mMarkerPresent.remove();
        }
        mMarkerPresent = map.addMarker(
                new MarkerOptions()
                        .position(latlng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot))
                        .anchor(0.5f, 0.5f)
                        .alpha(0.5f)
                        .visible(mVisiblePresent));
    }

    private void startLocate() {

        mLocationClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationClient.connect();
    }

    private void stopLocate() {

        if (mLocationClient == null || !mLocationClient.isConnected()) {
            return;
        }

        mFusedLocationProviderApi.removeLocationUpdates(mLocationClient, this);
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(LOCATION_UPDATE_MIN_TIME);
        if (mLocationClient.isConnected()) {
            mFusedLocationProviderApi.requestLocationUpdates(mLocationClient, request, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        mLocationClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
