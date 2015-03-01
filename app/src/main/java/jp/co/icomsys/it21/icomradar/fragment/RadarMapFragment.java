package jp.co.icomsys.it21.icomradar.fragment;

import android.app.Fragment;
import android.app.Service;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import jp.co.icomsys.it21.icomradar.R;

public class RadarMapFragment extends Fragment implements OnMapReadyCallback,LocationListener {

    private MapView mapView;

    private LocationManager mLocationManager;
    private Location mLocation;

    // アイコムシステックの緯度経度
    private static final double ICOM_LATITUDE = 35.64649742;
    private static final double ICOM_LONGITUDE = 139.748530399999996;

    // 更新時間(目安)
    private static final int LOCATION_UPDATE_MIN_TIME = 0;
    // 更新距離(目安)
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapsInitializer.initialize(getActivity());

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //GPSサービスの取得
        mLocationManager = (LocationManager)getActivity().getSystemService(Service.LOCATION_SERVICE);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));

    }

    @Override
    public void onResume() {

        //GPSから現在地の情報取得の開始
        if(mLocationManager != null) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    this);
            //GPSから最後に取得した情報を初期値として設定
            mLocation = mLocationManager.getLastKnownLocation("gps");
        }

        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        // GPS位置取得の終了
        if(mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        // GPS位置取得の終了
        if(mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

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

    // 現在の緯度経度取得 このメソッドなくてもいいけどまあ一応。
    public LatLng getPresentLatLng() {
        LatLng presentLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        return presentLatLng;
    }

    // アイコムちゃんの緯度経度取得
    public LatLng getIcomLatLng(){
        LatLng icomLatLng = new LatLng(ICOM_LATITUDE, ICOM_LONGITUDE);
        return icomLatLng;
    }

    /////////////////////////////////////////////////
    // ここから４つLocationListerに必須のメソッド
    // Called when the location has changed.
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        //Log.e(TAG, "onLocationChanged.");
    }
    @Override
    public void onProviderDisabled(String provider){}
    @Override
    public void onProviderEnabled(String provider){}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){}

}
