package io.move.move;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Socket socket;
    private static final int SERVERPORT = 6000;
    private static final String SERVER_IP = "192.168.15.187";

    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        new Thread(new ClientThread()).start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);//depends.
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(12.9754, 80.2206)));
        /*mGoogleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(13.0012,80.2565),
                new LatLng(12.9754,80.2206)));//gotta include the city bounds. here it's adyar and vChery*/


        //make more markeroptions and addMarkers
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(12.9754, 80.2206)).
                title("firstplace");
        googleMap.addMarker(markerOptions);

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
}
