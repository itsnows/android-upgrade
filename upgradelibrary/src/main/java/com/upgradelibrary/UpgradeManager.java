package com.upgradelibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import android.util.Log;
import android.widget.Toast;

import com.upgradelibrary.bean.Upgrade;
import com.upgradelibrary.bean.UpgradeOptions;

import java.lang.ref.WeakReference;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/28 11:24
 * <p>
 * 升级管理
 */

public class UpgradeManager {
    private static final String TAG = UpgradeManager.class.getSimpleName();
    private Activity activity;
    private CheckForUpdatesTask task;

    public UpgradeManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * 检测更新
     *
     * @param options     更新选项
     * @param isAutoCheck 是否自动检测更新
     */
    @SuppressLint("RestrictedApi")
    public void checkForUpdates(@NonNull UpgradeOptions options, boolean isAutoCheck) {
        execute(Preconditions.checkNotNull(options), isAutoCheck);
    }

    /**
     * 检测更新
     *
     * @param options           更新选项
     * @param onUpgradeListener 更新监听回调接口
     */
    @SuppressLint("RestrictedApi")
    public void checkForUpdates(@NonNull UpgradeOptions options, @Nullable OnUpgradeListener onUpgradeListener) {
        execute(Preconditions.checkNotNull(options), onUpgradeListener);
    }

    /**
     * 执行检测更新
     *
     * @param parames
     */
    private void execute(Object... parames) {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new CheckForUpdatesTask(activity);
        }
        if (task.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        task.execute(parames);
    }

    /**
     * 取消检测更新
     */
    public void cancel() {
        if (task == null) {
            return;
        }
        if (!task.isCancelled()) {
            task.cancel(false);
        }
        Log.d(TAG, "cancel checked updates");
    }

    /**
     * 更新监听回调接口
     */
    public interface OnUpgradeListener {

        void onUpdateAvailable(Upgrade upgrade, UpgradeServiceManager manager);

        void onNoUpdateAvailable(String message);

    }

    /**
     * 检测更新任务
     */
    public static class CheckForUpdatesTask extends AsyncTask<Object, Void, Message> {
        private static final int RESULT_CODE_TRUE = 0x1024;
        private static final int RESULT_CODE_FALSE = 0x1025;
        private WeakReference<Activity> reference;

        public CheckForUpdatesTask(Activity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Message doInBackground(Object... objects) {
            Message message = new Message();
            message.what = RESULT_CODE_TRUE;
            message.obj = objects[1];
            message.setData(new Bundle());
            try {
                UpgradeOptions upgradeOptions = (UpgradeOptions) objects[0];
                if (upgradeOptions.getUrl() != null && upgradeOptions.getUrl().endsWith(".apk")) {
                    message.getData().putParcelable("upgrade_options", upgradeOptions);
                    return message;
                }
                if (upgradeOptions.getUrl() != null && upgradeOptions.getUrl().endsWith(".xml")) {
                    Upgrade upgrade = Upgrade.parser(upgradeOptions.getUrl());
                    message.getData().putParcelable("upgrade", upgrade);
                    message.getData().putParcelable("upgrade_options", new UpgradeOptions.Builder()
                            .setIcon(upgradeOptions.getIcon())
                            .setTitle(upgradeOptions.getTitle())
                            .setDescription(upgradeOptions.getDescription())
                            .setStorage(upgradeOptions.getStorage())
                            .setUrl(upgrade.getDowanloadUrl())
                            .setMutiThreadEnabled(upgradeOptions.isMultithreadEnabled())
                            .setMaxThreadPools(upgradeOptions.getMaxThreadPools())
                            .setMd5(upgrade.getMd5())
                            .build());
                    return message;
                }
                throw new IllegalArgumentException("Url：" + upgradeOptions.getUrl() + " link error");
            } catch (Exception e) {
                e.printStackTrace();
                message.what = RESULT_CODE_FALSE;
                message.getData().putString("message", e.getMessage());
            }
            return message;
        }

        @Override
        protected void onCancelled(Message message) {
            Activity activity = reference.get();
            super.onCancelled(message);
        }

        @Override
        protected void onPostExecute(Message message) {
            Activity activity = reference.get();
            if (activity == null) {
                return;
            }

            Bundle bundle = message.getData();
            Upgrade upgrade = bundle.getParcelable("upgrade");
            UpgradeOptions upgradeOptions = bundle.getParcelable("upgrade_options");
            switch (message.what) {
                case RESULT_CODE_TRUE:
                    if (message.obj instanceof Boolean) {
                        boolean isAutoCheck = (boolean) message.obj;
                        if (upgrade.getVersionCode() <= Util.getVersionCode(activity)) {
                            if (!isAutoCheck) {
                                Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        if (upgrade.getMode() == Upgrade.UPGRADE_MODE_PARTIAL && !upgrade.getDevice().contains(Util.getSerial())) {
                            if (!isAutoCheck) {
                                Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        if (isAutoCheck) {
                            if (Historical.isIgnoreVersion(activity, upgrade.getVersionCode())) {
                                return;
                            }
                        }
                        UpgradeDialog.newInstance(activity, upgrade, upgradeOptions).show();
                    } else {
                        if (message.obj == null) {
                            return;
                        }

                        OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                        if (upgrade.getVersionCode() <= Util.getVersionCode(activity)) {
                            onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                            return;
                        }

                        if (upgrade.getMode() == Upgrade.UPGRADE_MODE_PARTIAL && !upgrade.getDevice().contains(Util.getSerial())) {
                            onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                            return;
                        }

                        if (Historical.isIgnoreVersion(activity, upgrade.getVersionCode())) {
                            onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                            return;
                        }
                        onUpgradeListener.onUpdateAvailable(upgrade, new UpgradeServiceManager(activity, upgradeOptions));
                    }
                    break;
                case RESULT_CODE_FALSE:
                    if (message.obj instanceof Boolean) {
                        boolean isAutoCheck = (boolean) message.obj;
                        if (!isAutoCheck) {
                            Toast.makeText(activity, activity.getString(R.string.check_for_update_failure), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                        if (onUpgradeListener != null) {
                            onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_failure));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
