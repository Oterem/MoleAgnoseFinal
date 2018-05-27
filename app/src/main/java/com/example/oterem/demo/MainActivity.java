package com.example.oterem.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onItemClick(View v){
        Intent myIntent = new Intent(MainActivity.this, NavigationActivity.class);
        MainActivity.this.startActivity(myIntent);

    }
}
