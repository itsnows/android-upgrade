package com.itsnows.upgrade.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.itsnows.upgrade.model.bean.UpgradeOptions;
import com.itsnows.upgrade.service.UpgradeService;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/3/19 11:09
 * <p>
 * UpgradeClient
 */
public class UpgradeClient {
    private Activity activity;
    private UpgradeOptions upgradeOptions;
    private UpgradeService upgradeService;
    private OnBinderServiceLisenter onBinderServiceLisenter;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            upgradeService = ((UpgradeService.UpgradeServiceBinder) service).getUpgradeService();
            if (onBinderServiceLisenter != null) {
                onBinderServiceLisenter.onBinder(upgradeService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (onBinderServiceLisenter != null) {
                onBinderServiceLisenter.onUnbinder();
            }
        }
    };

    public UpgradeClient(Activity activity, UpgradeOptions upgradeOptions) {
        this.activity = activity;
        this.upgradeOptions = upgradeOptions;
    }

    public void setOnBinderServiceLisenter(OnBinderServiceLisenter onBinderServiceLisenter) {
        this.onBinderServiceLisenter = onBinderServiceLisenter;
    }

    public void start() {
        UpgradeService.launch(activity, upgradeOptions);
    }

    public void binder() {
        UpgradeService.launch(activity, upgradeOptions, serviceConnection);
    }

    public void unbinder() {
        if (upgradeService != null) {
            activity.unbindService(serviceConnection);
        }
    }

    public interface OnBinderServiceLisenter {

        void onBinder(UpgradeService upgradeService);

        void onUnbinder();
    }

}
