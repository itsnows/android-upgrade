package com.itsnows.upgrade.model;

import com.itsnows.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.upgrade.model.bean.UpgradeVersion;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/2/6 20:41
 * <p>
 * UpgradeDataSource
 */
public interface UpgradeDataSource {

    /**
     * 获取上级版本
     *
     * @param version
     * @return
     */
    UpgradeVersion getUpgradeVersion(int version);

    /**
     * 忽略版本
     *
     * @param version
     */
    void setUpgradeVersion(UpgradeVersion version);

    /**
     * 获取升级缓存
     *
     * @param url
     * @return
     */
    UpgradeBuffer getUpgradeBuffer(String url);

    /**
     * 设置升级缓存
     *
     * @param buffer
     */
    void setUpgradeBuffer(UpgradeBuffer buffer);

}
