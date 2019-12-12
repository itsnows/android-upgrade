package com.itsnows.upgrade;

import com.itsnows.upgrade.model.bean.Upgrade;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-5-17 上午11:16
 * <p>
 * OnUpgradeListener
 */
public interface OnUpgradeListener {

    void onUpdateAvailable(UpgradeClient client);

    void onUpdateAvailable(Upgrade.Stable stable, UpgradeClient client);

    void onUpdateAvailable(Upgrade.Beta bate, UpgradeClient client);

    void onNoUpdateAvailable(String message);

}
