package com.itsnows.upgrade;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-5-17 上午8:53
 * <p>
 * OnDownloadListener
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
