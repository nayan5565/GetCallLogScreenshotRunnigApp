package com.example.nayan.appanalysis2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenshotActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 23;
    private int shot = 0;
    private LinearLayout rootContent;
    private Handler handler;


    private static ScreenshotActivity instance;

    //screenshot code

    private static final String TAG = ScreenshotActivity.class.getName();
    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    public int condition = 0;
    private ScreenshotActivity.OrientationChangeCallback mOrientationChangeCallback;

    public static ScreenshotActivity getInstance() {


        return instance;
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("condition", " is " + condition);
//                    if (condition == 0) {
//
//                    }
                    if (condition == 1) {
                        Image image = null;
                        FileOutputStream fos = null;
                        Bitmap bitmap = null;

                        try {
                            image = reader.acquireLatestImage();
//                            if (image != null) {
                                Image.Plane[] planes = image.getPlanes();
                                ByteBuffer buffer = planes[0].getBuffer();
                                int pixelStride = planes[0].getPixelStride();
                                int rowStride = planes[0].getRowStride();
                                int rowPadding = rowStride - pixelStride * mWidth;

                                // create bitmap
                                bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                                bitmap.copyPixelsFromBuffer(buffer);

                                // write bitmap to a file
                                fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fos);

                                IMAGES_PRODUCED++;
                                Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
//                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }
                            }

                            if (bitmap != null) {
                                bitmap.recycle();
                            }

                            if (image != null) {
                                image.close();
                            }
                        }
                    }


                }
            }, 10000);

        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(ScreenshotActivity.MediaProjectionStopCallback.this);
                }
            });
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        instance = this;

//        mHandler = new Handler();

        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

//        start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (sMediaProjection != null) {
                File externalFilesDir = getExternalFilesDir(null);
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

                // display metrics
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new ScreenshotActivity.OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                sMediaProjection.registerCallback(new ScreenshotActivity.MediaProjectionStopCallback(), mHandler);
            }
        }
        this.finish();
    }

    /****************************************** UI Widget Callbacks *******************************/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startProjection() {
        condition = 1;
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        Log.e("Screenshot: ", "start project ");


//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startProjection();
//            }
//        }, 60000);

    }

    public void stopProjection() {
        mHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    /****************************************** Factoring Virtual Display creation ****************/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 1);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);


        mImageReader.setOnImageAvailableListener(new ScreenshotActivity.ImageAvailableListener(), mHandler);
    }


    public Bitmap getScreenShot(View view) {
        View screenView = getWindow().getDecorView().getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

//    private void screen() {
//        Toast.makeText(this, "screenshot", Toast.LENGTH_SHORT).show();
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
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                screen();
//
//            }
//        }, 60000);
//    }

//    private void takeScreenshot() {
//        shot++;
//        Bitmap b = null;
//
//        b = getScreenShot(rootContent);
//
//        //If bitmap is not null
//        if (b != null) {
//
//            File saveFile = ScreenshotUtils.getMainDirectoryName(this);//get the path to save screenshot
//            File file = ScreenshotUtils.store(b, "screenshot" + shot + ".jpg", saveFile);//save the screenshot to selected path
//            Toast.makeText(this, R.string.screenshot_take_success, Toast.LENGTH_SHORT).show();
////            shareScreenshot(file);//finally share screenshot
//        } else
//            //If bitmap is null show toast message
//            Toast.makeText(this, R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                takeScreenshot();
//
//            }
//        }, 60000);
//    }

    /*  Show screenshot Bitmap */
//    private void showScreenShotImage(Bitmap b) {
////        imageView.setImageBitmap(b);
//    }

    /*  Share Screenshot  */
    private void shareScreenshot(File file) {
        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text));
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    private void requestStoragePermissionToMashmallow() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Utils.toastMassage(this, "Permission granted now you can read the storage");
            } else {
//                Utils.toastMassage(this, "Oops you just denied the permission");
            }
        }
    }


    //code for forground app

    private boolean needsUsageStatsPermission() {
        return postLollipop() && !hasUsageStatsPermission(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private boolean postLollipop() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Ondestr", " ondestroy screenshot");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
