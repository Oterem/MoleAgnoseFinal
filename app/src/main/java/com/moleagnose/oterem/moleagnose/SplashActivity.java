
package com.moleagnose.oterem.moleagnose;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000;
    private ImageView logoImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImageView = (ImageView) findViewById(R.id.ivLogo);
        logoImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.logo));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_TIMEOUT);

    }

}
