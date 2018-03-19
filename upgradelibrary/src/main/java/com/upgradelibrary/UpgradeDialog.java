package com.upgradelibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.util.Preconditions;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
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

public class UpgradeDialog extends AlertDialog implements View.OnClickListener, UpgradeServiceManager.OnBinderUpgradeServiceLisenter {
    public static final String TAG = UpgradeDialog.class.getSimpleName();
    private AppCompatTextView tvTitle;
    private AppCompatTextView tvDate;
    private AppCompatTextView tvVersions;
    private AppCompatTextView tvLogs;

    private View vDoneButton;
    private AppCompatButton btnNegative;
    private AppCompatButton btnNeutral;
    private AppCompatButton btnPositive;

    private View vProgress;
    private AppCompatTextView tvProgress;
    private ProgressBar pbProgressBar;
    private AppCompatButton btnProgress;

    private Activity activity;
    @NonNull
    private Upgrade upgrade;
    private UpgradeService upgradeService;
    private UpgradeServiceManager upgradeServiceManager;

    private UpgradeDialog(@NonNull Context context) {
        super(context);
        this.activity = (Activity) context;
    }

    private UpgradeDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.activity = (Activity) context;
    }

    private UpgradeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.activity = (Activity) context;
    }

    /**
     * @param activity Activity
     * @param upgrade  更新实体
     * @return
     */
    @SuppressLint("RestrictedApi")
    public static UpgradeDialog newInstance(@NonNull Activity activity, @NonNull Upgrade upgrade, UpgradeOptions upgradeOptions) {
        Preconditions.checkNotNull(upgrade);
        Preconditions.checkNotNull(upgradeOptions);
        UpgradeDialog upgradeDialog = new UpgradeDialog(activity);
        upgradeDialog.initArgs(upgrade, upgradeOptions);
        return upgradeDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upgrade);
        initView();
    }

    private void initArgs(Upgrade upgrade, UpgradeOptions upgradeOptions) {
        this.upgrade = upgrade;
        this.upgradeServiceManager = new UpgradeServiceManager(activity, upgradeOptions);
    }

    private void initView() {
        tvTitle = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_title);
        tvDate = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_date);
        tvVersions = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_version);
        tvLogs = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_logs);

        vDoneButton = findViewById(R.id.v_dialog_upgrade_done_button);
        btnNegative = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_negative);
        btnNeutral = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_neutral);
        btnPositive = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_positive);

        vProgress = findViewById(R.id.v_dialog_upgrade_progress);
        tvProgress = (AppCompatTextView) findViewById(R.id.tv_dialog_upgrade_progress);
        pbProgressBar = (ProgressBar) findViewById(R.id.pb_dialog_upgrade_progressbar);
        btnProgress = (AppCompatButton) findViewById(R.id.btn_dialog_upgrade_progress);

        tvTitle.setText(getString(R.string.dialog_upgrade_title));
        tvDate.setText(getString(R.string.dialog_upgrade_date, upgrade.getDate()));
        tvVersions.setText(getString(R.string.dialog_upgrade_versions, upgrade.getVersionName()));
        tvLogs.setText(getLogs());
        tvProgress.setText(getString(R.string.dialog_upgrade_progress, 0));
        btnProgress.setEnabled(false);
        btnNeutral.setOnClickListener(this);
        btnNegative.setOnClickListener(this);
        btnPositive.setOnClickListener(this);
        btnProgress.setOnClickListener(this);

        if (upgrade.getMode() == Upgrade.UPGRADE_MODE_FORCED) {
            btnNeutral.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);
            setCancelable(false);
        }

        showDoneButton();
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

    private void showDoneButton() {
        if (vProgress.getVisibility() == View.VISIBLE) {
            vProgress.setVisibility(View.GONE);
        }
        if (vDoneButton.getVisibility() == View.GONE) {
            vDoneButton.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress() {
        if (vDoneButton.getVisibility() == View.VISIBLE) {
            vDoneButton.setVisibility(View.GONE);
        }
        if (vProgress.getVisibility() == View.GONE) {
            vProgress.setVisibility(View.VISIBLE);
        }
    }

    private void ignoreUpgrade() {
        Historical.setIgnoreVersion(getContext(), upgrade.getVersionCode());
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        upgradeServiceManager.unbinder();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_dialog_upgrade_negative) {
            dismiss();
        } else if (id == R.id.btn_dialog_upgrade_neutral) {
            dismiss();
            ignoreUpgrade();
        } else if (id == R.id.btn_dialog_upgrade_positive) {
            if (Util.mayRequestExternalStorage(activity)) {
                if (upgrade.getMode() != Upgrade.UPGRADE_MODE_FORCED) {
                    dismiss();
                    showProgress();
                }
                upgradeServiceManager.setOnBinderUpgradeServiceLisenter(this);
                upgradeServiceManager.binder();
            }
        } else if (id == R.id.btn_dialog_upgrade_progress) {
            if (upgradeService == null) {
                return;
            }
            String tag = (String) v.getTag();
            if ("onStart".equals(tag) || "onProgress".equals(tag)) {
                upgradeService.pause();
            } else if ("onPause".equals(tag) || "onError".equals(tag)) {
                upgradeService.resume();
            } else if ("onComplete".equals(tag)) {
                dismiss();
                upgradeService.complete();
            }
        }
    }

    @Override
    public void onBinder(UpgradeService upgradeService) {
        UpgradeDialog.this.upgradeService = upgradeService;
        showProgress();
        upgradeService.setOnDownloadListener(new UpgradeService.OnDownloadListener() {

            @Override
            public void onStart() {
                super.onStart();
                btnProgress.setEnabled(true);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_pause));
                btnProgress.setTag("onStart");
                Log.d(TAG, "onStart");
            }

            @Override
            public void onProgress(long progress, long maxProgress) {
                int tempProgress = (int) ((float) progress / maxProgress * 100);
                if (tempProgress > pbProgressBar.getProgress()) {
                    tvProgress.setText(getString(R.string.dialog_upgrade_progress, tempProgress > 100 ? 100 : tempProgress));
                    pbProgressBar.setProgress(tempProgress > 100 ? 100 : tempProgress);
                }
                btnProgress.setTag("onProgress");
                Log.d(TAG, "onProgress：" + Util.formatByte(progress) + "/" + Util.formatByte(maxProgress));
            }

            @Override
            public void onPause() {
                super.onPause();
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_resume));
                btnProgress.setTag("onPause");
                Log.d(TAG, "onPause");
            }

            @Override
            public void onCancel() {
                dismiss();
                btnProgress.setTag("onCancel");
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError() {
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_resume));
                btnProgress.setTag("onError");
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_complete));
                btnProgress.setTag("onComplete");
                Log.d(TAG, "onComplete");
            }
        });
    }

    @Override
    public void onUnbinder() {
        Log.d(TAG, "onUnbinder");
    }
}
