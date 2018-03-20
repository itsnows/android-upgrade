package com.upgrade;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.upgradelibrary.UpgradeManager;
import com.upgradelibrary.UpgradeServiceManager;
import com.upgradelibrary.Util;
import com.upgradelibrary.bean.Upgrade;
import com.upgradelibrary.bean.UpgradeOptions;
import com.upgradelibrary.service.UpgradeService;

import java.io.File;

/**
 * 支持：断点续传
 * 支持：暂停、取消
 * 支持：分流下载
 * 支持：动态网络监听下载
 * 支持：8.0 适配
 * <p>
 * 更新文档模板路径：../android-upgrade/upgradelibrary/app-update.xml
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final UpgradeManager manager = new UpgradeManager(this);
        findViewById(R.id.button_check_updates_default_common).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.checkForUpdates(new UpgradeOptions.Builder()
                        .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                        // 通知栏标题（可选）
                        .setTitle("腾讯QQ")
                        // 通知栏描述（可选）
                        .setDescription("更新通知栏")
                        // 下载链接或更新文档链接
                        .setUrl("http://www.rainen.cn/test/app-update_common.xml")
                        // 下载文件存储路径（可选）
                        .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                        // 是否支持多线性下载（可选）
                        .setMutiThreadEnabled(true)
                        // 线程池大小（可选）
                        .setMaxThreadPools(1)
                        // 文件MD5（可选）
                        .setMd5(null)
                        .build(), false);
            }
        });

        findViewById(R.id.button_check_updates_default_forced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.checkForUpdates(new UpgradeOptions.Builder()
                        .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                        // 通知栏标题（可选）
                        .setTitle("腾讯QQ")
                        // 通知栏描述（可选）
                        .setDescription("更新通知栏")
                        // 下载链接或更新文档链接
                        .setUrl("http://www.rainen.cn/test/app-update_forced.xml")
                        // 下载文件存储路径（可选）
                        .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                        // 是否支持多线性下载（可选）
                        .setMutiThreadEnabled(true)
                        // 线程池大小（可选）
                        .setMaxThreadPools(10)
                        // 文件MD5（可选）
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
                    public void onUpdateAvailable(Upgrade upgrade, UpgradeServiceManager manager) {
                        showUpgradeDialog(upgrade, manager);
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

    /**
     * 显示更新提示（自定义提示）
     *
     * @param upgrade Upgrade
     * @param manager UpgradeServiceManager
     */
    private void showUpgradeDialog(Upgrade upgrade, final UpgradeServiceManager manager) {
        StringBuffer logs = new StringBuffer();
        for (int i = 0; i < upgrade.getLogs().size(); i++) {
            logs.append(upgrade.getLogs().get(i));
            logs.append(i < upgrade.getLogs().size() - 1 ? "\n" : "");
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("发现新版本 v" + upgrade.getVersionName())
                .setMessage(logs.toString())
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                // 下载监听接口
                manager.setOnBinderUpgradeServiceLisenter(new UpgradeServiceManager.OnBinderUpgradeServiceLisenter() {
                    @Override
                    public void onBinder(UpgradeService upgradeService) {
                        upgradeService.setOnDownloadListener(new UpgradeService.OnDownloadListener() {

                            @Override
                            public void onStart() {
                                super.onStart();
                                Log.d(TAG, "onStart");
                            }

                            @Override
                            public void onProgress(long progress, long maxProgress) {
                                Log.d(TAG, "onProgress：" + Util.formatByte(progress) + "/" + Util.formatByte(maxProgress));
                            }

                            @Override
                            public void onPause() {
                                super.onPause();
                                Log.d(TAG, "onPause");
                            }

                            @Override
                            public void onCancel() {
                                super.onCancel();
                                Log.d(TAG, "onCancel");
                            }

                            @Override
                            public void onError() {
                                Log.d(TAG, "onError");
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete");
                            }
                        });

                    }

                    @Override
                    public void onUnbinder() {
                        Log.d(TAG, "onUnbinder");
                    }
                });
                // 开始下载
                manager.binder();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                manager.unbinder();
            }
        }).show();
    }

}
