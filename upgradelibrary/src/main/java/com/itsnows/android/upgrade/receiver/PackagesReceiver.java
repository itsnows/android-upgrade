package com.itsnows.android.upgrade.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.itsnows.android.upgrade.UpgradeLogger;
import com.itsnows.android.upgrade.UpgradeUtil;
import com.itsnows.android.upgrade.model.UpgradeRepository;
import com.itsnows.android.upgrade.model.bean.UpgradeVersion;

/**
 * PackagesReceiver
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 20-4-22 下午2:43
 */
public class PackagesReceiver extends BroadcastReceiver {
    private static final String TAG = PackagesReceiver.class.getSimpleName();

    @Override
    public final void onReceive(Context context, Intent intent) {
        String packageName = null;
        if (intent.getData() != null) {
            packageName = intent.getData().getSchemeSpecificPart();
        }
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            UpgradeLogger.i(TAG, "onReceive：Added " + packageName);
            onPackageAdded(context, packageName);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            UpgradeLogger.i(TAG, "onReceive：Replaced " + packageName);
            onPackageReplaced(context, packageName);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            UpgradeLogger.i(TAG, "onReceive：Removed " + packageName);
            onPackageRemoved(context, packageName);
        }
    }

    public final void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        context.registerReceiver(this, intentFilter);
    }

    public final void unregisterReceiver(Context context) {
        context.unregisterReceiver(this);
    }

    /**
     * Application package added.
     *
     * @param context
     * @param packageName
     */
    public void onPackageAdded(Context context, String packageName) {
    }

    /**
     * Application package replaced.
     *
     * @param context
     * @param packageName
     */
    public void onPackageReplaced(Context context, String packageName) {
        if (context.getPackageName().equals(packageName)) {
            UpgradeRepository repository = UpgradeRepository.getInstance(context);
            UpgradeVersion upgradeVersion = repository.getUpgradeVersion(UpgradeUtil.getVersionCode(context));
            UpgradeUtil.rebootApp(context, context.getPackageName());
        }
    }

    /**
     * Application package removed.
     *
     * @param context
     * @param packageName
     */
    public void onPackageRemoved(Context context, String packageName) {

    }

}
