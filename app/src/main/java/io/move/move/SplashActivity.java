package io.move.move;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openMainIntent();
            }
        }, 2000);
    }

    private void openMainIntent() {
        Intent intent = new Intent(SplashActivity.this, MapActivity.class);
        startActivity(intent);
    }
}
