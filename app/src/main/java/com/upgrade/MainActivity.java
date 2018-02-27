package com.upgrade;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.upgradelibrary.UpgradeHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final UpgradeHelper helper = new UpgradeHelper(MainActivity.this);
        findViewById(R.id.button_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.checkForUpdates("http://www.rainen.cn/test/app-update.xml", false);
            }
        });
        findViewById(R.id.button_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.cancel();
            }
        });
    }
}
