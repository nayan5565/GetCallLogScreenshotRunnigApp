package com.example.nayan.appanalysis2;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nayan.appanalysis2.forgroundApp.ForegroundToastService;

/**
 * Created by ASUS on 1/6/2018.
 */

public class SubmitActivity extends AppCompatActivity implements View.OnClickListener {
    private Button submit;
    private EditText edt;
    private String pass;
    private int STORAGE_PERMISSION_CODE = 23;
    private static final int REQUEST_ID = 1;
    private Button btUsagePermission;
    private TextView tvPermission;
    private static SubmitActivity instance;

    public static SubmitActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_activity);
        instance = this;
        pass = "h82a12";
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(this);
        edt = (EditText) findViewById(R.id.edt);


        btUsagePermission = (Button) findViewById(R.id.usage_permission);
        tvPermission = (TextView) findViewById(R.id.permission_text);

//        requestPermission();
        requestStoragePermissionToMashmallow();


        if (!needsUsageStatsPermission()) {
            btUsagePermission.setVisibility(View.GONE);
            submit.setVisibility(View.VISIBLE);
            edt.setVisibility(View.VISIBLE);
            tvPermission.setText(R.string.usage_permission_granted);
//            ForegroundToastService.start(getBaseContext());
            ForegroundToastService.start(getApplicationContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            edt.setVisibility(View.GONE);
            btUsagePermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUsageStatsPermission();
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!needsUsageStatsPermission()) {
            btUsagePermission.setVisibility(View.GONE);
            submit.setVisibility(View.VISIBLE);
            edt.setVisibility(View.VISIBLE);
            tvPermission.setText(R.string.usage_permission_granted);
//            ForegroundToastService.start(getBaseContext());
            ForegroundToastService.start(getApplicationContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            edt.setVisibility(View.GONE);
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
            submit.setVisibility(View.VISIBLE);
            edt.setVisibility(View.VISIBLE);
            tvPermission.setText(R.string.usage_permission_granted);
//            ForegroundToastService.start(getBaseContext());
            ForegroundToastService.start(getApplicationContext());
//            Toast.makeText(getBaseContext(), getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        } else {
            btUsagePermission.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            edt.setVisibility(View.GONE);
            btUsagePermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUsageStatsPermission();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onactivityResult", " ");
        if (requestCode == REQUEST_ID)
            ScreenshotManager.INSTANCE.onActivityResult(resultCode, data, SubmitActivity.this);

    }

    @Override
    public void onClick(View view) {
        if (edt.getText().toString().equals(pass)) {
            startActivity(new Intent(SubmitActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(SubmitActivity.this, "wrong pass", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScreenshotManager.INSTANCE.requestScreenshotPermission(this, REQUEST_ID);
        }
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
}
