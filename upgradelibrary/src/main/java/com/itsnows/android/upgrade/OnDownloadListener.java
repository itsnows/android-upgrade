package com.itsnows.android.upgrade;

/**
 * OnDownloadListener
 *
 * @author itsnows, xue.com.fei@gmail.com
 * @since 19-5-17 上午8:53
 */
public abstract class OnDownloadListener {

    public void onStart() {

    }

    public abstract void onProgress(long max, long progress);

    public void onPause() {

    }

    public void onCancel() {

    }

    public abstract void onError(UpgradeException e);

    public abstract void onComplete();
}
