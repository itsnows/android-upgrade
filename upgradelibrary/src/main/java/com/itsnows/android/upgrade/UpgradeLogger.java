package com.itsnows.android.upgrade;

import android.util.Log;

/**
 * UpgradeLogger
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 2020-4-20 下午10:31
 */
public class UpgradeLogger {
    private static final String TAG = UpgradeLogger.class.getSimpleName();
    public static int level = Log.VERBOSE;

    private UpgradeLogger() {
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void v(String tag, String msg, Object... args) {
        if (level <= Log.VERBOSE) {
            Log.v(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args));
        }
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param tr   An exception to log.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void v(String tag, String msg, Throwable tr, Object... args) {
        if (level <= Log.VERBOSE) {
            Log.v(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args), tr);
        }
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void d(String tag, String msg, Object... args) {
        if (level <= Log.DEBUG) {
            Log.d(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args));
        }
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param tr   An exception to log.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void d(String tag, String msg, Throwable tr, Object... args) {
        if (level <= Log.DEBUG) {
            Log.d(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args), tr);
        }
    }

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void i(String tag, String msg, Object... args) {
        if (level <= Log.INFO) {
            Log.i(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args));
        }
    }

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param tr   An exception to log.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void i(String tag, String msg, Throwable tr, Object... args) {
        if (level <= Log.INFO) {
            Log.i(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args), tr);
        }
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void w(String tag, String msg, Object... args) {
        if (level <= Log.WARN) {
            Log.w(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args));
        }
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param tr   An exception to log.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void w(String tag, String msg, Throwable tr, Object... args) {
        if (level <= Log.WARN) {
            Log.w(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args), tr);
        }
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void e(String tag, String msg, Object... args) {
        if (level <= Log.ERROR) {
            Log.e(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args));
        }

    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param tag  Used to identify the source of a log message.  It usually identifies
     *             the class or activity where the log call occurs.
     * @param msg  The message you would like logged.
     * @param tr   An exception to log.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     */
    public static void e(String tag, String msg, Throwable tr, Object... args) {
        if (level <= Log.ERROR) {
            Log.e(tag == null ? TAG : tag, msg == null ? "" : args.length == 0 ? msg : String.format(msg, args), tr);
        }
    }

}
