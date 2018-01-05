package com.example.nayan.appanalysis2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.nayan.appanalysis2.database.MImage;
import com.example.nayan.appanalysis2.forgroundApp.ForegroundToastService;
import com.example.nayan.appanalysis2.fragment.HomeFragment;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 23;
    private Button btUsagePermission;
    private TextView tvPermission;
    private static final int REQUEST_ID = 1;
    private ArrayList<MImage> mImages;
    private static MainActivity instance;
    Handler handler;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContactsProvider contactsProvider = new ContactsProvider(getApplicationContext());
        Data<Contact> contacts = contactsProvider.getContacts();

        TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
        Data<Sms> sms=provider.getSms(TelephonyProvider.Filter.ALL);


        CallsProvider callsProvider = new CallsProvider(getApplicationContext());
        Data<Call> callData= callsProvider.getCalls();


//        Gson gson=new Gson();
//        String json= gson.toJson(sms.getList());
//        Log.e("SMS",json);

        Gson gson=new Gson();
        String json= gson.toJson(sms.getList());
        JSONObject j=new JSONObject();
        try {
            j.put("Call",json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Call",json);

        instance = this;
        handler = new Handler();
        mImages = new ArrayList<>();
//        requestPermission();

//        mImages = DBManager.getInstance().getAllImage();
        if (mImages.size() > 0) {
            Log.e("Image", " size " + mImages.size() + " name" + mImages.get(0).getImgName());
//            deleteImageFromSdcard();
        }

        btUsagePermission = (Button) findViewById(R.id.usage_permission);
        tvPermission = (TextView) findViewById(R.id.permission_text);
        requestStoragePermissionToMashmallow();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ScreenshotManager.INSTANCE.requestScreenshotPermission(this, REQUEST_ID);
//        }

        setSupportActionBar(toolbar);
        setFragment(HomeFragment.class);
//        whatsApp();


        if (!needsUsageStatsPermission()) {
            btUsagePermission.setVisibility(View.GONE);
            tvPermission.setText(R.string.usage_permission_granted);
            ForegroundToastService.start(getApplicationContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUsageStatsPermission();
                }
            });
        }

//        startActivity(new Intent(this, ScreenshotActivity.class));
    }

    private void deleteImageFromSdcard() {
        File externalFilesDir = this.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/screenshots/" + mImages.get(21).getImgName();
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                Log.e("file Deleted :", " " + path + mImages.get(21).getImgName());
            } else {
                Log.e("file Not Deleted :", " " + path + mImages.get(21).getImgName());
            }
        }
    }

    public void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScreenshotManager.INSTANCE.requestScreenshotPermission(this, REQUEST_ID);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ID)
            ScreenshotManager.INSTANCE.onActivityResult(resultCode, data, MainActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!needsUsageStatsPermission()) {
            btUsagePermission.setVisibility(View.GONE);
            tvPermission.setText(R.string.usage_permission_granted);
//            ForegroundToastService.start(getBaseContext());
            ForegroundToastService.start(getApplicationContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setVisibility(View.VISIBLE);
            btUsagePermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUsageStatsPermission();
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!needsUsageStatsPermission()) {
            btUsagePermission.setVisibility(View.GONE);
            tvPermission.setText(R.string.usage_permission_granted);
            ForegroundToastService.start(getApplicationContext());
//            ForegroundToastService.start(getBaseContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setVisibility(View.VISIBLE);
            btUsagePermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUsageStatsPermission();
                }
            });
        }
    }


//
//    private void whatsApp() {
//        PackageManager pm = getPackageManager();
//        try {
//
//            Intent waIntent = new Intent(Intent.ACTION_SEND);
//            waIntent.setType("text/plain");
//            String text = "YOUR TEXT HERE";
//
//            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
//            //Check if package exists or not. If not then code
//            //in catch block will be called
//            waIntent.setPackage("com.whatsapp");
//
//            waIntent.putExtra(Intent.EXTRA_TEXT, text);
//            startActivity(Intent.createChooser(waIntent, "Share with"));
//
//        } catch (PackageManager.NameNotFoundException e) {
//            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
//
//    public Bitmap getScreenShot(View view) {
//        View screenView = getWindow().getDecorView().getRootView();
//        screenView.setDrawingCacheEnabled(true);
//        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
//        screenView.setDrawingCacheEnabled(false);
//        return bitmap;
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
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_SMS,
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

    private void setFragment(Class<? extends Fragment> fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment_container, fragment.newInstance());
            fragmentTransaction.commit();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_settings:
//                return true;
        }

        return super.onOptionsItemSelected(item);
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


}
