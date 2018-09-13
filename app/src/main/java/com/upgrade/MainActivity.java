package com.upgrade;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itsnows.upgrade.Util;
import com.itsnows.upgrade.common.UpgradeClient;
import com.itsnows.upgrade.common.UpgradeManager;
import com.itsnows.upgrade.data.bean.Upgrade;
import com.itsnows.upgrade.data.bean.UpgradeOptions;
import com.itsnows.upgrade.service.UpgradeService;

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
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private UpgradeManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new UpgradeManager(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.button_check_updates_default_common).setOnClickListener(this);
        findViewById(R.id.button_check_updates_default_forced).setOnClickListener(this);
        findViewById(R.id.button_check_updates_default_bate).setOnClickListener(this);
        findViewById(R.id.button_check_updates_custom).setOnClickListener(this);
        findViewById(R.id.button_check_updates_custom_download).setOnClickListener(this);
        findViewById(R.id.button_cancle).setOnClickListener(this);
        autoCheckUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void checkUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("更新通知栏")
                // 下载链接或更新文档链接
                .setUrl("http://www.rainen.cn/test/app-update-common.xml")
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线性下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(10)
                // 文件MD5（可选）
                .setMd5(null)
                .build(), false);
    }

    private void autoCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("更新通知栏")
                // 下载链接或更新文档链接
                .setUrl("http://www.rainen.cn/test/app-update-common.xml")
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线性下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(1)
                // 文件MD5（可选）
                .setMd5(null)
                .build(), true);
    }

    private void forceCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("更新通知栏")
                // 下载链接或更新文档链接
                .setUrl("http://www.rainen.cn/test/app-update-forced.xml")
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线程下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(10)
                // 文件MD5（可选）
                .setMd5(null)
                .build(), false);
    }

    private void customerCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("更新通知栏")
                .setUrl("http://www.rainen.cn/test/app-update-common.xml")
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadEnabled(true)
                .setMultithreadPools(1)
                .setMd5(null)
                .build(), new UpgradeManager.OnUpgradeListener() {

            @Override
            public void onUpdateAvailable(UpgradeClient manager) {

            }

            @Override
            public void onUpdateAvailable(Upgrade.Stable stable, UpgradeClient client) {
                showUpgradeDialog(stable, client);
            }

            @Override
            public void onUpdateAvailable(Upgrade.Beta beta, UpgradeClient client) {
            }

            @Override
            public void onNoUpdateAvailable(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customerDownloadUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("更新通知栏")
                // 下载链接或更新文档链接
                .setUrl("http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk")
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线程下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(1)
                // 文件MD5（可选）
                .setMd5(null)
                .build(), false);
    }

    /**
     * 显示更新提示（自定义提示）
     *
     * @param stable  Upgrade.Stable
     * @param client UpgradeClient
     */
    private void showUpgradeDialog(Upgrade.Stable stable, final UpgradeClient client) {
        StringBuffer logs = new StringBuffer();
        for (int i = 0; i < stable.getLogs().size(); i++) {
            logs.append(stable.getLogs().get(i));
            logs.append(i < stable.getLogs().size() - 1 ? "\n" : "");
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("发现新版本 v" + stable.getVersionName())
                .setMessage(logs.toString())
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                client.setOnBinderServiceLisenter(new UpgradeClient.OnBinderServiceLisenter() {
                    @Override
                    public void onBinder(UpgradeService upgradeService) {
                        upgradeService.setOnDownloadListener(new UpgradeService.OnDownloadListener() {

                            @Override
                            public void onStart() {
                                super.onStart();
                                Log.d(TAG, "onStart");
                            }

                            @Override
                            public void onProgress(long max, long progress) {
                                Log.d(TAG, "onProgress：" + Util.formatByte(progress) + "/" + Util.formatByte(max));
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
                                dialog.dismiss();
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
                client.binder();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                client.unbinder();
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Util.REQUEST_CODE_WRITE_EXTERNAL_STORAGE ||
                grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            customerDownloadUpdates();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_check_updates_default_common:
                checkUpdates();
                break;
            case R.id.button_check_updates_default_forced:
                forceCheckUpdates();
                break;
            case R.id.button_check_updates_default_bate:
                break;
            case R.id.button_check_updates_custom:
                customerCheckUpdates();
                break;
            case R.id.button_check_updates_custom_download:
                if (Util.mayRequestExternalStorage(this, true)) {
                    customerDownloadUpdates();
                }
                break;
            case R.id.button_cancle:
                manager.cancel();
                break;
            default:
                break;
        }

    }
}
