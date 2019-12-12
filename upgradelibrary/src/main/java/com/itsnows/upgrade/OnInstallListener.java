package com.itsnows.upgrade;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-5-17 上午8:54
 * <p>
 * OnInstallListener
 */
public interface OnInstallListener {

    void onCheck();

    void onStart();

    void onCancel();

    void onError(UpgradeException e);

    void onComplete();

}
