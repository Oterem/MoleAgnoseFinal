package com.example.oterem.demo;


import android.app.AlertDialog;
import android.content.ClipData;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.droidbyme.dialoglib.AnimUtils;
import com.droidbyme.dialoglib.DroidDialog;

import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends LoadingDialog
        implements NavigationView.OnNavigationItemSelectedListener {
    //---------------------------- static Vars --------------------------------------
    private static final int ACTION_IMAGE_CAPTURE = 1;
    private static final int ACTION_GET_CONTENT = 2;
    private static final String UPLOAD_BUCKET = "moleagnose-images";
    private static final String DOWNLOAD_BUCKET = "moleagnose-results";
    private static final String TAG = "MainActivity";
    private static final int TIME_FOR_AWS_LAMBDA = 10;
    //--------------------------End static Vars -------------------------------------
    //----------------------------- Global Vars --------------------------------------
    private Uri photoURI;
    private String imageName = "";
    private String uploadedKey = "";
    private String nameToDownload = "";
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.INTERNET}, 1);
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        }
        AWSMobileClient.getInstance().initialize(this).execute();

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
            Utils.tellAboutUs(this, null);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void startAboutActivity() {
        Intent i = new Intent(this, About.class);
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
//                imageName = photoFile.getName();
//                String[] tempStrs = imageName.split("\\.");
//                imageName = tempStrs[0];
                imageName = photoFile.getName().split("\\.")[0];
            } catch (IOException ex) {
                Utils.makeToast(this, getString(R.string.create_file_error));
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
                        Utils.makeToast(this, getString(R.string.create_file_error));
                    }
                    break;
