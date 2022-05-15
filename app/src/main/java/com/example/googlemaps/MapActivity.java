package com.example.googlemaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{


    private static final String TAG = "MapActivity";
    private static final float ZOOM = 16;
    private static final int LOCATION_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;  //Domyślne brak uprawnień

    private GoogleMap mMap;     //Mapa google
    private AutoCompleteTextView mAutoCompleteTextView;   //Wyszukiwarka
    private ImageView mGPS; //Ikonka GPS

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, R.string.map_is_ready, Toast.LENGTH_SHORT).show(); //"Mapa jest gotowa"
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.search);
        mGPS = (ImageView) findViewById(R.id.ic_gps);

        getLocationPermission();

    }

    private void init(){

        mAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) { //Przyciski na które reaguje wyszukiwarka
                if(actionID == EditorInfo.IME_ACTION_SEARCH || actionID == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN|| keyEvent.getAction() == KeyEvent.KEYCODE_ENTER ){
                    geoLocate();
                }
                return false;
            }
        });

        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    //Wciśnięcie ikony GPS
                getDeviceLocation();
            }
        });

    }

    private void geoLocate(){   //Uruchomiono geolokalizację

        String searchString = mAutoCompleteTextView.getText().toString();

        Geocoder mGeocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();    //lista pod sugerowane wyszukiwanie, które nie zadziałało :c
        try{
            list = mGeocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, getString(R.string.geoLocate) + e.getMessage());
        }

        if(list.size() > 0){
            Address address = list.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),ZOOM, address.getAddressLine(0));
        }
    }

    private void getLocationPermission() {  //Przydzielaniee uprawnień

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};    //Bardzo dokladna lokalizacja

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_CODE);
        }
    }

    private void getDeviceLocation(){ //Pobieranie aktualnej lokalizacji urządzenia
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){    //Odnaleziono lokalizacje + przesunięcie kamery na lokalizacje
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    ZOOM, getString(R.string.Localization));
                        }else{
                            Toast.makeText(MapActivity.this, R.string.Cant_get_location, Toast.LENGTH_SHORT).show();  //Obecna lokalizacja wynosi null
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, getString(R.string.getDeviceLocation) + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals(getString(R.string.Localization))){
            MarkerOptions mMarker= new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(mMarker);
        }
    }

    private void initMap(){ //Inicjalizacja mapy
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //Wysłano zapytanie

        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false; //Nie przyznano uprawnień
                        return;
                    }
                mLocationPermissionsGranted = true;   //Przyznano uprawnienia
                //inicjuj mapę
                initMap();
            }
        }
    }

}