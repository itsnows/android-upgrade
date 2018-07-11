package com.upgradelibrary.common;

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

import com.upgradelibrary.R;
import com.upgradelibrary.Util;
import com.upgradelibrary.data.UpgradeRepository;
import com.upgradelibrary.data.bean.Upgrade;
import com.upgradelibrary.data.bean.UpgradeOptions;
import com.upgradelibrary.data.bean.UpgradeVersion;

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

        void onUpdateAvailable(UpgradeServiceClient manager);

        void onUpdateAvailable(Upgrade.Stable stable, UpgradeServiceClient client);

        void onUpdateAvailable(Upgrade.Beta bate, UpgradeServiceClient client);

        void onNoUpdateAvailable(String message);

    }

    /**
     * 检测更新任务
     */
    private static class CheckForUpdatesTask extends AsyncTask<Object, Void, Message> {
        private static final int RESULT_CODE_TRUE = 0x1024;
        private static final int RESULT_CODE_FALSE = 0x1025;
        private WeakReference<Activity> reference;

        private CheckForUpdatesTask(Activity activity) {
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
                message.getData().putParcelable("upgrade_options", upgradeOptions);
                if (upgradeOptions.getUrl() != null && upgradeOptions.getUrl().endsWith(".apk")) {
                    return message;
                }

                if (upgradeOptions.getUrl() != null && upgradeOptions.getUrl().endsWith(".xml")) {
                    Upgrade upgrade = Upgrade.parser(upgradeOptions.getUrl());
                    if (upgrade != null) {
                        message.getData().putParcelable("upgrade", upgrade);
                        return message;
                    }
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
            UpgradeOptions.Builder builder = new UpgradeOptions.Builder()
                    .setIcon(upgradeOptions.getIcon())
                    .setTitle(upgradeOptions.getTitle())
                    .setDescription(upgradeOptions.getDescription())
                    .setStorage(upgradeOptions.getStorage())
                    .setUrl(upgradeOptions.getUrl())
                    .setMultithreadEnabled(upgradeOptions.isMultithreadEnabled())
                    .setMultithreadPools(upgradeOptions.getMultithreadPools())
                    .setMd5(upgradeOptions.getMd5());
            switch (message.what) {
                case RESULT_CODE_TRUE:
                    if (upgrade == null) {
                        if (message.obj instanceof Boolean) {
                            new UpgradeServiceClient(activity, builder.build()).start();
                        } else {
                            if (message.obj == null) {
                                return;
                            }
                            OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                            onUpgradeListener.onUpdateAvailable(new UpgradeServiceClient(activity, builder.build()));
                        }
                    } else {
                        if (upgrade.getStable() != null && upgrade.getBeta() != null) {
                            if (!upgrade.getBeta().getDevice().contains(Util.getSerial()) ||
                                    upgrade.getStable().getVersionCode() >= upgrade.getBeta().getVersionCode()) {
                                if (message.obj instanceof Boolean) {
                                    boolean isAutoCheck = (boolean) message.obj;
                                    UpgradeRepository repository = new UpgradeRepository(activity);
                                    UpgradeVersion version = repository.getUpgradeVersion(upgrade.getStable().getVersionCode());
                                    if (isAutoCheck && version != null && version.isIgnored()) {
                                        return;
                                    }
                                    if (upgrade.getStable().getVersionCode() <= Util.getVersionCode(activity)) {
                                        if (!isAutoCheck) {
                                            Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        return;
                                    }
                                    upgrade.setBeta(null);
                                    UpgradeDialog.newInstance(activity, upgrade, builder
                                            .setUrl(upgrade.getStable().getDowanloadUrl())
                                            .setMd5(upgrade.getStable().getMd5())
                                            .build()).show();
                                } else {
                                    if (message.obj == null) {
                                        return;
                                    }
                                    OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                    UpgradeRepository repository = new UpgradeRepository(activity);
                                    UpgradeVersion version = repository.getUpgradeVersion(upgrade.getStable().getVersionCode());
                                    if (version != null && version.isIgnored()) {
                                        onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                        return;
                                    }
                                    if (upgrade.getStable().getVersionCode() <= Util.getVersionCode(activity)) {
                                        onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                        return;
                                    }
                                    upgrade.setBeta(null);
                                    UpgradeDialog.newInstance(activity, upgrade, builder
                                            .setUrl(upgrade.getStable().getDowanloadUrl())
                                            .setMd5(upgrade.getStable().getMd5())
                                            .build()).show();
                                }
                                return;
                            }
                            if (message.obj instanceof Boolean) {
                                boolean isAutoCheck = (boolean) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getBeta().getVersionCode());
                                if (isAutoCheck && version != null && version.isIgnored()) {
                                    return;
                                }
                                if (upgrade.getBeta().getVersionCode() <= Util.getVersionCode(activity)) {
                                    if (!isAutoCheck) {
                                        Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    return;
                                }
                                upgrade.setStable(null);
                                UpgradeDialog.newInstance(activity, upgrade, builder
                                        .setUrl(upgrade.getBeta().getDowanloadUrl())
                                        .setMd5(upgrade.getBeta().getMd5())
                                        .build()).show();
                            } else {
                                if (message.obj == null) {
                                    return;
                                }
                                OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getBeta().getVersionCode());
                                if (version != null && version.isIgnored()) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                if (upgrade.getBeta().getVersionCode() <= Util.getVersionCode(activity)) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                upgrade.setStable(null);
                                UpgradeDialog.newInstance(activity, upgrade, builder
                                        .setUrl(upgrade.getBeta().getDowanloadUrl())
                                        .setMd5(upgrade.getBeta().getMd5())
                                        .build()).show();
                            }
                            return;
                        }
                        if (upgrade.getBeta() != null) {
                            if (message.obj instanceof Boolean) {
                                boolean isAutoCheck = (boolean) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getBeta().getVersionCode());
                                if (isAutoCheck && version != null && version.isIgnored()) {
                                    return;
                                }
                                if (upgrade.getBeta().getVersionCode() <= Util.getVersionCode(activity)) {
                                    if (!isAutoCheck) {
                                        Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    return;
                                }
                                if (!upgrade.getBeta().getDevice().contains(Util.getSerial())) {
                                    if (!isAutoCheck) {
                                        Toast.makeText(activity, activity.getString(R.string.check_for_update_notfound), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    return;
                                }
                                UpgradeDialog.newInstance(activity, upgrade, builder
                                        .setUrl(upgrade.getBeta().getDowanloadUrl())
                                        .setMd5(upgrade.getBeta().getMd5())
                                        .build()).show();
                            } else {
                                if (message.obj == null) {
                                    return;
                                }
                                OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getBeta().getVersionCode());
                                if (version != null && version.isIgnored()) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                if (!upgrade.getBeta().getDevice().contains(Util.getSerial())) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                if (upgrade.getBeta().getVersionCode() <= Util.getVersionCode(activity)) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                onUpgradeListener.onUpdateAvailable(upgrade.getBeta(), new UpgradeServiceClient(activity, builder
                                        .setUrl(upgrade.getBeta().getDowanloadUrl())
                                        .setMd5(upgrade.getBeta().getMd5())
                                        .build()));
                            }
                            return;
                        }
                        if (upgrade.getStable() != null) {
                            if (message.obj instanceof Boolean) {
                                boolean isAutoCheck = (boolean) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getStable().getVersionCode());
                                if (isAutoCheck && version != null && version.isIgnored()) {
                                    return;
                                }
                                UpgradeDialog.newInstance(activity, upgrade, builder
                                        .setUrl(upgrade.getStable().getDowanloadUrl())
                                        .setMd5(upgrade.getStable().getMd5())
                                        .build()).show();
                            } else {
                                if (message.obj == null) {
                                    return;
                                }
                                OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                UpgradeRepository repository = new UpgradeRepository(activity);
                                UpgradeVersion version = repository.getUpgradeVersion(upgrade.getStable().getVersionCode());
                                if (version != null && version.isIgnored()) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                if (upgrade.getStable().getVersionCode() <= Util.getVersionCode(activity)) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.check_for_update_notfound));
                                    return;
                                }
                                onUpgradeListener.onUpdateAvailable(upgrade.getStable(), new UpgradeServiceClient(activity, builder
                                        .setUrl(upgrade.getStable().getDowanloadUrl())
                                        .setMd5(upgrade.getStable().getMd5())
                                        .build()));
                            }
                            return;
                        }
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
