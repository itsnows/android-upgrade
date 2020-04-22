package com.upgrade;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itsnows.upgrade.OnDownloadListener;
import com.itsnows.upgrade.OnUpgradeListener;
import com.itsnows.upgrade.UpgradeClient;
import com.itsnows.upgrade.UpgradeConstant;
import com.itsnows.upgrade.UpgradeException;
import com.itsnows.upgrade.UpgradeManager;
import com.itsnows.upgrade.UpgradeUtil;
import com.itsnows.upgrade.model.bean.Upgrade;
import com.itsnows.upgrade.model.bean.UpgradeOptions;

import java.io.File;

/**
 * 支持：断点续传
 * 支持：暂停、取消
 * 支持：分流下载
 * 支持：动态网络监听下载
 * 支持：自动安装（root权限）
 * 支持：自动清除安装包
 * 支持：10.0 适配
 * <p>
 * 更新文档模板路径：../doc/app-update.xml
 * 更新文档模板路径：../doc/app-update.json
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DOC_JSON_URL = "https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update.json";
    private static final String DOC_XML_URL = "https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update.xml";
    private static final String DOC_BATE_XML_URL = "https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update-bate.xml";
    private static final String DOC_FORCE_XML_URL = "https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update-force.xml";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0x8052;
    private UpgradeManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new UpgradeManager(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_check_updates_default_common).setOnClickListener(this);
        findViewById(R.id.btn_check_updates_default_force).setOnClickListener(this);
        findViewById(R.id.btn_check_updates_default_bate).setOnClickListener(this);
        findViewById(R.id.btn_check_updates_custom).setOnClickListener(this);
        findViewById(R.id.btn_check_updates_custom_download).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        autoCheckUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 自动检测更新（稳定版：普通升级）
     */
    private void autoCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                // 对话框主题（可选）
                .setTheme(ContextCompat.getColor(this, R.color.colorPrimary))
                // 通知栏图标（可选）
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("下载更新")
                // 下载链接或更新文档链接
                .setUrl(DOC_JSON_URL)
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线性下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(10)
                // 文件MD5（可选）
                .setMd5(null)
                // 是否自动删除安装包（可选）
                .setAutocleanEnabled(true)
                // 是否自动安装安装包（可选）
                .setAutomountEnabled(true)
                // 是否自动检测更新
                .build(), true);
    }

    /**
     * 默认检测更新（稳定版：普通升级）
     */
    private void commonCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("下载更新")
                .setUrl(DOC_XML_URL)
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadEnabled(true)
                .setMultithreadPools(1)
                .build(), true);
    }

    /**
     * 默认检测更新（稳定版：强制升级）
     */
    private void forceCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("下载更新")
                .setUrl(DOC_FORCE_XML_URL)
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadEnabled(true)
                .setMultithreadPools(10)
                .setMd5(null)
                .setAutocleanEnabled(true)
                .setAutomountEnabled(true)
                .build(), false);
    }

    /**
     * 默认检测更新（测试版：普通升级）
     * 提示：灰度升级，需要添加自己设备sn
     */
    private void betaCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("下载更新")
                .setUrl(DOC_BATE_XML_URL)
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadEnabled(true)
                .setMultithreadPools(10)
                .setMd5(null)
                .setAutocleanEnabled(true)
                .setAutomountEnabled(true)
                .build(), false);
    }

    /**
     * 自定义下载更新（自己实现逻辑代码、更新提示）
     */
    private void customerCheckUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("下载更新")
                .setUrl(DOC_XML_URL)
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadEnabled(true)
                .setMultithreadPools(1)
                .setMd5(null)
                .setAutocleanEnabled(true)
                .setAutomountEnabled(true)
                .build(), new OnUpgradeListener() {

            @Override
            public void onUpdateAvailable(UpgradeClient client) {
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

    /**
     * 自定义下载更新（自己实现逻辑代码、更新提示）
     */
    private void customerDownloadUpdates() {
        manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setTitle("腾讯QQ")
                .setDescription("下载更新")
                .setUrl("http://gdown.baidu.com/data/wisegame/2965a5c112549eb8/QQ_996.apk")
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                .setMultithreadPools(1)
                .setMultithreadEnabled(true)
                .setMd5(null)
                .setAutocleanEnabled(true)
                .build(), false);
    }

    /**
     * 显示更新提示（自定义提示）
     *
     * @param stable Upgrade.Stable
     * @param client UpgradeClient
     */
    private void showUpgradeDialog(Upgrade.Stable stable, final UpgradeClient client) {
        StringBuffer logs = new StringBuffer();
        for (int i = 0; i < stable.getLogs().size(); i++) {
            logs.append(stable.getLogs().get(i));
            logs.append(i < stable.getLogs().size() - 1 ? "\n" : "");
        }

        View view = View.inflate(this, R.layout.dialog_custom, null);
        TextView tvMessage = view.findViewById(R.id.tv_dialog_custom_message);
        final Button btnUpgrade = view.findViewById(R.id.btn_dialog_custom_upgrade);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                client.remove();
            }
        });

        tvMessage.setText(logs.toString());
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开始下载
                if (mayRequestExternalStorage(MainActivity.this, true)) {
                    client.start();
                }
                dialog.dismiss();
            }
        });
        client.setOnDownloadListener(new OnDownloadListener() {

            @Override
            public void onStart() {
                super.onStart();
                Log.d(TAG, "onStart");
            }

            @Override
            public void onProgress(long max, long progress) {
                Log.d(TAG, "onProgress：" + UpgradeUtil.formatByte(progress) + "/" + UpgradeUtil.formatByte(max));
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
            public void onError(UpgradeException e) {
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                btnUpgrade.setTag(UpgradeConstant.MSG_KEY_DOWNLOAD_COMPLETE_REQ);
                Log.d(TAG, "onComplete");
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                client.remove();
            }
        });
        dialog.show();
    }

    /**
     * 判断申请外部存储所需权限
     *
     * @param context
     * @param isActivate
     * @return
     */
    public boolean mayRequestExternalStorage(Context context, boolean isActivate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (isActivate) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE &&
                grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            customerDownloadUpdates();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check_updates_default_common:
                commonCheckUpdates();
                break;
            case R.id.btn_check_updates_default_force:
                forceCheckUpdates();
                break;
            case R.id.btn_check_updates_default_bate:
                betaCheckUpdates();
                break;
            case R.id.btn_check_updates_custom:
                customerCheckUpdates();
                break;
            case R.id.btn_check_updates_custom_download:
                if (mayRequestExternalStorage(this, true)) {
                    customerDownloadUpdates();
                }
                break;
            case R.id.btn_cancel:
                manager.cancel();
                break;
            default:
                break;
        }

    }


}
