package com.upgrade;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.upgradelibrary.UpgradeManager;
import com.upgradelibrary.UpgradeServiceManager;
import com.upgradelibrary.bean.Upgrade;
import com.upgradelibrary.bean.UpgradeOptions;
import com.upgradelibrary.service.UpgradeService;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final UpgradeManager manager = new UpgradeManager(this);
        findViewById(R.id.button_check_updates_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.checkForUpdates(new UpgradeOptions.Builder()
                        .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                        .setTitle("腾讯QQ")
                        .setDescription("更新通知栏")
                        .setUrl("http://www.rainen.cn/test/app-update.xml")
                        .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                        .setMutiThreadEnabled(true)
                        .setMaxThreadPools(1)
                        .setMd5(null)
                        .build(), false);
            }
        });

        findViewById(R.id.button_check_updates_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.checkForUpdates(new UpgradeOptions.Builder()
                        .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                        .setTitle("腾讯QQ")
                        .setDescription("更新通知栏")
                        .setUrl("http://www.rainen.cn/test/app-update.xml")
                        .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                        .setMutiThreadEnabled(true)
                        .setMaxThreadPools(1)
                        .setMd5(null)
                        .build(), new UpgradeManager.OnUpgradeListener() {
                    @Override
                    public void onUpdateAvailable(Upgrade upgrade, final UpgradeServiceManager manager) {
                        StringBuffer logs = new StringBuffer();
                        for (int i = 0; i < upgrade.getLogs().size(); i++) {
                            logs.append(upgrade.getLogs().get(i));
                            logs.append(i < upgrade.getLogs().size() - 1 ? "\n" : "");
                        }
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("发现新版本 v" + upgrade.getVersionName() + " - " + upgrade.getDate())
                                .setMessage(logs.toString())
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.setOnBinderUpgradeServiceLisenter(new UpgradeServiceManager.OnBinderUpgradeServiceLisenter() {
                                    @Override
                                    public void onBinder(UpgradeService upgradeService) {
                                        upgradeService.setOnDownloadListener(new UpgradeService.OnDownloadListener() {
                                            @Override
                                            public void onProgress(long progress, long maxProgress) {

                                            }

                                            @Override
                                            public void onError() {

                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onUnbinder() {

                                    }
                                });
                                manager.binder();
                            }
                        }).show();
                    }

                    @Override
                    public void onNoUpdateAvailable(String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.button_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.cancel();
            }
        });
    }
}
