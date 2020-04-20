package com.itsnows.upgrade;

import java.io.IOException;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-5-16 下午5:19
 * <p>
 * UpgradeException
 */
public class UpgradeException extends IOException {

    /**
     * 安装包md5效验失败
     */
    public static final int ERROR_CODE_PACKAGE_INVALID = 10020;

    /**
     * 后台安装失败
     */
    public static final int ERROR_CODE_BACKGROUND_INSTALL_FAIL = 10050;

    /**
     * 未知错误
     */
    public static final int ERROR_CODE_UNKNOWN = 10045;

    private int code;

    public UpgradeException() {
        this(ERROR_CODE_UNKNOWN);
    }

    public UpgradeException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
