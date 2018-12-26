package com.android.solarcalculator;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "Mapactivity";

    private static final String API_KEY = BuildConfig.Api_Key;

    private FusedLocationProviderClient mFusedLocationClient;

    private TextView tv1,sr,ss,mr,ms;
    private ImageView b1,b2,b3;

    //permission variable
    private Boolean mLocationPermissionsGranted = false;

    private static final float DEFAULT_ZOOM = 15f;


    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //widgets
    private EditText msearchlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        msearchlocation = (EditText) findViewById(R.id.search_location);
        tv1 = (TextView) findViewById(R.id.tv1);
        b1 = (ImageView) findViewById(R.id.b1);
        b2 = (ImageView) findViewById(R.id.b2);
        b3 = (ImageView) findViewById(R.id.b3);

        sr = (TextView) findViewById(R.id.sr);
        ss = (TextView) findViewById(R.id.ss);
        mr = (TextView) findViewById(R.id.mr);
        ms = (TextView) findViewById(R.id.ms);


        //getLocationpermission
        getLocationPermission();

    }


    private void init(){
        Log.d(TAG, "init: initializing");

        msearchlocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER
                        || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geolocate();
                }

                return false;
            }
        });

        hidekeyboard();

        }


    private void geolocate() {

        String location = msearchlocation.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(location, 1);

        }catch (IOException e){
            Log.e(TAG, "IO exception" + e.getMessage());
        }

        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, address.toString());


            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Toast.makeText(getApplicationContext(), dateFormat.format(date), Toast.LENGTH_SHORT).show();

            double lati = address.getLatitude();
            double longi = address.getLongitude();

            String dstring = dateFormat.format(date);
            Date date1 = new Date();
            try {
                date1 =(Date) dateFormat.parse(dstring);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SunTimes suntimes = SunTimes.compute()
                    .on(date1).at(lati, longi).execute();
            Log.d(TAG, "sunrise: " + suntimes.getRise());
            Log.d(TAG, "sunset: " + suntimes.getSet());

            MoonTimes moonTimes = MoonTimes.compute()
                    .on(date1).at(lati, longi).execute();
            Log.d(TAG, "moonrise: " + moonTimes.getRise());
            Log.d(TAG, "moonset: " + moonTimes.getSet());

            tv1.setText(dstring);

            String temp = "";
            String srise = suntimes.getRise().toString();
            temp = getTime(srise);
            sr.setText(temp);
            String sset = suntimes.getSet().toString();
            temp = getTime(sset);
            ss.setText(temp);
            String mrise = moonTimes.getRise().toString();
            temp = getTime(mrise);
            mr.setText(temp);
            String mset = moonTimes.getSet().toString();
            temp = getTime(mset);
            ms.setText(temp);






            Log.d(TAG, "Only Time" + temp);




            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    private String getTime(String srise) {

        String sdf= "hh:mm:ss";
        Date date2 = new Date();
        String[] temp = srise.split(" ");
        SimpleDateFormat objsdf = new SimpleDateFormat("hh:mm:ss");
        try {
            date2 = objsdf.parse(srise);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp[3];

    }

    private void getLocationPermission() {

        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();



            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        Log.d(TAG, "intializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //getting the device location
        if(mLocationPermissionsGranted) {

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();

        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getDeviceLocation(){

        Log.d(TAG, "getting the device location");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //getting the last location
        try {
            if (mLocationPermissionsGranted) {
                final Task location =  mFusedLocationClient.getLastLocation();
                        location.addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful() && task.getResult()!= null){
                                    Log.d(TAG, "location found");
                                    Location currLocation = (Location) task.getResult();

                                    moveCamera(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()),DEFAULT_ZOOM, "My Location");
                                }
                                else {
                                    Log.d(TAG, "location is not found");
                                    Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }catch (SecurityException e){
            Log.e(TAG, "deviceLocation: SecurityException" + e.getMessage());
        }

    }

    //moving the camera to current location
    private void moveCamera(LatLng latLng, float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        //for pindrop
        if(!title.equals("My Location")) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title);

            mMap.addMarker(markerOptions);
        }


        hidekeyboard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    public void hidekeyboard(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
