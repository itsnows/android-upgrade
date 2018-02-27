package com.upgradelibrary;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Preconditions;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/12 15:29
 * <p>
 * UpgradeDialog
 */

public class UpgradeDialog extends DialogFragment implements View.OnClickListener, ServiceConnection {
    public static final String TAG = UpgradeDialog.class.getSimpleName();
    private AppCompatTextView tvTitle;
    private AppCompatTextView tvDate;
    private AppCompatTextView tvVersions;
    private AppCompatTextView tvLogs;
    private AppCompatButton btnNegative;
    private AppCompatButton btnNeutral;
    private AppCompatButton btnPositive;
    private UpgradeService upgradeService;

    @NonNull
    private Upgrade upgrade;

    /**
     * @param upgrade 更新实体
     * @return
     */
    public static UpgradeDialog newInstance(@NonNull Upgrade upgrade) {
        Preconditions.checkNotNull(upgrade);
        UpgradeDialog upgradeDialog = new UpgradeDialog();
        upgradeDialog.initArgs(upgrade);
        return upgradeDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_upgrade, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initArgs(Upgrade upgrade) {
        this.upgrade = upgrade;
    }

    private void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_upgrade_title);
        tvDate = view.findViewById(R.id.tv_dialog_upgrade_date);
        tvVersions = view.findViewById(R.id.tv_dialog_upgrade_version);
        tvLogs = view.findViewById(R.id.tv_dialog_upgrade_logs);
        btnNegative = view.findViewById(R.id.btn_dialog_upgrade_negative);
        btnNeutral = view.findViewById(R.id.btn_dialog_upgrade_neutral);
        btnPositive = view.findViewById(R.id.btn_dialog_upgrade_positive);

        tvTitle.setText(getString(R.string.dialog_upgrade_title));
        tvDate.setText(getString(R.string.dialog_upgrade_date, upgrade.getDate()));
        tvVersions.setText(getString(R.string.dialog_upgrade_versions, upgrade.getVersionName()));
        StringBuilder logs = new StringBuilder();
        for (int i = 0; i < this.upgrade.getLogs().size(); i++) {
            logs.append(this.upgrade.getLogs().get(i)).append(i < this.upgrade.getLogs().size() - 1 ? "\n" : "");
        }
        tvLogs.setText(logs.toString());
        btnNeutral.setOnClickListener(this);
        btnNegative.setOnClickListener(this);
        btnPositive.setOnClickListener(this);

        if (upgrade.getMode() == Upgrade.UPGRADE_MODE_FORCED) {
            setCancelable(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        executeUpgrade();
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    @Override
    @Deprecated
    public final void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    public void show(FragmentTransaction transaction) {
        super.show(transaction, TAG);
    }

    @Override
    @Deprecated
    public final int show(FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag);
    }

    @Override
    public void dismiss() {
        if (!isCancelable()) {
            return;
        }
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (upgradeService != null) {
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_dialog_upgrade_negative) {
            dismiss();
            UpgradeHistorical.setIgnoreVersion(getActivity(), upgrade.getVersionCode());
        } else if (i == R.id.btn_dialog_upgrade_neutral) {
            dismiss();
        } else if (i == R.id.btn_dialog_upgrade_positive) {
            if (Util.mayRequestExternalStorage(getActivity())) {
                dismiss();
                executeUpgrade();
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

    private void executeUpgrade() {
        UpgradeService.start(getActivity(), new UpgradeOptions.Builder()
                .setUrl(upgrade.getDowanloadUrl())
                .setMd5(upgrade.getMd5())
                .setMutiThreadEnabled(false)
                .setMaxThreadPools(10)
                .build(), this);
    }
}
