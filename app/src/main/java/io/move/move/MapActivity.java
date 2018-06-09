package io.move.move;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private Socket socket;
    private static final int SERVERPORT = 6000;
    private static final String SERVER_IP = "192.168.15.187";

    GoogleMap mGoogleMap;
    LocationSettingsRequest.Builder mBuilder;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    FusedLocationProviderClient mFusedLocationProviderClient;

    SlidingUpPanelLayout mSlidingUpPanel;

    MarkerOptions markerOptions1;
    MarkerOptions markerOptions2;
    MarkerOptions markerOptions3;
    MarkerOptions markerOptions4;
    MarkerOptions markerOptions5;
    MarkerOptions markerOptions6;

    /**
     *     sample latitudes and longitudes of places as follows:
            banashankari, whitefield, yeshwantpur, koramangla, electronic city, airport.
     */
    double[] latitudes = {12.9255, 12.9698, 13.0280, 12.9279, 12.8407, 13.1986};
    double[] longitudes = {77.5468, 77.7499, 77.5409, 77.6271, 77.6763, 77.7066};


    String TAG = "MapView, MOOVEBABY";

    //last location
    public double lat;
    public double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.mSlidingUpPanel);
        new Thread(new ClientThread()).start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //makes a location request
        createLocRequest();
        //makes the LocationSettingsRequest.Builder
        mBuilder = new LocationSettingsRequest.Builder();
        mSettingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = mSettingsClient.checkLocationSettings(mBuilder.build());


        //when the location settings are all set and good
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    //Location settings are active. can get data.
                    Log.e(TAG, "location settings are OK. MAPVIEW");
                    pullLastLocationData();
            }
        });

        //when the user location settings are not a-OK
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //location settings not active. reasons be phone's location turned off or no GPS.
                //TODO respond to user to turn on gps.
                Log.e(TAG, "location settings are NOT OK. MAPVIEW");

            }
        });

    }

    @SuppressLint("MissingPermission")
    private void pullLastLocationData() {
        //TODO HAVE TO ASK FOR PERMISSION IN THE SPLASH>>>> FOR NOW WE ARE ENABLING IT MANUALLY.
        Log.e(TAG, "dont forget to ask permission");
        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!= null){
                    Log.e(TAG, "location INFO are OK. MAPVIEW");
                    lat =  location.getLatitude();
                    Log.e(TAG, "Latitude is "+ lat );
                    longitude =  location.getLongitude();
                    Log.e(TAG, "Longitude is "+ longitude );



                    updateMapView();
                }
            }
        });


    }

    private void updateMapView() {
        //gets the last known location, focuses on map.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.e(TAG, "location settings are OK. NOW MAP IS UPDATING TO THAT LOCATION COORDINATES");
        MarkerOptions mMarkerOptions = new MarkerOptions().position(new LatLng(lat, longitude));
        BitmapDescriptor mIcon = BitmapDescriptorFactory.fromResource(R.drawable.user_location);
        mMarkerOptions.icon(mIcon);

        /**
         *         TODO BASICALLY THIS IS NOT SUPPOSED TO BE A MARKER>>>
         *         TODO the actual camera view should be here, continuously updating through a loop
         *         TODO but im putting that as a marker cuz im an asshole
         *
         */

        mGoogleMap.addMarker(mMarkerOptions);




    }

    private void createLocRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setInterval(10000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "the Map is ready");
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//depends.
        if(lat != 0 && longitude != 0){
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, longitude)));
            mGoogleMap.setMaxZoomPreference(400);
            mGoogleMap.setMinZoomPreference(10);

        }else{
            //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(12.9754, 80.2206)));
            //mGoogleMap.setMaxZoomPreference(200);
            //mGoogleMap.setMinZoomPreference(10);

        }
        /*mGoogleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(13.0012,80.2565),
                new LatLng(12.9754,80.2206)));//gotta include the city bounds. here it's adyar and vChery*/

        googleMap.setOnMarkerClickListener(this);


        //make more markeroptions and addMarkers
        markerOptions1 = new MarkerOptions().position(new LatLng(latitudes[0], longitudes[0])).
                title("banashankari");
        googleMap.addMarker(markerOptions1);


        markerOptions2 = new MarkerOptions().position(new LatLng(latitudes[1], longitudes[1])).
                title("whitefield");
        googleMap.addMarker(markerOptions2);

        markerOptions3 = new MarkerOptions().position(new LatLng(latitudes[2], longitudes[2])).
                title("yeshwantpur");
        googleMap.addMarker(markerOptions3);

        markerOptions4 = new MarkerOptions().position(new LatLng(latitudes[3], longitudes[3])).
                title("koramangla");
        googleMap.addMarker(markerOptions4);



        markerOptions5 = new MarkerOptions().position(new LatLng(latitudes[4], longitudes[4])).
                title("Electronic city");
        googleMap.addMarker(markerOptions5);

        markerOptions6 = new MarkerOptions().position(new LatLng(latitudes[5], longitudes[5])).
                title("Airport");
        googleMap.addMarker(markerOptions6);


    }


    /***
     * Starts a scan of QR code of the bicycle
     */
    public void scanQRCode(View view) {
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
                            // TODO: Start ride
                            dialog.dismiss();

                        }
                    });
                    dialog.show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        /**
         * finish for all markers
         */
        if(marker.equals(markerOptions1)){
            //mSlidingUpPanel.setVisibility(View.VISIBLE);
        }else if(marker.equals(markerOptions2)){
            //mSlidingUpPanel.setVisibility(View.VISIBLE);


        }else if(marker.equals(markerOptions2)){
        //mSlidingUpPanel.setVisibility(View.VISIBLE);


        }else{

        }
        return false;
    }
}