//
                case ACTION_GET_CONTENT: //in case user is loading picture from gallery
                    try {
                        photoURI = data.getData();
                        CropImage.activity(photoURI).start(this);
                    } catch (Exception e) {
                        Utils.makeToast(this, getString(R.string.create_file_error));
                    }
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {//Delete the full size image after the crop
//                        File fdelete = new File(photoURI.getPath());
//                        if (fdelete.exists()) {
//                            if (fdelete.delete()) {
//                                System.out.println("file Deleted :" + photoURI.getPath());
//                            } else {
//                                System.out.println("file not Deleted :" + photoURI.getPath());
//                            }
//                        }
                        photoURI = result.getUri();
                        imageName = getPath(this, photoURI);
                        imageName = imageName.replaceFirst(".*/(\\w+).*", "$1");
                        UploadToS3AsyncTask job = new UploadToS3AsyncTask();
                        if(Utils.isNetworkConnected(this)){
                            job.execute(photoURI);
                        }
                        else{
                            Utils.makeToast(this,getString(R.string.no_internet_connection_message));
                        }
                    }
                    break;

            }
        }
    }


    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        Log.i("URI", uri + "");
        String result = uri + "";
        // DocumentProvider
        //  if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (isKitKat && (result.contains("media.documents"))) {
            String[] ary = result.split("/");
            int length = ary.length;
            String imgary = ary[length - 1];
            final String[] dat = imgary.split("%3A");
            final String docId = dat[1];
            final String type = dat[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
            } else if ("audio".equals(type)) {
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    dat[1]
            };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * This function creates a pop-up window with user diagnose
     *
     * @param title      - String
     * @param body       - String: The content of pop up
     * @param precentage - double: The prediction of melanoma
     * @param level      - int (-1,0,1) The severity of diagnose. 1-Good, 0-medium risk, (-1)-Bad
     */
    private void showPopUp(String title, String body, double precentage, int level) {
        switch (level) {
            case -1:
                new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_bad)
                        .title(title)
                        .content(body)
                        .color(ContextCompat.getColor(this, R.color.diagnose_bad), 0, ContextCompat.getColor(this, R.color.diagnose_bad))
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
                        .color(ContextCompat.getColor(this, R.color.diagnose_meduim), 0, ContextCompat.getColor(this, R.color.diagnose_meduim))
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
                        .color(ContextCompat.getColor(this, R.color.diagnose_ok), 0, ContextCompat.getColor(this, R.color.diagnose_ok))
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

    private void showCase(String msg) {
        View camera = findViewById(R.id.content_camera);
        View gallery = findViewById(R.id.content_gallery);
        View history = findViewById(R.id.content_history);
        View links = findViewById(R.id.content_link);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, msg);
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


    private class UploadToS3AsyncTask extends AsyncTask<Uri, Integer, Void> {

        public void uploadWithTransferUtility(String path) {
            String android_id = Settings.Secure.getString(getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                            .build();

            uploadedKey = (android_id + "_" + imageName).replace(".", "_");
            Log.i(TAG, uploadedKey);
//            TransferObserver uploadObserver =
//                    transferUtility.upload(UPLOAD_BUCKET, uploadedKey + ".jpg", new File(path));
            TransferObserver uploadObserver = transferUtility.upload(UPLOAD_BUCKET,uploadedKey+".jpg",new File(path));

            // Attach a listener to the observer to get state update and progress notifications
            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        Log.d("YourActivity", "Upload Complete");


                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;


                    Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.d(TAG, "Error in upload");
                }

            });

            // If you prefer to poll for the data, instead of attaching a
            // listener, check for the state and progress in the observer.
            if (TransferState.COMPLETED == uploadObserver.getState()) {
                Log.d("YourActivity", "Upload Complete");

            }
//        Toast.makeText(getApplicationContext(), "Upload to server completed", Toast.LENGTH_LONG);
            Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
            Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());

        }
        /**
         * Set the view before image process.
         */
        @Override
        protected void onPreExecute() {
            showProgressDialog(getString(R.string.progressDialog_uploading_image));
        }

        @Override
        protected void onPostExecute(Void Void) {
            hideProgressDialog();
            DownloadFromS3AsyncTask myWork = new DownloadFromS3AsyncTask();
            myWork.execute();
        }

        @Override
        protected Void doInBackground(Uri... Uri) {

            String path = getPath(getApplicationContext(), Uri[0]);
            Log.i("OT:",path);
            uploadWithTransferUtility(path);
            for(int i=0;i<TIME_FOR_AWS_LAMBDA;i++){
                try {
                    Thread.sleep(1000);

                }catch (Exception e){
                    e.printStackTrace();;
                }
            }
            return null;
        }
    }
    private class DownloadFromS3AsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            downloadFromS3();
            return null;
        }

        @Override
        protected void onPreExecute()
        {

            showProgressDialog(getString(R.string.progressDialog_downloading_image));}

        @Override
        protected void onPostExecute(Void aVoid) {}
    }

    public void downloadFromS3(){

        File f = Utils.getTempFile(getApplicationContext());
        nameToDownload = f.getName();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        Log.i(TAG, "OT3: Downloading from s3 "+nameToDownload);
        Log.i(TAG, "key is: "+uploadedKey+"omri.json");
        TransferObserver downloadObserver =
                transferUtility.download(DOWNLOAD_BUCKET,uploadedKey+".json",f);

        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.i(TAG, "OT3: Download complete");
                    String cachePath = getCacheDir().getPath();
                    File myDisk = new File(cachePath);
                    File json_string = new File(myDisk+File.separator+nameToDownload);
                    String res = "";
                    FileInputStream fis = null;
                    JSONObject json = null;
                    try {
                        //f = new BufferedInputStream(new FileInputStream(filePath));
                        //f.read(buffer);

                        fis = new FileInputStream(json_string);
                        char current;
                        while (fis.available() > 0) {
                            current = (char) fis.read();
                            res = res + String.valueOf(current);
                        }
                    } catch (Exception e) {
                        Log.d("TourGuide", e.toString());
                    } finally {
                        if (fis != null)
                            try {
                                fis.close();
                            } catch (IOException ignored) {
                                Toast.makeText(getApplicationContext(),"Error parsing json", Toast.LENGTH_LONG);
                            }
                    }
                    try{
                        json = new JSONObject(res);
                        JSONObject bigger = json.getJSONObject("bigger");
                        String name = bigger.getString("name");
                        double val = bigger.getDouble("value");
                        Log.i(TAG, "========Final Score===========");
                        Log.i(TAG, "OT: "+name+", "+val);
                        hideProgressDialog();

                        /*----------------Call for pop up diagnose-------------------------*/
                        showPopUp("Diagnose", "your diagnose is:"+name+". result: "+val,val,0);
                        /*---------------------------------------*/
                        Toast.makeText(getApplicationContext(),name+": "+val+"%",Toast.LENGTH_LONG);
                        Log.i(TAG, "========End of Final Score===========");

                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Error parsing json1", Toast.LENGTH_LONG);

                    }

                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });


    }






}
