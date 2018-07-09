package com.upgradelibrary.data;

import com.upgradelibrary.data.bean.UpgradeBuffer;
import com.upgradelibrary.data.bean.UpgradeVersion;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/7/6 20:41
 * <p>
 * DataSource
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
     * @param upgradeVersion
     */
    void putUpgradeVersion(UpgradeVersion upgradeVersion);

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
     * @param upgradeBuffer
     */
    void putUpgradeBuffer(UpgradeBuffer upgradeBuffer);

}
