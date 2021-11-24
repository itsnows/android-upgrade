package com.itsnows.android.upgrade;

import java.io.IOException;

/**
 * UpgradeException
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 19-5-16 下午5:19
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

    private final int code;

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
