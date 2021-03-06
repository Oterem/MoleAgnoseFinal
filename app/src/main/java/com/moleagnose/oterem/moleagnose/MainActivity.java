package com.moleagnose.oterem.moleagnose;


import android.app.Dialog;

import android.content.Intent;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.droidbyme.dialoglib.AnimUtils;
import com.droidbyme.dialoglib.DroidDialog;

import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;



public class MainActivity extends LoadingDialog
        implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.deleteCache(getApplicationContext());

    }

    //---------------------------- static Vars --------------------------------------
    private static final int ACTION_IMAGE_CAPTURE = 1;
    private static final int ACTION_GET_CONTENT = 2;
    private static final String UPLOAD_BUCKET = "moleagnose-images";
    private static final String DOWNLOAD_BUCKET = "moleagnose-results";
    private static final String TAG = "MainActivity";
    private static final int TIME_FOR_AWS_LAMBDA = 10;
    private static final int LOW_BOUND = 30;
    private static final int HIGH_BOUND = 70;
    private static String pathOfFinalImage = "";
    //--------------------------End static Vars -------------------------------------
    //----------------------------- Global Vars --------------------------------------
    private Uri photoURI;
    private String imageName = "";
    private String imageExtension = "";
    private String uploadedKey = "";
    private String nameToDownload = "";
    private String helpUrl = "";
    private RequestQueue mQueue;
    private static ArrayList<String>names;
    private static ArrayList<String>urls;
    private static ArrayList<String>imageUrls;
    private JSONArray jsonArray;
    //--------------------------END Global var --------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(this);
        jsonParse();
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //These ArrayLists will store data coming from JSON
        names = new ArrayList<>();
        urls = new ArrayList<>();
        imageUrls = new ArrayList<>();


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
        showDisclaimer();

    }

    /**
     * This function downloads link.json from AWS and insert relevant data to
     * the relevant arrays.
     */
    private void jsonParse(){
        String url = getResources().getString(R.string.links_json_url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    jsonArray = response.getJSONArray(getResources().getString(R.string.json_array_name));
                    helpUrl = response.getString("helpUrl");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject link = jsonArray.getJSONObject(i);
                        String name = link.getString("name");
                        String url = link.getString("url");
                        String imageUrl = link.getString("image");

                        Log.i(TAG,name+" "+url+" "+imageUrl);
                        names.add(name);
                        urls.add(url);
                        imageUrls.add(imageUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
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
        showCase("DummyString");//activate showCase
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
            Intent i = new Intent(Intent.ACTION_VIEW);
            //We check if the url downloaded from JSON in aws. if not, we switch to backup
            if(helpUrl.equals("")){
                helpUrl = getResources().getString(R.string.backup_help_url);
            }
            i.setData(Uri.parse(helpUrl));
            startActivity(i);
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
        } else if (id == R.id.nav_history) {
            galleryActivity(null);

        } else if (id == R.id.nav_links) {

            startLinksActivity(null);


        } else if (id == R.id.nav_share) {
            Utils.tellAboutUs(this, null);
        } else if (id == R.id.nav_send) {
            Utils.sendMail(this);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void startAboutActivity() {
        Intent i = new Intent(this, About.class);
        startActivity(i);
    }
    public void startLinksActivity(View v){
        if(names.isEmpty()){
            if(Utils.isNetworkConnected(getApplicationContext())){
                jsonParse();
            }
            Utils.makeToast(getApplicationContext(),getResources().getString(R.string.internet_error_loading_links));
        }
        Intent i = new Intent(this, ListViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("names",names);
        bundle.putStringArrayList("urls",urls);
        bundle.putStringArrayList("imageUrls",imageUrls);
        bundle.putString("helpUrl",helpUrl);
        i.putExtras(bundle);
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
                imageName = photoFile.getName().split("\\.")[0];
            } catch (IOException ex) {
                Utils.makeToast(this, getString(R.string.create_file_error));
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.moleagnose.oterem.moleagnose.fileprovider", photoFile);
                getBaseContext().grantUriPermission("com.moleagnose.oterem.moleagnose.fileprovider",photoURI,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, ACTION_IMAGE_CAPTURE);
            }
        }
    }


    public void galleryBrowse(View v) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
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
                        photoURI = result.getUri();
                        imageName = Utils.getPath(this, photoURI);
                        imageExtension = imageName.substring(imageName.lastIndexOf(".") + 1);
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

    /***
     * This function opens a share functionality
     * @param path - The path of the diagnosed image, The path is required for the file provider
     */
    private void shareDiagnose(String path){
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        File file = new File(path);
        Uri u = FileProvider.getUriForFile(this,"com.moleagnose.oterem.moleagnose.fileprovider",file);
        shareIntent.setDataAndType(u,"image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, u);
        startActivity(shareIntent);
    }


    /**
     * This function creates a pop-up window with user diagnose
     *
     *
     *
     * @param precentage - double: The prediction of melanoma
     * @param level      - int (-1,0,1) The severity of diagnose. 1-Good, 0-medium risk, (-1)-Bad
     */
    private void showPopUp(double precentage, int level) {
        switch (level) {
            case -1:
                pathOfFinalImage = Utils.addWaterMarkandSave(getApplicationContext(),photoURI,getString(R.string.text_on_image_result_val_minus_one));
                new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_bad)
                        .title(getResources().getString(R.string.pop_up_title))
                        .content(getResources().getString(R.string.pop_up_body_bad))
                        .color(ContextCompat.getColor(this, R.color.diagnose_bad), 0, ContextCompat.getColor(this, R.color.diagnose_bad))
                        .negativeButton(getResources().getString(R.string.share), new DroidDialog.onNegativeListener() {
                            @Override
                            public void onNegative(Dialog dialog) {
                                shareDiagnose(pathOfFinalImage);
                                dialog.dismiss();
                            }
                        })
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();

                break;
            case 0:
                pathOfFinalImage = Utils.addWaterMarkandSave(getApplicationContext(),photoURI,getString(R.string.text_on_image_result_val_zero));
                new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_medium_risk)
                        .title(getResources().getString(R.string.pop_up_title))
                        .content(getResources().getString(R.string.pop_up_body_medium))
                        .color(ContextCompat.getColor(this, R.color.diagnose_meduim), 0, ContextCompat.getColor(this, R.color.diagnose_meduim))
                        .negativeButton(getResources().getString(R.string.share), new DroidDialog.onNegativeListener() {
                            @Override
                            public void onNegative(Dialog dialog) {
                                shareDiagnose(pathOfFinalImage);
                                dialog.dismiss();
                            }
                        })
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();

                break;


            case 1:
                pathOfFinalImage = Utils.addWaterMarkandSave(getApplicationContext(),photoURI,getString(R.string.text_on_image_result_val_one));
                new DroidDialog.Builder(this)
                        .icon(R.drawable.diagnose_good)
                        .title(getResources().getString(R.string.pop_up_title))
                        .content(getResources().getString(R.string.pop_up_body_good))
                        .color(ContextCompat.getColor(this, R.color.diagnose_ok), 0, ContextCompat.getColor(this, R.color.diagnose_ok))
                        .negativeButton(getResources().getString(R.string.share), new DroidDialog.onNegativeListener() {
                            @Override
                            public void onNegative(Dialog dialog) {
                                shareDiagnose(pathOfFinalImage);
                                dialog.dismiss();
                            }
                        })
                        .positiveButton(getResources().getString(R.string.pop_up_close_window), new DroidDialog.onPositiveListener() {
                            @Override
                            public void onPositive(Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .animation(AnimUtils.AnimFadeInOut)
                        .show();
                break;

        }

    }

    private void showDisclaimer(){
        new DroidDialog.Builder(this)
                .icon(R.drawable.info_logo)
                .title(getResources().getString(R.string.show_case_title_disclaimer))
                .content(getResources().getString(R.string.show_case_disclaimer))
                .color(ContextCompat.getColor(this, R.color.header_color), 0, ContextCompat.getColor(this, R.color.header_color))
                .neutralButton(getResources().getString(R.string.show_case_close_disclaimer), new DroidDialog.onNeutralListener() {
                    @Override
                    public void onNeutral(Dialog dialog) {
                        dialog.dismiss();
                    }
                })
                .animation(AnimUtils.AnimFadeInOut)
                .show();
    }

    private void showCase(String msg) {

        View camera = findViewById(R.id.content_camera);
        View gallery = findViewById(R.id.content_gallery);
        View history = findViewById(R.id.content_history);
        View links = findViewById(R.id.content_link);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, msg);

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
            Log.i(TAG,imageName);
            String android_id = Settings.Secure.getString(getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                            .build();

            uploadedKey = (android_id + "_" + imageName).replace(".", "_");
            Log.i(TAG, "uploadKey is: "+uploadedKey);
            transferUtility.upload(UPLOAD_BUCKET, uploadedKey + "." + imageExtension, new File(path));
            TransferObserver uploadObserver = transferUtility.upload(UPLOAD_BUCKET,uploadedKey + "." + imageExtension,new File(path));

            // Attach a listener to the observer to get state update and progress notifications
            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        Log.d("YourActivity", "Upload Complete");
                        DownloadFromS3AsyncTask myWork = new DownloadFromS3AsyncTask();
                        myWork.execute();
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
        protected void onPostExecute(Void Void) {}

        @Override
        protected Void doInBackground(Uri... Uri) {

            String path = Utils.getPath(getApplicationContext(), Uri[0]);
            Log.i(TAG,"starting uploadAsyncTask - do in background");
            uploadWithTransferUtility(path);
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
            hideProgressDialog();
            showProgressDialog(getString(R.string.progressDialog_downloading_image));}

        @Override
        protected void onPostExecute(Void aVoid) {}
    }

    public void downloadFromS3(){

        try{
            Thread.sleep(1000*TIME_FOR_AWS_LAMBDA);
        }catch (Exception e){
            e.printStackTrace();
        }

        File f = Utils.getTempFile(getApplicationContext());
        nameToDownload = f.getName();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        Log.i(TAG, "Downloading from s3 "+uploadedKey + "_" + imageExtension +".json");
        TransferObserver downloadObserver =
                transferUtility.download(DOWNLOAD_BUCKET,uploadedKey + "_" + imageExtension + ".json",f);

        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.i(TAG, "Download complete");
                    String cachePath = getCacheDir().getPath();
                    File myDisk = new File(cachePath);
                    File json_string = new File(myDisk+File.separator+nameToDownload);
                    String res = "";
                    FileInputStream fis = null;
                    JSONObject json;
                    try {


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
                        double melanoma = json.getDouble("melanoma");
                        int diagnose = Utils.decideDiagnose(melanoma,LOW_BOUND,HIGH_BOUND);
                        showPopUp(0.0,diagnose);
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
                hideProgressDialog();
                Utils.makeToast(getApplicationContext(),getResources().getString(R.string.aws_error_download));
                ex.printStackTrace();

            }
        });


    }


    public void galleryActivity(View v) {
        ArrayList<String> imagesList = Utils.resultImageList(getApplicationContext());
        if(!imagesList.isEmpty()) {
            ZGallery.with(this, imagesList)
                    .setToolbarTitleColor(ZColor.WHITE)
                    .setGalleryBackgroundColor(ZColor.WHITE)
                    .setToolbarColorResId(R.color.header_color)
                    .setTitle(getResources().getString(R.string.history_activity_title))
                    .show();
        }
        else{
            Utils.makeToast(this,getString(R.string.no_image_in_history_to_show));
        }
    }



}
