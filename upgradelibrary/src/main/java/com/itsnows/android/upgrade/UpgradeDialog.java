package com.itsnows.android.upgrade;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Preconditions;

import com.itsnows.android.upgrade.model.UpgradeRepository;
import com.itsnows.android.upgrade.model.bean.Upgrade;
import com.itsnows.android.upgrade.model.bean.UpgradeOptions;
import com.itsnows.android.upgrade.model.bean.UpgradeVersion;
import com.itsnows.android.upgrade.service.UpgradeService;
import com.itsnows.upgrade.R;

/**
 * UpgradeDialog
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 2018/1/12 15:29
 */
public class UpgradeDialog extends AlertDialog implements View.OnClickListener {
    private static final String TAG = UpgradeDialog.class.getSimpleName();
    private final Activity activity;
    @NonNull
    private final Upgrade upgrade;
    @NonNull
    private final UpgradeOptions upgradeOptions;
    private LinearLayoutCompat llHeadBar;
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
    private UpgradeClient upgradeClient;
    private boolean isRequestPermission;

    private UpgradeDialog(@NonNull Context context, @NonNull Upgrade upgrade,
                          @NonNull UpgradeOptions upgradeOptions) {
        super(context);
        this.activity = (Activity) context;
        this.upgrade = upgrade;
        this.upgradeOptions = upgradeOptions;
    }

    /**
     * 创建更新对话框
     *
     * @param activity
     * @param upgrade
     * @param upgradeOptions
     * @return
     */
    @SuppressLint("RestrictedApi")
    public static UpgradeDialog newInstance(@NonNull Activity activity, @NonNull Upgrade upgrade,
                                            @NonNull UpgradeOptions upgradeOptions) {
        Preconditions.checkNotNull(upgrade);
        Preconditions.checkNotNull(upgradeOptions);
        return new UpgradeDialog(activity, upgrade, upgradeOptions);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            return;
        }

        if (!isRequestPermission) {
            return;
        }

        if (!UpgradeUtil.mayRequestExternalStorage(activity, false)) {
            return;
        }

