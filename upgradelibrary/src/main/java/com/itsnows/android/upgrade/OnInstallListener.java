package com.itsnows.android.upgrade;

/**
 * OnInstallListener
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 19-5-17 上午8:54
 */
public interface OnInstallListener {

    void onValidate();

    void onStart();

    void onCancel();

    void onError(UpgradeException e);

    void onComplete();

}
