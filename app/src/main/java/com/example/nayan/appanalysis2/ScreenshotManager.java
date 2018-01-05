package com.example.nayan.appanalysis2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.nayan.appanalysis2.database.DBManager;
import com.example.nayan.appanalysis2.database.MImage;
import com.example.nayan.appanalysis2.forgroundApp.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Dev on 1/1/2018.
 */

public class ScreenshotManager {
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    public static final ScreenshotManager INSTANCE = new ScreenshotManager();
    private Intent mIntent;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String TAG = ScreenshotManager.class.getName();
    public MediaProjection mediaProjection;
    private MImage mImage;
    private ArrayList<MImage> mImages;

    private ScreenshotManager() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestScreenshotPermission(@NonNull Activity activity, int requestId) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), requestId);
    }


    public void onActivityResult(int resultCode, Intent data, Context context) {
        if (resultCode == Activity.RESULT_OK && data != null)
            mIntent = data;
        else mIntent = null;
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
            File storeDirectory = new File(STORE_DIRECTORY);
            if (!storeDirectory.exists()) {
                boolean success = storeDirectory.mkdirs();
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.");
                    return;
                }
            }
        } else {
            Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @UiThread
    public boolean takeScreenshot(@NonNull final Context context) {
        if (mIntent == null)
            return false;
        final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mediaProjection == null) {

        } else {
            mediaProjection.stop();

        }
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mIntent);

        if (mediaProjection == null)
            return false;
        final int density = context.getResources().getDisplayMetrics().densityDpi;
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int width = size.x, height = size.y;

//        final Point windowSize = new Point();
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        windowManager.getDefaultDisplay().getRealSize(windowSize);

        // start capture reader
        final ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        final VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(SCREENCAP_NAME, width, height, density, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, null);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(final ImageReader reader) {
                Log.e("AppLog", "onImageAvailable");
                if (mediaProjection == null) {

                } else {
                    mediaProjection.stop();
                }

                        Image image = null;
                        Bitmap bitmap = null;
                        FileOutputStream fos = null;
                        File externalFilesDir = null;
                        try {
                            image = reader.acquireLatestImage();
                            if (image != null) {
                                Image.Plane[] planes = image.getPlanes();
                                ByteBuffer buffer = planes[0].getBuffer();
                                int pixelStride = planes[0].getPixelStride(), rowStride = planes[0].getRowStride(), rowPadding = rowStride - pixelStride * width;
                                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                                bitmap.copyPixelsFromBuffer(buffer);
                                // write bitmap to a file
                                String now = Utils.getToday();
                                fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + now + ".png");
                                mImage = new MImage();
                                mImage.setImgName("/myscreen_" + now + ".png");

                               long id= DBManager.getInstance().addImage(mImage);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fos);
                                getImage(context, mImage.getImgName(),id);
                                IMAGES_PRODUCED++;
                                Log.e(TAG, "captured image: " + now);

                            }
                        } catch (Exception e) {
                            if (bitmap != null)
                                bitmap.recycle();
                            e.printStackTrace();
                        }
                        if (image != null)
                            image.close();
                        reader.close();

                    }
        }, null);

        mediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Log.e("ScreenCapture", "stopping projection.");
                if (virtualDisplay != null)
                    virtualDisplay.release();
                imageReader.setOnImageAvailableListener(null, null);
                mediaProjection.unregisterCallback(this);
                mediaProjection = null;
            }
        }, null);
        return true;
    }

    private void updateUi(final String message) {

    }

    public void getImage(Context context, String image,long id) {
        File externalFilesDir = MainApplication.context.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + image;
        File file = new File(path);
        if (file.exists()) {
//            Toast.makeText(context, "file exists", Toast.LENGTH_SHORT).show();
            Log.e("TEST", "FILE EXISTS");
            sendImageToServer(context, file,id,image);
        } else {
//            Toast.makeText(context, "file is not exists", Toast.LENGTH_SHORT).show();
            Log.e("TEST", "FILE NOT EXISTS");
        }
    }

    public void sendImageToServer(final Context context, File file, final long id, final String image) {
        if (!Utils.isInternetOn())
            return;
        Log.e("TEST", "call server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("app_name", R.string.app_name);
        params.put("duration", "");
        try {
            params.put("screenshot", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post("http://www.swapnopuri.com/app/calllog/api/upload_screenshot/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.e("Image", response.toString());
                    if (response.has("status") && response.getString("status").equals("success")) {
                        Log.e("Image", "  uploaded");
                        deleteImageFromSdcard(id,image);
//                        Toast.makeText(context, "image uploaded", Toast.LENGTH_SHORT).show();


                    } else {
                        Log.e("Image", " not uploaded");
                        Toast.makeText(context, "image uploaded failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("IMAGE", responseString);
            }
        });
    }

    private void deleteImageFromSdcard(long id,String image) {
//        mImages = DBManager.getInstance().getAllImage();

        File externalFilesDir = MainApplication.context.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + image;
        Log.e("file  :", " " + path + image);
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                DBManager.getInstance().deleteData(DBManager.TABLE_DOWNLOAD, "id",id+"");
                Log.e("file Deleted :", " " + path + image);
            } else {
                Log.e("file Not Deleted :", " " + path + image);
            }
        }
    }

//    public void screen() {
//        Date now = new Date();
//        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//        Process sh = null;
//        try {
//            sh = Runtime.getRuntime().exec("su", null, null);
//            OutputStream os = sh.getOutputStream();
//            os.write(("/system/bin/screencap -p " + "/sdcard/img.png").getBytes("ASCII"));
//            os.flush();
//
//            os.close();
//            sh.waitFor();
//
//            Bitmap screen = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() +
//                    File.separator + "img.png");
//
////my code for saving
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            screen.compress(Bitmap.CompressFormat.JPEG, 15, bytes);
//
////you can create a new file name "test.jpg" in sdcard folder.
//
//            File f = new File(Environment.getExternalStorageDirectory() + File.separator + now + "test.jpg");
//            f.createNewFile();
////write the bytes in file
//            FileOutputStream fo = new FileOutputStream(f);
//            fo.write(bytes.toByteArray());
//// remember close de FileOutput
//
//            fo.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
}
