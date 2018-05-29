package com.example.oterem.demo;


import android.content.ClipData;

import android.app.Dialog;

import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;

import android.widget.Button;

import android.widget.Toast;
import com.droidbyme.dialoglib.AnimUtils;
import com.droidbyme.dialoglib.DroidDialog;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //---------------------------- static Vars --------------------------------------
    private static final int ACTION_IMAGE_CAPTURE = 1;
    private static final int ACTION_GET_CONTENT = 2;
    //--------------------------End static Vars -------------------------------------
    //----------------------------- Global Vars --------------------------------------
    private Uri photoURI;
    //--------------------------END Global var --------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        showCase("true");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startAboutActivity();
            return true;
        }
        if (id == R.id.action_help) {

        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            launchCamera(null);
        } else if (id == R.id.nav_gallery) {
            galleryBrowse(null);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            Utils.tellAboutUs(this,null);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void startAboutActivity() {
        Intent i = new Intent(this,About.class);
        startActivity(i);
    }

    public void launchCamera(View v) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utils.createImageFile(this);
            } catch (IOException ex) {
               Utils.makeToast(this,getString(R.string.create_file_error));
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.com.example.oterem.demo.fileprovider", photoFile);
                getBaseContext().grantUriPermission("com.omri.opencvdemo", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, ACTION_IMAGE_CAPTURE);
            }
        }
    }

    public void galleryBrowse(View v) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        TextView t = (TextView) findViewById(R.id.textView);
//        t.setText("");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACTION_GET_CONTENT);
    }
    /*-------------------------------------------------------------------*/

    /**
     * For handling different intents
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //histogram_btn.setEnabled(true);


            switch (requestCode) {
                case ACTION_IMAGE_CAPTURE: //in case user is taking a picture
                    try {
                        CropImage.activity(photoURI)
                                .start(this);
                    } catch (Exception e) {
                        Utils.makeToast(this,getString(R.string.create_file_error));
                    }
                    break;
//
                case ACTION_GET_CONTENT: //in case user is loading picture from gallery
                    try {
                        photoURI = data.getData();
                        CropImage.activity(photoURI).start(this);
                    } catch (Exception e) {
                        Utils.makeToast(this,getString(R.string.create_file_error));
                    }
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {//Delete the full size image after the crop
                        File fdelete = new File(photoURI.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                System.out.println("file Deleted :" + photoURI.getPath());
                            } else {
                                System.out.println("file not Deleted :" + photoURI.getPath());
                            }
                        }
                        photoURI = result.getUri();
                    }
                    break;

            }
        }
    }

    /**
     * This function creates a pop-up window with user diagnose
     * @param title - String
     * @param body - String: The content of pop up
     * @param precentage - double: The prediction of melanoma
     * @param level - int (-1,0,1) The severity of diagnose. 1-Good, 0-medium risk, (-1)-Bad
     */
    private void showPopUp(String title,String body, double precentage, int level){
        switch (level){
            case -1:
                  new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_bad)
                        .title(title)
                        .content(body)
                        .color(ContextCompat.getColor(this, R.color.diagnose_bad),0,ContextCompat.getColor(this, R.color.diagnose_bad))
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                                //TODO: this point is after getting diagnose. we save here the result (save date+ diagnose and "write" on image). and prepare the app for new cycle

                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();
                break;
            case 0:
                new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_medium_risk)
                        .title(title)
                        .content(body)
                        .color(ContextCompat.getColor(this, R.color.diagnose_meduim),0,ContextCompat.getColor(this, R.color.diagnose_meduim))
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                                //TODO: this point is after getting diagnose. we save here the result (save date+ diagnose and "write" on image). and prepare the app for new cycle

                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();
                break;


            case 1:
                  new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_good)
                        .title(title)
                        .content(body)
                        .color(ContextCompat.getColor(this, R.color.diagnose_ok),0,ContextCompat.getColor(this, R.color.diagnose_ok))
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                                //TODO: this point is after getting diagnose. we save here the result (save date+ diagnose and "write" on image). and prepare the app for new cycle

                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();
                break;

        }

    }

    private void showCase(String msg){
        View camera = findViewById(R.id.content_camera);
        View gallery = findViewById(R.id.content_gallery);
        View history = findViewById(R.id.content_history);
        View links = findViewById(R.id.content_link);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this,msg);
        //sequence.setConfig(config);

        sequence.addSequenceItem(camera,
                getResources().getString(R.string.show_case_camera_text), getResources().getString(R.string.show_case_close_window));
        sequence.addSequenceItem(gallery,
                getResources().getString(R.string.show_case_gallery_text), getResources().getString(R.string.show_case_close_window));
        sequence.addSequenceItem(history,
                getResources().getString(R.string.show_case_history_text), getResources().getString(R.string.show_case_close_window));
        sequence.addSequenceItem(links,
                getResources().getString(R.string.show_case_links_text), getResources().getString(R.string.show_case_close_window));
        sequence.start();
    }
    
}

