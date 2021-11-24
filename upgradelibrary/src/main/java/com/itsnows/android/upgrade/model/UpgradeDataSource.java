package com.itsnows.android.upgrade.model;

import com.itsnows.android.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.android.upgrade.model.bean.UpgradeVersion;

import org.json.JSONException;

/**
 * UpgradeDataSource
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 2018/2/6 20:41
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
