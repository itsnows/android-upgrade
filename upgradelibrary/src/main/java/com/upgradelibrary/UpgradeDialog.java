package com.upgradelibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.util.Preconditions;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.upgradelibrary.bean.Upgrade;
import com.upgradelibrary.bean.UpgradeOptions;
import com.upgradelibrary.service.UpgradeService;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/12 15:29
 * <p>
 * UpgradeDialog
 */

public class UpgradeDialog extends AlertDialog implements View.OnClickListener, ServiceConnection {
    public static final String TAG = UpgradeDialog.class.getSimpleName();
    private AppCompatTextView tvTitle;
    private AppCompatTextView tvDate;
    private AppCompatTextView tvVersions;
    private AppCompatTextView tvLogs;
    private AppCompatButton btnNegative;
    private AppCompatButton btnNeutral;
    private AppCompatButton btnPositive;
    private UpgradeService upgradeService;
    private LinearLayoutCompat linearLayoutCompat;
    private ProgressBar progressView;

    @NonNull
    private Upgrade upgrade;

    private UpgradeDialog(@NonNull Context context) {
        super(context);
    }

    private UpgradeDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    private UpgradeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * @param context Context
     * @param upgrade 更新实体
     * @return
     */
    public static UpgradeDialog newInstance(@NonNull Context context, @NonNull Upgrade upgrade) {
        Preconditions.checkNotNull(upgrade);
        UpgradeDialog upgradeDialog = new UpgradeDialog(context);
        upgradeDialog.initArgs(upgrade);
        return upgradeDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upgrade);
        initView();
    }

    private void initArgs(Upgrade upgrade) {
        this.upgrade = upgrade;
    }

    private void initView() {
        tvTitle = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_title);
        tvDate = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_date);
        tvVersions = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_version);
        tvLogs = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_logs);
        btnNegative = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_negative);
        btnNeutral = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_neutral);
        btnPositive = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_positive);
        progressView = (ProgressBar) findViewById(R.id.pb_dialog_upgrade_progress);

        progressView.setMax(100);
        progressView.setVisibility(View.GONE);

        tvTitle.setText(getString(R.string.dialog_upgrade_title));
        tvDate.setText(getString(R.string.dialog_upgrade_date, upgrade.getDate()));
        tvVersions.setText(getString(R.string.dialog_upgrade_versions, upgrade.getVersionName()));

        tvLogs.setText(getLogs());
        btnNeutral.setOnClickListener(this);
        btnNegative.setOnClickListener(this);
        btnPositive.setOnClickListener(this);

        if (upgrade.getMode() == Upgrade.UPGRADE_MODE_FORCED) {
            btnNeutral.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);
            setCancelable(false);
        }
    }

    private String getString(@StringRes int id) {
        return getContext().getResources().getString(id);
    }

    private String getString(@StringRes int id, Object... formatArgs) {
        return getContext().getResources().getString(id, formatArgs);
    }

    private String getLogs() {
        StringBuilder logs = new StringBuilder();
        for (int i = 0; i < this.upgrade.getLogs().size(); i++) {
            logs.append(this.upgrade.getLogs().get(i));
            logs.append(i < this.upgrade.getLogs().size() - 1 ? "\n" : "");
        }
        return logs.toString();
    }

    private void ignoreUpgrade() {
        Historical.setIgnoreVersion(getContext(), upgrade.getVersionCode());
    }

    private void executeUpgrade() {
        UpgradeService.start(getContext(), new UpgradeOptions.Builder()
                .setUrl(upgrade.getDowanloadUrl())
                .setMd5(upgrade.getMd5())
                .setMutiThreadEnabled(false)
                .setMaxThreadPools(10)
                .build(), this);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        if (upgradeService != null) {
            getContext().unbindService(this);
        }
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_dialog_upgrade_negative) {
            dismiss();
            return;
        }

        if (id == R.id.btn_dialog_upgrade_neutral) {
            ignoreUpgrade();
            dismiss();
            return;
        }

        if (id == R.id.btn_dialog_upgrade_positive) {
            if (Util.mayRequestExternalStorage(getContext())) {
                executeUpgrade();
                if (progressView.getVisibility() == View.GONE) {
                    progressView.setVisibility(View.VISIBLE);
                }
                if (upgrade.getMode() != Upgrade.UPGRADE_MODE_FORCED) {
                    dismiss();
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        upgradeService = ((UpgradeService.UpgradeServiceBinder) service).getUpgradeService();
        upgradeService.setOnDownloadListener(new UpgradeService.OnDownloadListener() {

            @Override
            public void onStart() {
                super.onStart();
                Log.d(TAG, "onStart");
            }

            @Override
            public void onProgress(long progress, long maxProgress) {
                Log.d(TAG, "onProgress：" + Util.formatByte(progress) + "/" + Util.formatByte(maxProgress));
                progressView.setProgress((int) (progress * 100 / maxProgress));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(TAG, "onPause");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError() {
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        upgradeService = null;
    }

}
