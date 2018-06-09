package io.move.move;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private Socket socket;
    private static final int SERVERPORT = 6000;
    private static final String SERVER_IP = "192.168.15.187";

    GoogleMap googleMap;
    private Location mCurrentLocation;
    //private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    boolean mRequestLocationUpdates;
    String mLastUpdateTime;
    int permissionLocationCheckFine;
    GoogleApiClient mGoogleApiClient;
    SupportMapFragment mapFragment;

    double mLatitude, mLongitude;

    /**
     * sample latitudes and longitudes of places as follows:
     * banashankari, whitefield, yeshwantpur, koramangla, electronic city, airport.
     */
    double[] latArray = {12.9255, 12.9698, 13.0280, 12.9279, 12.8407, 13.1986};
    double[] longArray = {77.5468, 77.7499, 77.5409, 77.6271, 77.6763, 77.7066};

    MarkerOptions markerOptions1;
    MarkerOptions markerOptions2;
    MarkerOptions markerOptions3;
    MarkerOptions markerOptions4;
    MarkerOptions markerOptions5;
    MarkerOptions markerOptions6;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.mSlidingUpPanel);
        new Thread(new ClientThread()).start();
        checkForPermissions();

        //declare the map
        mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
    }

    public void letsStartListeningLocation() {

        mRequestLocationUpdates = true;


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }
        //mLocationRequest is initialized

        createLocationRequest();

        //then the locationSettings builder is also initialized
        LocationSettingsRequest.Builder mLocationSettingsRequest = new
                LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        mLocationSettingsRequest.build());


    }

    @Override
    protected void onStart() {
        letsStartListeningLocation();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    protected void createLocationRequest() {
        //sets settings for the locationrequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            /* allotted interval is 10 sec, and the fastest interval it can recieve
            due to other apps is 5 seconds to revent exceptions
            */
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mRequestLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
       /* if (googleMap != null)
            googleMap.animateCamera(cameraUpdate);*/
        updateUi();
    }

    private void updateUi() {
        //lastUpdatedTime.setText("Location last updated on " + mLastUpdateTime);
//        coordinateTextView.setText("Latitude is " + String.valueOf(mCurrentLocation.getLatitude())
        //              + "Longitude is" + String.valueOf(mCurrentLocation.getLongitude()));
        mapFragment.getMapAsync(this);

    }

    //permissions
    private void checkForPermissions() {
        //check if permissions are granted, and then continue
        permissionLocationCheckFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocationCheckFine != PackageManager.PERMISSION_GRANTED) {
            //permission not granted. proceed to ask
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    12);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    onRestart();


                } else {

                    // permission denied, boo!
                    //for now, the app keeps asking until the user agrees. NOTE: ITS A BAD UX PRACTICE
                    checkForPermissions();
                }
                return;
            }

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mLatitude = latLng.latitude;
        mLongitude = latLng.longitude;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11);
        googleMap.animateCamera(cameraUpdate);
        this.googleMap = googleMap;

        BitmapDescriptor mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bike_icon);

        //make more markeroptions and addMarkers
        markerOptions1 = new MarkerOptions().position(new LatLng(latArray[0], longArray[0])).
                title("banashankari").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions1);


        markerOptions2 = new MarkerOptions().position(new LatLng(latArray[1], longArray[1])).
                title("whitefield").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions2);

        markerOptions3 = new MarkerOptions().position(new LatLng(latArray[2], longArray[2])).
                title("yeshwantpur").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions3);

        markerOptions4 = new MarkerOptions().position(new LatLng(latArray[3], longArray[3])).
                title("koramangla").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions4);


        markerOptions5 = new MarkerOptions().position(new LatLng(latArray[4], longArray[4])).
                title("Electronic city").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions5);

        markerOptions6 = new MarkerOptions().position(new LatLng(latArray[5], longArray[5])).
                title("Airport").icon(mBitmapDescriptor);
        googleMap.addMarker(markerOptions6);

        //googleMap.setMinZoomPreference(13.0f);

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        //TODO make a ui
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.swipeView);
        relativeLayout.setVisibility(View.VISIBLE);
        TextView mTextView = (TextView) findViewById(R.id.slideUpText);
        mTextView.setText(arg0.getTitle());

        //scanQRCode();
        return false;
    }


    /***
     * Starts a scan of QR code of the bicycle
     */
    public void scanQRCode(View v) {
        // variable to initiate QR scan
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Place the bicycle's QR code inside the rectangle to scan it.");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Log.v("TAG", "InetAddress created successfully");
                socket = new Socket(serverAddr, SERVERPORT);
                Log.v("TAG", "Client created successfully");

            } catch (UnknownHostException e1) {
                Log.v("TAG", "unknownhostexception");
                e1.printStackTrace();
            } catch (IOException e1) {
                Log.v("TAG", "ioexception");
                e1.printStackTrace();
            }

        }

    }

    /***
     * Fires up after QR scan is done
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
            } else {
                final String contents = result.getContents();
                Log.v("TAG", "QR code contents: " + contents);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(contents);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                final MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title("Starting a ride")
                        .customView(R.layout.dialog_ridestart, true)
                        .build();
                View dialogCustomView = dialog.getCustomView();
                if (dialogCustomView != null) {
                    dialogCustomView.findViewById(R.id.ridestart_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialogCustomView.findViewById(R.id.ridestart_yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // opens RideActivity
                            Intent mIntent = new Intent (MapActivity.this, RideActivity.class);
                            mIntent.putExtra("Latitude", mLatitude);
                            mIntent.putExtra("Longitude", mLongitude);
                            startActivity(mIntent);

                        }
                    });
                    dialog.show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
