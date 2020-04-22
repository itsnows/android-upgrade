package com.itsnows.upgrade;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import android.widget.Toast;

import com.itsnows.upgrade.model.UpgradeRepository;
import com.itsnows.upgrade.model.bean.Upgrade;
import com.itsnows.upgrade.model.bean.UpgradeOptions;
import com.itsnows.upgrade.model.bean.UpgradeVersion;

import java.lang.ref.WeakReference;

/**
 * Author: itsnows
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
     * @param params
     */
    private void execute(Object... params) {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new CheckForUpdatesTask(activity);
        }
        if (task.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        task.execute(params);
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
        UpgradeLogger.d(TAG, "Cancel checked updates");
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
        protected Message doInBackground(Object... params) {
            Message message = new Message();
            message.what = RESULT_CODE_TRUE;
            message.obj = params[1];
            message.setData(new Bundle());
            try {
                UpgradeOptions upgradeOptions = (UpgradeOptions) params[0];
                message.getData().putParcelable("upgrade_options", upgradeOptions);
                if (upgradeOptions.getUrl() != null && upgradeOptions.getUrl().endsWith(".apk")) {
                    return message;
                }
                Upgrade upgrade = Upgrade.parser(upgradeOptions.getUrl());
                if (upgrade != null) {
                    message.getData().putParcelable("upgrade", upgrade);
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
                    .setTheme(upgradeOptions.getTheme())
                    .setIcon(upgradeOptions.getIcon())
                    .setTitle(upgradeOptions.getTitle())
                    .setDescription(upgradeOptions.getDescription())
                    .setStorage(upgradeOptions.getStorage())
                    .setUrl(upgradeOptions.getUrl())
                    .setMultithreadEnabled(upgradeOptions.isMultithreadEnabled())
                    .setMultithreadPools(upgradeOptions.getMultithreadPools())
                    .setAutomountEnabled(upgradeOptions.isAutomountEnabled())
                    .setAutocleanEnabled(upgradeOptions.isAutocleanEnabled())
                    .setMd5(upgradeOptions.getMd5());
            switch (message.what) {
                case RESULT_CODE_TRUE:
                    if (upgrade == null) {
                        if (message.obj == null || message.obj instanceof Boolean) {
                            UpgradeClient.add(activity, builder.build()).start();
                            return;
                        }
                        OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                        onUpgradeListener.onUpdateAvailable(UpgradeClient.add(activity, builder.build()));
                        return;
                    }
                    if (upgrade.getStable() != null && upgrade.getBeta() != null) {
                        if (!upgrade.getBeta().getDevice().contains(UpgradeUtil.getSerial()) ||
                                upgrade.getStable().getVersionCode() >= upgrade.getBeta().getVersionCode()) {
                            if (message.obj instanceof Boolean) {
                                boolean isAutoCheck = (boolean) message.obj;
                                UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                        .getUpgradeVersion(upgrade.getStable().getVersionCode());
                                if (isAutoCheck && version != null && version.isIgnored()) {
                                    return;
                                }
                                if (upgrade.getStable().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                    if (!isAutoCheck) {
                                        Toast.makeText(activity, activity.getString(R.string.message_check_for_update_no_available), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    return;
                                }
                                upgrade.setBeta(null);
                                UpgradeDialog.newInstance(activity, upgrade, builder
                                        .setUrl(upgrade.getStable().getDownloadUrl())
                                        .setMd5(upgrade.getStable().getMd5())
                                        .build()).show();
                            } else {
                                if (message.obj != null) {
                                    OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                    UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                            .getUpgradeVersion(upgrade.getStable().getVersionCode());
                                    if (version != null && version.isIgnored()) {
                                        onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                        return;
                                    }
                                    if (upgrade.getStable().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                        onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                        return;
                                    }
                                    onUpgradeListener.onUpdateAvailable(upgrade.getStable(), UpgradeClient.add(activity, builder
                                            .setUrl(upgrade.getStable().getDownloadUrl())
                                            .setMd5(upgrade.getStable().getMd5())
                                            .build()));
                                }
                            }
                            return;
                        }
                        if (message.obj instanceof Boolean) {
                            boolean isAutoCheck = (boolean) message.obj;
                            UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                    .getUpgradeVersion(upgrade.getBeta().getVersionCode());
                            if (isAutoCheck && version != null && version.isIgnored()) {
                                return;
                            }
                            if (upgrade.getBeta().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                if (!isAutoCheck) {
                                    Toast.makeText(activity, activity.getString(R.string.message_check_for_update_no_available), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                return;
                            }
                            upgrade.setStable(null);
                            UpgradeDialog.newInstance(activity, upgrade, builder
                                    .setUrl(upgrade.getBeta().getDownloadUrl())
                                    .setMd5(upgrade.getBeta().getMd5())
                                    .build()).show();
                        } else {
                            if (message.obj != null) {
                                OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                                UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                        .getUpgradeVersion(upgrade.getBeta().getVersionCode());
                                if (version != null && version.isIgnored()) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                    return;
                                }
                                if (upgrade.getBeta().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                    onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                    return;
                                }
                                upgrade.setStable(null);
                                onUpgradeListener.onUpdateAvailable(upgrade.getBeta(), UpgradeClient.add(activity, builder
                                        .setUrl(upgrade.getBeta().getDownloadUrl())
                                        .setMd5(upgrade.getBeta().getMd5())
                                        .build()));
                            }
                        }
                        return;
                    }
                    if (upgrade.getBeta() != null) {
                        if (message.obj instanceof Boolean) {
                            boolean isAutoCheck = (boolean) message.obj;
                            UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                    .getUpgradeVersion(upgrade.getBeta().getVersionCode());
                            if (isAutoCheck && version != null && version.isIgnored()) {
                                return;
                            }
                            if (upgrade.getBeta().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                if (!isAutoCheck) {
                                    Toast.makeText(activity, activity.getString(R.string.message_check_for_update_no_available), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                return;
                            }
                            if (!upgrade.getBeta().getDevice().contains(UpgradeUtil.getSerial())) {
                                if (!isAutoCheck) {
                                    Toast.makeText(activity, activity.getString(R.string.message_check_for_update_no_available), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                return;
                            }
                            UpgradeDialog.newInstance(activity, upgrade, builder
                                    .setUrl(upgrade.getBeta().getDownloadUrl())
                                    .setMd5(upgrade.getBeta().getMd5())
                                    .build()).show();
                        } else {
                            if (message.obj == null) {
                                return;
                            }
                            OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                            UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                    .getUpgradeVersion(upgrade.getBeta().getVersionCode());
                            if (version != null && version.isIgnored()) {
                                onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                return;
                            }
                            if (!upgrade.getBeta().getDevice().contains(UpgradeUtil.getSerial())) {
                                onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                return;
                            }
                            if (upgrade.getBeta().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                return;
                            }
                            onUpgradeListener.onUpdateAvailable(upgrade.getBeta(), UpgradeClient.add(activity, builder
                                    .setUrl(upgrade.getBeta().getDownloadUrl())
                                    .setMd5(upgrade.getBeta().getMd5())
                                    .build()));
                        }
                        return;
                    }
                    if (upgrade.getStable() != null) {
                        if (message.obj instanceof Boolean) {
                            boolean isAutoCheck = (boolean) message.obj;
                            UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                    .getUpgradeVersion(upgrade.getStable().getVersionCode());
                            if (isAutoCheck && version != null && version.isIgnored()) {
                                return;
                            }
                            UpgradeDialog.newInstance(activity, upgrade, builder
                                    .setUrl(upgrade.getStable().getDownloadUrl())
                                    .setMd5(upgrade.getStable().getMd5())
                                    .build()).show();
                        } else {
                            if (message.obj == null) {
                                return;
                            }
                            OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                            UpgradeVersion version = UpgradeRepository.getInstance(activity)
                                    .getUpgradeVersion(upgrade.getStable().getVersionCode());
                            if (version != null && version.isIgnored()) {
                                onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                return;
                            }
                            if (upgrade.getStable().getVersionCode() <= UpgradeUtil.getVersionCode(activity)) {
                                onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_no_available));
                                return;
                            }
                            onUpgradeListener.onUpdateAvailable(upgrade.getStable(), UpgradeClient.add(activity, builder
                                    .setUrl(upgrade.getStable().getDownloadUrl())
                                    .setMd5(upgrade.getStable().getMd5())
                                    .build()));
                        }
                        return;
                    }
                case RESULT_CODE_FALSE:
                    if (message.obj instanceof Boolean) {
                        boolean isAutoCheck = (boolean) message.obj;
                        if (!isAutoCheck) {
                            Toast.makeText(activity, activity.getString(R.string.message_check_for_update_failure), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        OnUpgradeListener onUpgradeListener = (OnUpgradeListener) message.obj;
                        if (onUpgradeListener != null) {
                            onUpgradeListener.onNoUpdateAvailable(activity.getString(R.string.message_check_for_update_failure));
                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }

}
