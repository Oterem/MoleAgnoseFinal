package com.example.oterem.demo;

import android.icu.util.RangeValueIterator;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;

import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.logo)
                .setDescription(getResources().getString(R.string.about_description))
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addEmail("shai.work1@gmail.com")
                .addGitHub(getResources().getString(R.string.about_github),"fork us!")
                .addPlayStore("com.ideashower.readitlater.pro")
                .addItem(createCopyright())
                .create();
        setContentView(aboutPage);
    }

    private Element createCopyright(){
        Element copyRight = new Element();
        String copyright = String.format("Copyright %d by Omri Terem & Shai Lehmann",Calendar.getInstance().get(Calendar.YEAR));
        copyRight.setTitle(copyright);
        copyRight.setGravity(Gravity.CENTER);
        return  copyRight;
    }
}