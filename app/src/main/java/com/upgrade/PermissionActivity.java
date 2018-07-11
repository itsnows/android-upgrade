package com.upgrade;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;

import com.upgradelibrary.Util;
import com.upgradelibrary.data.bean.Upgrade;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/23 10:47
 * <p>
 * PermissionActivity
 */

public class PermissionActivity extends Activity {
    public static final String ACTION_UPGRADE_DIALOG = "upgrade_dialog";
    public static final String ACTION_INSTALL_APK = "install_apk";
    private Bundle parame;

    /**
     * @param activity
     * @param parame
     */
    public static void open(@NonNull Activity activity, @NonNull Bundle parame) {
        Preconditions.checkNotNull(activity);
        Preconditions.checkNotNull(parame);
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra("parame", parame);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parame = getIntent().getBundleExtra("parame");
        if (ACTION_UPGRADE_DIALOG.equals(parame.getString("action"))) {
            Upgrade upgrade = parame.getParcelable("upgrade");


        } else if (ACTION_INSTALL_APK.equals(parame.getString("action"))) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Util.REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            default:
                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


}
