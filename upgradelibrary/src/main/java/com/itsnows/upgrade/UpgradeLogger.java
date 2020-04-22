package com.itsnows.upgrade;

import android.util.Log;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2020-4-20 下午10:31
 * <p>
 * UpgradeLogger
 */
public class UpgradeLogger {
    private static final String TAG = UpgradeLogger.class.getSimpleName();
    private static final int VERBOSE = 5;
    private static final int DEBUG = 4;
    private static final int INFO = 3;
    private static final int WARN = 2;
    private static final int ERROR = 1;
    private static final int NONE = 0;
    private static int level = VERBOSE;

    /**
     * VERBOSE
     *
     * @param tag
     * @param message
     */
    public static void v(String tag, String message) {
        if (level >= VERBOSE) Log.v(tag != null ? tag : TAG, message);
    }

    /**
     * DEBUG
     *
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (level >= DEBUG) Log.d(tag != null ? tag : TAG, message);
    }

    /**
     * INFO
     *
     * @param tag
     * @param message
     */
    public static void i(String tag, String message) {
        if (level >= INFO) Log.i(tag != null ? tag : TAG, message);
    }

    /**
     * WARN
     *
     * @param tag
     * @param message
     */
    public static void w(String tag, String message) {
        if (level >= WARN) Log.w(tag != null ? tag : TAG, message);
    }

    /**
     * ERROR
     *
     * @param tag
     * @param message
     */
    public static void e(String tag, String message) {
        if (level >= ERROR) Log.e(tag != null ? tag : TAG, message);
    }

}