        executeUpgrade();
        isRequestPermission = false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upgrade);
        initView();
        initUpgrade();
    }

    private void initView() {
        llHeadBar = findViewById(R.id.ll_dialog_upgrade_head_bar);
        tvTitle = findViewById(R.id.tv_dialog_upgrade_title);
        tvDate = findViewById(R.id.tv_dialog_upgrade_date);
        tvVersions = findViewById(R.id.tv_dialog_upgrade_version);
        tvLogs = findViewById(R.id.tv_dialog_upgrade_logs);

        vDoneButton = findViewById(R.id.v_dialog_upgrade_done_button);
        btnNegative = findViewById(R.id.btn_dialog_upgrade_negative);
        btnNeutral = findViewById(R.id.btn_dialog_upgrade_neutral);
        btnPositive = findViewById(R.id.btn_dialog_upgrade_positive);

        vProgress = findViewById(R.id.v_dialog_upgrade_progress);
        tvProgress = findViewById(R.id.tv_dialog_upgrade_progress);
        pbProgressBar = findViewById(R.id.pb_dialog_upgrade_progressbar);
        btnProgress = findViewById(R.id.btn_dialog_upgrade_progress);

        llHeadBar.setBackground(getHeadDrawable());
        pbProgressBar.setProgressDrawable(getProgressDrawable());
        tvTitle.setText(getString(R.string.dialog_upgrade_title));
        tvDate.setText(getString(R.string.dialog_upgrade_date, getDate()));
        tvVersions.setText(getString(R.string.dialog_upgrade_versions, getVersionName()));
        tvLogs.setText(getLogs());
        tvProgress.setText(getString(R.string.dialog_upgrade_progress, 0));
        tvProgress.setTextColor(getColorAccent());
        btnPositive.setTextColor(getColorAccent());
        btnProgress.setTextColor(getAccentColorStateList());
        btnNeutral.setOnClickListener(this);
        btnNegative.setOnClickListener(this);
        btnPositive.setOnClickListener(this);
        btnProgress.setOnClickListener(this);
        if (getMode() == Upgrade.UPGRADE_MODE_FORCED) {
            btnNeutral.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);
            setCancelable(false);
        }

        showDoneButton();

    }

    private void initUpgrade() {
        upgradeClient = UpgradeClient.add(activity, upgradeOptions);
        upgradeClient.setOnConnectListener(new OnConnectListener() {
            @Override
            public void onConnected() {
                showProgress();
            }

            @Override
            public void onDisconnected() {

            }
        });
        upgradeClient.setOnDownloadListener(new OnDownloadListener() {
            @Override
            public void onStart() {
                super.onStart();
                btnProgress.setEnabled(true);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_pause));
                btnProgress.setTag(UpgradeConstant.CODE_DOWNLOAD_START);
                UpgradeLogger.d(TAG, "Download start");
            }

            @Override
            public void onProgress(long max, long progress) {
                int tempProgress = (int) ((float) progress / max * 100);
                if (tempProgress > pbProgressBar.getProgress()) {
                    tvProgress.setText(getString(R.string.dialog_upgrade_progress,
                            Math.min(tempProgress, 100)));
                    pbProgressBar.setProgress(Math.min(tempProgress, 100));
                }
                btnProgress.setEnabled(true);
                btnProgress.setTag(UpgradeConstant.CODE_DOWNLOAD_PROGRESS);
                UpgradeLogger.d(TAG, "Download Progress");
            }

            @Override
            public void onPause() {
                super.onPause();
                btnProgress.setEnabled(true);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_resume));
                btnProgress.setTag(UpgradeConstant.CODE_DOWNLOAD_PAUSE);
                UpgradeLogger.d(TAG, "Download pause");
            }

            @Override
            public void onCancel() {
                dismiss();
                btnProgress.setEnabled(true);
                btnProgress.setTag(UpgradeConstant.CODE_DOWNLOAD_CANCEL);
                UpgradeLogger.d(TAG, "Download cancel");
            }

            @Override
            public void onError(UpgradeException e) {
                btnProgress.setEnabled(true);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_resume));
                UpgradeLogger.d(TAG, "Download cancel");
            }

            @Override
            public void onComplete() {
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_complete));
                btnProgress.setTag(UpgradeConstant.CODE_DOWNLOAD_COMPLETE);
            }
        });
        upgradeClient.setOnInstallListener(new OnInstallListener() {

            @Override
            public void onValidate() {
                btnProgress.setEnabled(false);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_check));
                btnProgress.setTag(UpgradeConstant.CODE_INSTALL_VALIDATE);
            }

            @Override
            public void onStart() {
                btnProgress.setEnabled(false);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_install));
                btnProgress.setTag(UpgradeConstant.CODE_INSTALL_START);
            }

            @Override
            public void onCancel() {
                btnProgress.setEnabled(true);
                btnProgress.setText(getString(R.string.dialog_upgrade_btn_reset));
                btnProgress.setTag(UpgradeConstant.CODE_INSTALL_CANCEL);
            }

            @Override
            public void onError(UpgradeException e) {
                btnProgress.setEnabled(true);
                if (e.getCode() == UpgradeException.ERROR_CODE_PACKAGE_INVALID) {
                    btnProgress.setText(String.format("%1$s，%2$s",
                            getString(R.string.message_install_package_invalid),
                            getString(R.string.dialog_upgrade_btn_reset)));
                }
                if (e.getCode() == UpgradeException.ERROR_CODE_BACKGROUND_INSTALL_FAIL) {
                    btnProgress.setText(String.format("%1$s，%2$s",
                            getString(R.string.message_install_error),
                            getString(R.string.dialog_upgrade_btn_reset)));
                }
                btnProgress.setTag(UpgradeConstant.CODE_INSTALL_ERROR);
            }

            @Override
            public void onComplete() {
                btnProgress.setEnabled(true);
                btnProgress.setText(String.format("%1$s，%2$s",
                        getString(R.string.message_install_complete),
                        getString(R.string.dialog_upgrade_btn_launch)));
                btnProgress.setTag(UpgradeConstant.CODE_INSTALL_COMPLETE);
            }
        });

        if (UpgradeUtil.isServiceRunning(getContext(), UpgradeService.class.getName())) {
            UpgradeLogger.d(TAG, "isServiceRunning");
            executeUpgrade();
        }
    }

    private String getString(@StringRes int id) {
        return getContext().getResources().getString(id);
    }

    private String getString(@StringRes int id, Object... formatArgs) {
        return getContext().getResources().getString(id, formatArgs);
    }

    private int getColorAccent() {
        if (upgradeOptions.getTheme() != 0) return upgradeOptions.getTheme();
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    private int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    private Drawable getHeadDrawable() {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(getColorAccent());
        gd.setCornerRadii(new float[]{
                getContext().getResources().getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius),
                getContext().getResources().getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius),
                getContext().getResources().getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius),
                getContext().getResources().getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius),
                0, 0, 0, 0});
        return gd;
    }

    private Drawable getProgressDrawable() {
        GradientDrawable gd1 = new GradientDrawable();
        gd1.setShape(GradientDrawable.RECTANGLE);
        gd1.setColor(ContextCompat.getColor(getContext(),
                R.color.dialog_upgrade_progress_background_color));
        gd1.setCornerRadius(getContext().getResources().
                getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius));

        int color = getColorAccent();
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = (color & 0x000000ff);
        int[] colors = new int[]{
                Color.argb(0x42, red, green, blue),
                Color.argb(0x8a, red, green, blue),
                Color.argb(alpha, red, green, blue)};
        GradientDrawable gd2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        gd2.setCornerRadius(getContext().getResources().
                getDimensionPixelOffset(R.dimen.dialog_upgrade_corner_radius));
        ClipDrawable cd2 = new ClipDrawable(gd2, Gravity.START, ClipDrawable.HORIZONTAL);
        LayerDrawable ld1 = new LayerDrawable(new Drawable[]{gd1, cd2});
        ld1.setId(0, android.R.id.background);
        ld1.setId(1, android.R.id.progress);
        return ld1;
    }

    private ColorStateList getAccentColorStateList() {
        int color = getColorAccent();
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = (color & 0x000000ff);
        int[] colors = new int[]{color,
                Color.argb(0x4d, red, green, blue)};
        int[][] states = new int[][]{
                {android.R.attr.state_enabled, 0},
                {}};
        return new ColorStateList(states, colors);
    }

    /**
     * 获取更新模式
     *
     * @return
     */
    private int getMode() {
        if (upgrade.getStable() != null) {
            return upgrade.getStable().getMode();
        }
        if (upgrade.getBeta() != null) {
            return upgrade.getBeta().getMode();
        }
        return Upgrade.UPGRADE_MODE_COMMON;
    }

    /**
     * 获取更新日期
     *
     * @return
     */
    private String getDate() {
        if (upgrade.getStable() != null) {
            return upgrade.getStable().getDate();
        }
        if (upgrade.getBeta() != null) {
            return upgrade.getBeta().getDate();
        }
        return "";
    }

    /**
     * 获取更新版本名称
     *
     * @return
     */
    private String getVersionName() {
        if (upgrade.getStable() != null) {
            return upgrade.getStable().getVersionName();
        }
        if (upgrade.getBeta() != null) {
            return upgrade.getBeta().getVersionName();
        }
        return "";
    }

    /**
     * 获取更新日志
     *
     * @return
     */
    private String getLogs() {
        StringBuilder logs = new StringBuilder();
        if (upgrade.getStable() != null) {
            for (int i = 0; i < this.upgrade.getStable().getLogs().size(); i++) {
                logs.append(this.upgrade.getStable().getLogs().get(i));
                logs.append(i < this.upgrade.getStable().getLogs().size() - 1 ? "\n" : "");
            }
            return logs.toString();
        }
        if (upgrade.getBeta() != null) {
            for (int i = 0; i < this.upgrade.getBeta().getLogs().size(); i++) {
                logs.append(this.upgrade.getBeta().getLogs().get(i));
                logs.append(i < this.upgrade.getBeta().getLogs().size() - 1 ? "\n" : "");
            }
            return logs.toString();
        }
        return "";
    }

    /**
     * 显示完成按钮
     */
    private void showDoneButton() {
        if (vProgress.getVisibility() == View.VISIBLE) {
            vProgress.setVisibility(View.GONE);
        }
        if (vDoneButton.getVisibility() == View.GONE) {
            vDoneButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示下载进度
     */
    private void showProgress() {
        if (vDoneButton.getVisibility() == View.VISIBLE) {
            vDoneButton.setVisibility(View.GONE);
        }
        if (vProgress.getVisibility() == View.GONE) {
            vProgress.setVisibility(View.VISIBLE);
            btnProgress.setEnabled(false);
        }
    }

    /**
     * 忽略升级
     */
    private void ignoreUpgrade() {
        UpgradeRepository repository = UpgradeRepository.getInstance(getContext());
        if (upgrade.getStable() != null) {
            UpgradeVersion upgradeVersion = new UpgradeVersion();
            upgradeVersion.setVersion(upgrade.getStable().getVersionCode());
            upgradeVersion.setIgnored(true);
            repository.putUpgradeVersion(upgradeVersion);
            return;
        }
        if (upgrade.getBeta() != null) {
            UpgradeVersion upgradeVersion = new UpgradeVersion();
            upgradeVersion.setVersion(upgrade.getBeta().getVersionCode());
            upgradeVersion.setIgnored(true);
            repository.putUpgradeVersion(upgradeVersion);
            return;
        }
        UpgradeLogger.i(TAG, "Execute ignore upgrade failure");
    }

    /**
     * 执行升级
     */
    private void executeUpgrade() {
        upgradeClient.start();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        upgradeClient.remove();
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
            if (!UpgradeUtil.mayRequestExternalStorage(activity, true)) {
                isRequestPermission = true;
                return;
            }
            executeUpgrade();
        } else if (id == R.id.btn_dialog_upgrade_progress) {
            if (upgradeClient == null) {
                return;
            }
            int tag = (int) v.getTag();
            if (tag == UpgradeConstant.CODE_DOWNLOAD_START
                    || tag == UpgradeConstant.CODE_DOWNLOAD_PROGRESS) {
                upgradeClient.pause();
            } else if (tag == UpgradeConstant.CODE_DOWNLOAD_PAUSE
                    || tag == UpgradeConstant.CODE_DOWNLOAD_ERROR) {
                upgradeClient.resume();
            } else if (tag == UpgradeConstant.CODE_DOWNLOAD_COMPLETE) {
                upgradeClient.install();
                if (getMode() != Upgrade.UPGRADE_MODE_FORCED) {
                    dismiss();
                }
            } else if (tag == UpgradeConstant.CODE_INSTALL_ERROR) {
                upgradeClient.resume();
            } else if (tag == UpgradeConstant.CODE_INSTALL_COMPLETE) {
                upgradeClient.reboot();
                dismiss();
            }
        }
    }
}
