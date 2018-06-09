package io.move.move;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RideActivity extends AppCompatActivity {
    double initLat, initLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        initLat = getIntent().getDoubleExtra("Latitude", 0);
        initLong = getIntent().getDoubleExtra("Longitude", 0);



    }

    public void mapOpen(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="+ 13.1986 + ","+ 77.7066));
        //leads to airport.
        startActivity(intent);
    }

    public void finishRide(View view) {
        Intent intent = new Intent(RideActivity.this, MapActivity.class);
        startActivity(intent);
    }
}
