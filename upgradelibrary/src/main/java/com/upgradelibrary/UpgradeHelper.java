package com.upgradelibrary;

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
import com.upgradelibrary.service.UpgradeService;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/10 19:44
 * <p>
 * UpgradeHelper
 */

@SuppressWarnings("deprecation")
public class UpgradeHelper {
    private static final String TAG = UpgradeHelper.class.getSimpleName();
    private Activity activity;
    private CheckForUpdatesTask task;

    public UpgradeHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * 检测更新
     *
     * @param documentUrl 更新文档链接
     * @param isAutoCheck 是否自动检测更新
     */
    public void checkForUpdates(@NonNull String documentUrl, boolean isAutoCheck) {
        execute(Preconditions.checkNotNull(documentUrl), isAutoCheck);
    }

    /**
     * 检测更新
     *
     * @param options     更新文档链接
     * @param isAutoCheck 是否自动检测更新
     */
    public void checkForUpdates(@NonNull UpgradeOptions options, boolean isAutoCheck) {


    }

    /**
     * 检测更新
     *
     * @param documentUrl 更新文档链接
     * @param callBack    检测更新回调接口
     */
    public void checkForUpdates(@NonNull String documentUrl, @Nullable CallBack callBack) {
        execute(Preconditions.checkNotNull(documentUrl), callBack);
    }

    /**
     * 执行检测更新
     *
     * @param parames
     */
    private void execute(Object... parames) {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new CheckForUpdatesTask();
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
     * 执行升级操作
     *
     * @param upgradeOptions 下载选项
     */
    public void executeUpgrade(UpgradeOptions upgradeOptions) {
        UpgradeService.start(activity, upgradeOptions);
    }

    public interface CallBack {

        void succeed(Upgrade upgrade);

        void failed(String message);
    }

    /**
     * 检测更新异步任务
     */
    private class CheckForUpdatesTask extends AsyncTask<Object, Void, Message> {
        private final int RESULT_SUCCEED = 0x1025;
        private final int RESULT_FAILED = 0x1026;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Message doInBackground(Object... params) {
            Message message = new Message();
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("upgrade", Upgrade.parser(Preconditions.checkNotNull((String) params[0])));
                message.setData(bundle);
                message.what = RESULT_SUCCEED;
            } catch (Exception e) {
                e.printStackTrace();
                message.what = RESULT_FAILED;
            }
            message.obj = params[1];
            return message;
        }

        @Override
        protected void onCancelled(Message message) {
            super.onCancelled(message);
        }

        @Override
        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            switch (message.what) {
                case RESULT_SUCCEED:
                    Upgrade upgrade = message.getData().getParcelable("upgrade");
                    if (message.obj instanceof Boolean) {
                        handlerResult(upgrade, (boolean) message.obj);
                    } else {
                        handlerResult(upgrade, (CallBack) message.obj);
                    }
                    break;
                case RESULT_FAILED:
                    if (message.obj instanceof Boolean) {
                        handlerResult(null, (boolean) message.obj);
                    } else {
                        handlerResult(null, (CallBack) message.obj);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * 处理结果
         *
         * @param upgrade
         * @param isAutoCheck
         */
        private void handlerResult(Upgrade upgrade, boolean isAutoCheck) {
            if (upgrade == null) {
                if (!isAutoCheck) {
                    Toast.makeText(activity, activity.getString(R.string.check_for_update_failure), Toast.LENGTH_SHORT).show();
                }
                return;
            }

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

            UpgradeDialog.newInstance(activity, upgrade).show();
        }

        /**
         * 处理结果
         *
         * @param upgrade
         * @param callBack
         */
        private void handlerResult(Upgrade upgrade, CallBack callBack) {
            if (callBack == null) {
                return;
            }

            if (upgrade == null) {
                callBack.failed(activity.getString(R.string.check_for_update_failure));
                return;
            }

            if (upgrade.getVersionCode() <= Util.getVersionCode(activity)) {
                callBack.failed(activity.getString(R.string.check_for_update_notfound));
                return;
            }

            if (upgrade.getMode() == Upgrade.UPGRADE_MODE_PARTIAL && !upgrade.getDevice().contains(Util.getSerial())) {
                callBack.failed(activity.getString(R.string.check_for_update_notfound));
                return;
            }

            if (Historical.isIgnoreVersion(activity, upgrade.getVersionCode())) {
                callBack.failed(activity.getString(R.string.check_for_update_notfound));
                return;
            }
            callBack.succeed(upgrade);
        }

    }
}
