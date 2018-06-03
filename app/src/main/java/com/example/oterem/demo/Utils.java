package com.example.oterem.demo;



import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.support.v4.content.ContextCompat.startActivity;

public abstract class Utils {
//    Bitmap src, String watermark, Point location, int color, int alpha, int size, boolean underline) {

        //-------Image water mark parameters-------
    private static final Point TEXT_LOCATION_ON_IMAGE = new Point (20,40);
    private static final int TEXT_COLOR_ON_IMAGE = Color.GREEN;
    private static final int TEXT_ALPHA_ON_IMAGE = 220;// text Transparency Level (val from 0 to 255)
    private static final int TEXT_SIZE_ON_IMAGE = 36;
    private static final boolean TEXT_UNDRTLINE_ON_IMAGE = false;
    private static final String IMAGES_RESULTS_FOLDER_NAME = "/moleAgnoseResults";
    private static final String IMAGES_RESULTS_FOLDER_DIR = Environment.getExternalStorageDirectory().toString() +IMAGES_RESULTS_FOLDER_NAME;
    //-------Image water mark parameters-------


    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        String imageName = imageFileName;
        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void tellAboutUs(Context context, View v){
        String str = context.getString(R.string.tellAboutUs_message);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, str);
        sharingIntent.setType("text/plain");
        context.startActivity(sharingIntent);
    }

    public static void makeToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
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

    public static File getTempFile(Context context) {
        File file = new File("");
        String name;
        try {
            String fileName = "omri";
            file = File.createTempFile(fileName, ".json", context.getCacheDir());
            String path = file.getAbsolutePath();
            name = parseImageName(path);

        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }

    private static String parseImageName(String path) {
        String[] tokens = path.split("/");
        String name = tokens[tokens.length - 1];
        String n = name.replace(".jpg", "");
        return name;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static int decideDiagnose(double mel, double low, double high){
        if(mel<(low/100))//no melanoma at all
            return 1;
        else if(mel<(high/100))
            return 0;
        else
            return -1;
    }

    public static void sendMail(Context context){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{context.getResources().getString(R.string.about_email)});
        i.putExtra(Intent.EXTRA_SUBJECT, "I want to report a bug");
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void addWaterMarkandSave(Context context,Uri uri,String diag){
        Bitmap bitmap=null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateFormat df = new SimpleDateFormat(context.getString(R.string.text_on_image_result_date_pattern));
        String date = df.format(Calendar.getInstance().getTime());
        String textOnImage = date+"\n\n"+diag;



        Bitmap newBit=mark(bitmap,textOnImage,TEXT_LOCATION_ON_IMAGE , TEXT_COLOR_ON_IMAGE , TEXT_ALPHA_ON_IMAGE , TEXT_SIZE_ON_IMAGE , TEXT_UNDRTLINE_ON_IMAGE);
        saveImage(newBit,"test");
    }


    public static Bitmap mark(Bitmap src, String watermark, Point location, int color, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);

        return result;
    }

    private static void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root+"/moleAgnoseResults");
        myDir.mkdirs();
        DateFormat df = new SimpleDateFormat("ddMMyyyy-HHmmss");
        String date = df.format(Calendar.getInstance().getTime());
        String fname = "moleAgonse-" + date+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<String> resultImageList(){
        ArrayList<String> imagesList = new ArrayList<>();
        File f = new File(IMAGES_RESULTS_FOLDER_DIR);
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            for (File inFile : files) {
                if (inFile.exists()) {
                    imagesList.add(inFile.getAbsolutePath());
                    String test = inFile.getAbsolutePath();
                }
            }
        }


        return imagesList;
    }
}
