package com.itsnows.upgrade.model;

import com.itsnows.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.upgrade.model.bean.UpgradeVersion;

import org.json.JSONException;

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
     * 保存升忽略版本
     *
     * @param version
     */
    void putUpgradeVersion(UpgradeVersion version);

    /**
     * 获取升级缓存
     *
     * @param url
     * @return
     */
    UpgradeBuffer getUpgradeBuffer(String url);

    /**
     * 保存升级缓存
     *
     * @param buffer
     */
    void putUpgradeBuffer(UpgradeBuffer buffer) throws JSONException;

}
