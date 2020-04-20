package com.itsnows.upgrade;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-5-16 下午11:09
 * <p>
 * UpgradeConstant
 */
public class UpgradeConstant {

    /**
     * 连接服务
     */
    public static final int MSG_KEY_CONNECT_REQ = 0x2001;
    public static final int MSG_KEY_CONNECT_RESP = 0x2002;

    /**
     * 断开连接服务
     */
    public static final int MSG_KEY_DISCONNECT_REQ = 0x2011;
    public static final int MSG_KEY_DISCONNECT_RESP = 0x2012;

    /**
     * 下载开始
     */
    public static final int MSG_KEY_DOWNLOAD_START_REQ = 0x2021;
    public static final int MSG_KEY_DOWNLOAD_START_RESP = 0x2022;

    /**
     * 下载进度
     */
    public static final int MSG_KEY_DOWNLOAD_PROGRESS_REQ = 0x2031;
    public static final int MSG_KEY_DOWNLOAD_PROGRESS_RESP = 0x2032;

    /**
     * 下载暂停
     */
    public static final int MSG_KEY_DOWNLOAD_PAUSE_REQ = 0x2041;
    public static final int MSG_KEY_DOWNLOAD_PAUSE_RESP = 0x2042;

    /**
     * 下载暂停
     */
    public static final int MSG_KEY_DOWNLOAD_RESUME_REQ = 0x2051;
    public static final int MSG_KEY_DOWNLOAD_RESUME_RESP = 0x2052;

    /**
     * 下载取消
     */
    public static final int MSG_KEY_DOWNLOAD_CANCEL_REQ = 0x2061;
    public static final int MSG_KEY_DOWNLOAD_CANCEL_RESP = 0x2062;

    /**
     * 下载错误
     */
    public static final int MSG_KEY_DOWNLOAD_ERROR_REQ = 0x2071;
    public static final int MSG_KEY_DOWNLOAD_ERROR_RESP = 0x2072;

    /**
     * 下载完成
     */
    public static final int MSG_KEY_DOWNLOAD_COMPLETE_REQ = 0x2081;
    public static final int MSG_KEY_DOWNLOAD_COMPLETE_RESP = 0x2082;

    /**
     * 安装效验
     */
    public static final int MSG_KEY_INSTALL_VALIDATE_REQ = 0x2091;
    public static final int MSG_KEY_INSTALL_VALIDATE_RESP = 0x2092;

    /**
     * 安装开始
     */
    public static final int MSG_KEY_INSTALL_START_REQ = 0x2101;
    public static final int MSG_KEY_INSTALL_START_RESP = 0x2102;

    /**
     * 安装取消
     */
    public static final int MSG_KEY_INSTALL_CANCEL_REQ = 0x2111;
    public static final int MSG_KEY_INSTALL_CANCEL_RESP = 0x2112;

    /**
     * 安装错误
     */
    public static final int MSG_KEY_INSTALL_ERROR_REQ = 0x2121;
    public static final int MSG_KEY_INSTALL_ERROR_RESP = 0x2122;

    /**
     * 安装完成
     */
    public static final int MSG_KEY_INSTALL_COMPLETE_REQ = 0x2131;
    public static final int MSG_KEY_INSTALL_COMPLETE_RESP = 0x2132;

    /**
     * 安装完成
     */
    public static final int MSG_KEY_INSTALL_REBOOT_REQ = 0x2141;
    public static final int MSG_KEY_INSTALL_REBOOT_RESP = 0x2142;

}
