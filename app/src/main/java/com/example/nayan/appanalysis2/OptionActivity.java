package com.example.nayan.appanalysis2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Dev on 12/31/2017.
 */

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnForground, btnCallLog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        btnCallLog = (Button) findViewById(R.id.btnCallLog);
        btnCallLog.setOnClickListener(this);
        btnForground = (Button) findViewById(R.id.btnForground);
        btnForground.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnCallLog) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (view.getId() == R.id.btnForground){
            startActivity(new Intent(this, ForGroundAppActivity.class));
        }
    }
}
