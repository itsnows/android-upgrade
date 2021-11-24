package com.itsnows.android.upgrade;

import com.itsnows.android.upgrade.model.bean.Upgrade;

/**
 * OnUpgradeListener
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 19-5-17 上午11:16
 */
public interface OnUpgradeListener {

    void onUpdateAvailable(UpgradeClient client);

    void onUpdateAvailable(Upgrade.Stable stable, UpgradeClient client);

    void onUpdateAvailable(Upgrade.Beta bate, UpgradeClient client);

    void onNoUpdateAvailable(String message);

}
