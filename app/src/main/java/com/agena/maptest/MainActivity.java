package com.agena.maptest;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{


    private GoogleApiClient mGoogleApiClient;
    private GoogleMap googleMap;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mTimeZone;
    private TextView mCurrentTime;
    private TextView mUtcTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println(">>>>>> Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTimeZone = (TextView) findViewById(R.id.timezone);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude);
        mCurrentTime = (TextView) findViewById(R.id.currentTime);
        mUtcTime = (TextView) findViewById(R.id.utcTime);

        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();

        mTimeZone.setText("TimeZone: " + timeZone.getDisplayName());

        // Clock
        Thread myThread = null;

        Runnable myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    DateFormat dateFormatCur = DateFormat.getTimeInstance();
                    String curTime  = dateFormatCur.format(new Date());

                    mCurrentTime.setText("Current Time:" + curTime);

                    DateFormat dateFormatUTC = DateFormat.getTimeInstance();
                    dateFormatUTC.setTimeZone(TimeZone.getTimeZone("gmt"));
                    String utcTime  = dateFormatUTC.format(new Date());
                    mUtcTime.setText("UTC Time:" + utcTime);
                }catch (Exception e) {}
            }
        });
    }

    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        System.out.println(">>>>>> buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng eroad = new LatLng(-36.7224008,174.7073903);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eroad, 13));
        googleMap.addMarker(new MarkerOptions().position(eroad));

        mLatitudeTextView.setText("Latitude: " + String.valueOf(-36.7224008));
        mLongitudeTextView.setText("Longitude: " + String.valueOf(174.7073903));
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println(">>>>>> Location Update");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println(">>>>>> onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println(">>>>>> onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println(">>>>>> Location Changed");
        mCurrentLocation = location;

        mLatitudeTextView.setText("Latitude: " + String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText("Longitude: " + String.valueOf(mCurrentLocation.getLongitude()));

        LatLng current = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current, 13);

        googleMap.animateCamera(update);
        googleMap.setMyLocationEnabled(true);

        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();

        mTimeZone.setText("TimeZone: " + timeZone.getDisplayName());
    }
}
