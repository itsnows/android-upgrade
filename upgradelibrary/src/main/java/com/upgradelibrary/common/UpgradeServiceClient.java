package com.upgradelibrary.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.upgradelibrary.data.bean.UpgradeOptions;
import com.upgradelibrary.service.UpgradeService;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/3/19 11:09
 * <p>
 * UpgradeServiceClient
 */
public class UpgradeServiceClient {
    private Activity activity;
    private UpgradeOptions upgradeOptions;
    private UpgradeService upgradeService;
    private OnBinderUpgradeServiceLisenter onBinderUpgradeServiceLisenter;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            upgradeService = ((UpgradeService.UpgradeServiceBinder) service).getUpgradeService();
            if (onBinderUpgradeServiceLisenter != null) {
                onBinderUpgradeServiceLisenter.onBinder(upgradeService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (onBinderUpgradeServiceLisenter != null) {
                onBinderUpgradeServiceLisenter.onUnbinder();
            }
        }
    };

    public UpgradeServiceClient(Activity activity, UpgradeOptions upgradeOptions) {
        this.activity = activity;
        this.upgradeOptions = upgradeOptions;
    }

    public void setOnBinderUpgradeServiceLisenter(OnBinderUpgradeServiceLisenter onBinderUpgradeServiceLisenter) {
        this.onBinderUpgradeServiceLisenter = onBinderUpgradeServiceLisenter;
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

    public interface OnBinderUpgradeServiceLisenter {

        void onBinder(UpgradeService upgradeService);

        void onUnbinder();
    }

}
