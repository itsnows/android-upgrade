package com.upgradelibrary.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.upgradelibrary.Historical;
import com.upgradelibrary.R;
import com.upgradelibrary.Util;
import com.upgradelibrary.bean.UpgradeBuffer;
import com.upgradelibrary.bean.UpgradeOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/12 9:33
 * <p>
 * 应用更新服务
 */

@SuppressWarnings("deprecation")
public class UpgradeService extends Service {
    public static final String TAG = UpgradeService.class.getSimpleName();
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 2000;
    private static final int SATUS_START = 0x1001;
    private static final int SATUS_PROGRESS = 0x1002;
    private static final int SATUS_PAUSE = 0x1003;
    private static final int SATUS_CANCEL = 0x1004;
    private static final int SATUS_ERROR = 0x1005;
    private static final int SATUS_COMPLETE = 0x1006;

    /**
     * 通知栏ID
     */
    private static final int NOTIFY_ID = 0x1024;

    /**
     * 升级进度通知栏
     */
    private Notification.Builder builder;
    private NotificationManager notificationManager;

    /**
     * 升级选项
     */
    private UpgradeOptions upgradeOption;

    /**
     * 任务线程
     */
    private TaskThread taskThread;

    /**
     * 下载处理
     */
    private DownloadHandler downloadHandler;

    /**
     * 下载回调接口
     */
    private OnDownloadListener onDownloadListener;

    /**
     * 网络状态变化广播
     */
    private NetWorkStateReceiver netWorkStateReceiver;

    /**
     * 状态
     */
    private int status;

    /**
     * 双击取消标记
     */
    private boolean isCancel;

    /**
     * 下载最大进度
     */
    private long maxProgress;

    /**
     * 下载进度
     */
    private long progress;

    /**
     * 下载进度百分比
     */
    private int percent;

    /**
     * 开始
     *
     * @param context
     * @param upgradeOption 升级选项
     */
    public static void start(Context context, UpgradeOptions upgradeOption) {
        start(context, upgradeOption, null);
    }

    /**
     * 开始
     *
     * @param context
     * @param upgradeOption     升级选项
     * @param serviceConnection 升级服务连接
     */
    public static void start(Context context, UpgradeOptions upgradeOption, ServiceConnection serviceConnection) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        if (upgradeOption == null) {
            throw new IllegalArgumentException("UpgradeOption can not be null");
        }
        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra("UpgradeOption", upgradeOption);
        context.startService(intent);
        if (serviceConnection != null) {
            context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadHandler = new DownloadHandler(this);
        netWorkStateReceiver = new NetWorkStateReceiver();
        netWorkStateReceiver.registerReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (status == SATUS_START || status == SATUS_PROGRESS) {
            pause();
            return super.onStartCommand(intent, flags, startId);
        }

        if (status == SATUS_PAUSE) {
            if (!isCancel) {
                isCancel = true;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isCancel = false;
                        resume();
                    }
                }, 2000L);
            } else {
                cancel();
            }
            return super.onStartCommand(intent, flags, startId);
        }

        if (status == SATUS_ERROR) {
            resume();
            return super.onStartCommand(intent, flags, startId);
        }

        if (status == SATUS_COMPLETE) {
            clearNotify(NOTIFY_ID);
            complete();
            return super.onStartCommand(intent, flags, startId);
        }

        UpgradeOptions upgradeOptions = intent.getParcelableExtra("UpgradeOption");
        if (upgradeOptions != null) {
            this.upgradeOption = new UpgradeOptions.Builder()
                    .setIcon(upgradeOptions.getIcon() == null ? Util.getAppIcon(this) : upgradeOptions.getIcon())
                    .setTitle(upgradeOptions.getTitle() == null ? Util.getAppName(this) : upgradeOptions.getTitle())
                    .setDescription(upgradeOptions.getDescription())
                    .setStorage(upgradeOptions.getStorage() == null ? new File(Environment.getExternalStorageDirectory(), getPackageName() + ".apk") : upgradeOptions.getStorage())
                    .setUrl(upgradeOptions.getUrl())
                    .setMd5(upgradeOptions.getMd5())
                    .setMutiThreadEnabled(upgradeOptions.isMultithreadEnabled())
                    .setMaxThreadPools(upgradeOptions.isMultithreadEnabled() ? upgradeOptions.getMaxThreadPools() == 0 ? 100 : upgradeOptions.getMaxThreadPools() : 0)
                    .build();
            initNotify();
            start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadHandler.removeCallbacksAndMessages(null);
        netWorkStateReceiver.unregisterReceiver(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new UpgradeServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * 初始化升级通知栏
     */
    private void initNotify() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(NOTIFY_ID), upgradeOption.getTitle(), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.setVibrationPattern(null);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, String.valueOf(NOTIFY_ID));
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
            builder.setLargeIcon(upgradeOption.getIcon());
            builder.setContentIntent(getDefalutIntent(PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setContentTitle(upgradeOption.getTitle());
            builder.setWhen(System.currentTimeMillis());
            builder.setPriority(Notification.PRIORITY_DEFAULT);
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.setDefaults(Notification.FLAG_AUTO_CANCEL);
        } else {
            builder = new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(upgradeOption.getIcon())
                    .setContentIntent(getDefalutIntent(PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle(upgradeOption.getTitle())
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setDefaults(Notification.FLAG_AUTO_CANCEL);
        }
    }

    /**
     * 设置通知栏
     */
    private void setNotify(String description) {
        if (status == SATUS_START) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else if (status == SATUS_PROGRESS) {
            builder.setProgress(100, percent, false);
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else {
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        }
        builder.setContentText(description);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    /**
     * 清除通知栏
     *
     * @param notifyId
     */
    public void clearNotify(int notifyId) {
        notificationManager.cancel(notifyId);
    }

    /**
     * 清除全部通知栏
     */
    public void clearAllNotify() {
        notificationManager.cancelAll();
    }

    /**
     * 通知栏意图
     *
     * @param flags
     * @return
     */
    public PendingIntent getDefalutIntent(int flags) {
        Intent intent = new Intent(this, UpgradeService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, flags);
        return pendingIntent;
    }

    /**
     * 安装
     */
    private void install() {
        Util.installApk(this, upgradeOption.getStorage().getPath());
    }

    /**
     * 开始
     */
    private void start() {
        if (taskThread != null) {
            if (taskThread.isAlive() || taskThread.isInterrupted()) {
                status = SATUS_CANCEL;
            }
            taskThread = null;
        }
        status = SATUS_START;
        taskThread = new TaskThread();
        taskThread.start();
    }

    /**
     * 暂停
     */
    public void pause() {
        status = SATUS_PAUSE;
    }

    /**
     * 继续
     */
    public void resume() {
        status = SATUS_START;
        start();
    }

    /**
     * 取消
     */
    public void cancel() {
        status = SATUS_CANCEL;
    }

    /**
     * 完成
     */
    public void complete() {
        status = SATUS_COMPLETE;
        stopSelf();
        install();
    }

    /**
     * 注入下载回调接口
     *
     * @param onDownloadListener
     */
    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    /**
     * 下载处理
     */
    private static class DownloadHandler extends Handler {
        private final SoftReference<UpgradeService> softReference;

        private DownloadHandler(UpgradeService upgradeService) {
            this.softReference = new SoftReference<>(upgradeService);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeService upgradeService = softReference.get();
            if (upgradeService == null) {
                return;
            }
            switch (msg.what) {
                case SATUS_START:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_start));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onStart();
                    }
                    break;
                case SATUS_PROGRESS:
                    upgradeService.setNotify(Util.formatByte(upgradeService.progress) + "/" + Util.formatByte(upgradeService.maxProgress));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onProgress(upgradeService.progress, upgradeService.maxProgress);
                    }
                    break;
                case SATUS_PAUSE:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_pause));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onPause();
                    }
                    break;
                case SATUS_CANCEL:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_cancel));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onCancel();
                    }
                    break;
                case SATUS_ERROR:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_error));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onError();
                    }
                    break;
                case SATUS_COMPLETE:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_complete));
                    upgradeService.install();
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onComplete();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

    /**
     * 下载监听回调接口
     */
    public static abstract class OnDownloadListener {

        public void onStart() {

        }

        public abstract void onProgress(long progress, long maxProgress);

        public void onPause() {

        }

        public void onCancel() {

        }

        public abstract void onError();

        public abstract void onComplete();

    }

    /**
     * 任务线程
     */
    private class TaskThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                downloadHandler.sendEmptyMessage(SATUS_START);
                long startLength = 0;
                long endLength = -1;

                File file = upgradeOption.getStorage();
                boolean exists = file.exists();
                if (!exists) {
                    File folder = new File(file.getPath().substring(0, file.getPath().lastIndexOf(File.separator)));
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                }

                UpgradeBuffer upgradeBuffer = Historical.getUpgradeBuffer(UpgradeService.this, upgradeOption.getUrl());
                if (upgradeBuffer != null && exists) {
                    progress = upgradeBuffer.getBufferLength();
                    maxProgress = upgradeBuffer.getFileLength();
                    List<UpgradeBuffer.ShuntPart> shuntParts = upgradeBuffer.getShuntParts();
                    for (int i = 0; i < shuntParts.size(); i++) {
                        startLength = shuntParts.get(i).getStartLength();
                        endLength = shuntParts.get(i).getEndLength();
                        if (shuntParts.size() == 1) {
                            if (Math.abs(System.currentTimeMillis() - upgradeBuffer.getLastModified()) > UpgradeBuffer.EXPIRY_DATE) {
                                file.delete();
                                startLength = 0;
                                progress = startLength;
                            }
                            if (startLength == file.length()) {
                                status = SATUS_COMPLETE;
                                downloadHandler.sendEmptyMessage(SATUS_COMPLETE);
                                break;
                            }
                            download(startLength, endLength);
                            return;
                        }
                        download(startLength, endLength);
                    }
                    return;
                }

                if ((endLength = readLength(upgradeOption.getUrl())) == -1) {
                    downloadHandler.sendEmptyMessage(SATUS_ERROR);
                    return;
                }

                progress = startLength;
                maxProgress = endLength;
                if (upgradeOption.isMultithreadEnabled()) {
                    int part = 5 * 1024 * 1024;
                    int count = (int) (endLength / part);
                    if (count > upgradeOption.getMaxThreadPools()) {
                        count = upgradeOption.getMaxThreadPools();
                        part = (int) (endLength / count);
                    }
                    long tempStartLength = 0;
                    long tempEndLength = 0;
                    for (int i = 1; i <= count; i++) {
                        tempStartLength = (i - 1) * part;
                        tempEndLength = tempStartLength + part - 1;
                        if (i == count) {
                            tempEndLength = endLength;
                        }
                        download(tempStartLength, tempEndLength);
                    }
                    return;
                }
                download(startLength, endLength);
            } catch (Exception e) {
                e.printStackTrace();
                downloadHandler.sendEmptyMessage(SATUS_ERROR);
            }
        }

        /**
         * 读取下载文件长度
         *
         * @param url 下载文件地址
         * @return
         */
        private long readLength(String url) {
            HttpURLConnection readConnection = null;
            try {
                readConnection = (HttpURLConnection) new URL(url).openConnection();
                readConnection.setRequestMethod("GET");
                readConnection.setDoInput(true);
                readConnection.setDoOutput(false);
                readConnection.setConnectTimeout(CONNECT_TIMEOUT);
                readConnection.setReadTimeout(READ_TIMEOUT);
                readConnection.connect();
                if (readConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readConnection.getContentLength();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (readConnection != null) {
                    readConnection.disconnect();
                }
            }
            return -1;
        }

        /**
         * 下载文件
         *
         * @param startLength 开始下载位置
         * @param entLength   结束下载位置
         */
        private void download(long startLength, long entLength) {
            new DownloadThread(startLength, entLength).start();
        }
    }

    /**
     * 下载线程
     */
    private class DownloadThread extends Thread {
        private long startLength;
        private long endLength;

        public DownloadThread(long startLength, long endLength) {
            this.startLength = startLength;
            this.endLength = endLength;
            init();
        }

        private void init() {
            setName("DownloadThread-" + getId());
            setPriority(NORM_PRIORITY);
            setDaemon(false);
            Log.d(TAG, "DownloadThread initialized");
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run() {
            super.run();
            HttpURLConnection downloadConnection = null;
            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {
                URL url = new URL(upgradeOption.getUrl());
                downloadConnection = (HttpURLConnection) url.openConnection();
                downloadConnection.setRequestMethod("GET");
                downloadConnection.setDoInput(true);
                downloadConnection.setDoOutput(false);
                File file = upgradeOption.getStorage();
                downloadConnection.setRequestProperty("Range", "bytes=" + startLength + "-" + endLength);
                downloadConnection.connect();

                if (downloadConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                    status = SATUS_ERROR;
                    downloadHandler.sendEmptyMessage(SATUS_ERROR);
                    return;
                }

                inputStream = downloadConnection.getInputStream();
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(startLength);
                byte[] bytes = new byte[1024];
                int temp = -1;
                do {
                    if (status == SATUS_CANCEL) {
                        downloadHandler.sendEmptyMessage(SATUS_CANCEL);
                        break;
                    }

                    if (status == SATUS_PAUSE) {
                        downloadHandler.sendEmptyMessage(SATUS_PAUSE);
                        break;
                    }

                    temp = inputStream.read(bytes);
                    if (temp == -1) {
                        if (progress < maxProgress) {
                            break;
                        }

                        if (TextUtils.isEmpty(upgradeOption.getMd5())) {
                            status = SATUS_COMPLETE;
                            downloadHandler.sendEmptyMessage(SATUS_COMPLETE);
                            break;
                        }

                        if (checkCompleteness(upgradeOption.getStorage().getPath(), upgradeOption.getMd5())) {
                            status = SATUS_COMPLETE;
                            downloadHandler.sendEmptyMessage(SATUS_COMPLETE);
                            break;
                        }

                        status = SATUS_ERROR;
                        downloadHandler.sendEmptyMessage(SATUS_ERROR);
                        break;
                    }

                    if (status == SATUS_START) {
                        status = SATUS_PROGRESS;
                    }
                    randomAccessFile.write(bytes, 0, temp);
                    startLength += temp;
                    progress += temp;
                    int tempPercent = (int) (((float) progress / maxProgress) * 100);
                    if (tempPercent > percent) {
                        percent = tempPercent;
                        downloadHandler.sendEmptyMessage(SATUS_PROGRESS);
                        recordDownload(upgradeOption.getUrl(), upgradeOption.getMd5());
                    }
                    Log.d(TAG, "Thread：" + getName() + " Position：" + startLength + "-" + endLength + " Download：" + percent + "% " + progress + "Byte/" + maxProgress + "Byte");
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
                status = SATUS_ERROR;
                downloadHandler.sendEmptyMessage(SATUS_ERROR);
            } finally {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (downloadConnection != null) {
                    downloadConnection.disconnect();
                }

            }
        }

        /**
         * 检测文件完整性
         *
         * @param path 文件路径
         * @param md5  文件Md5
         * @return
         */
        private boolean checkCompleteness(String path, String md5) throws IOException {
            MessageDigest messageDigest = null;
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(new File(path));
                messageDigest = MessageDigest.getInstance("MD5");
                byte[] buffer = new byte[1024];
                int temp = -1;
                while ((temp = fileInputStream.read(buffer, 0, 1024)) != -1) {
                    messageDigest.update(buffer, 0, temp);
                }
                BigInteger bigInteger = new BigInteger(1, messageDigest.digest());
                return TextUtils.equals(bigInteger.toString(), md5);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
            return false;
        }

        /**
         * 记录下载位置
         *
         * @param url 下载链接
         * @param md5 下载文件效验
         */
        private void recordDownload(String url, String md5) {
            UpgradeBuffer upgradeBuffer = Historical.getUpgradeBuffer(UpgradeService.this, url);
            if (upgradeBuffer == null) {
                upgradeBuffer = new UpgradeBuffer();
                upgradeBuffer.setDownloadUrl(url);
                upgradeBuffer.setFileMd5(md5);
                upgradeBuffer.setBufferLength(progress);
                upgradeBuffer.setFileLength(maxProgress);
                upgradeBuffer.setLastModified(System.currentTimeMillis());
                List<UpgradeBuffer.ShuntPart> shuntParts = new ArrayList<>(0);
                shuntParts.add(new UpgradeBuffer.ShuntPart(startLength, endLength));
                upgradeBuffer.setShuntParts(shuntParts);
                Historical.setUpgradeBuffer(UpgradeService.this, upgradeBuffer);
                return;
            }
            upgradeBuffer.setBufferLength(progress);
            List<UpgradeBuffer.ShuntPart> oldShuntParts = upgradeBuffer.getShuntParts();
            for (UpgradeBuffer.ShuntPart shuntPart : oldShuntParts) {
                if (shuntPart.getEndLength() == endLength) {
                    shuntPart.setStartLength(startLength);
                    Historical.setUpgradeBuffer(UpgradeService.this, upgradeBuffer);
                    return;
                }
            }
            oldShuntParts.add(new UpgradeBuffer.ShuntPart(startLength, endLength));
            Historical.setUpgradeBuffer(UpgradeService.this, upgradeBuffer);
        }
    }

    /**
     * 网络状态变化广播
     */
    private class NetWorkStateReceiver extends BroadcastReceiver {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo dataNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                // WIFI已连接，移动数据已连接
                if (status == SATUS_PAUSE) {
                    start();
                }
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                // WIFI已连接，移动数据已断开
                if (status == SATUS_PAUSE) {
                    start();
                }
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                // WIFI已断开，移动数据已连接
                if (status == SATUS_PAUSE) {
                    start();
                }
            } else {
                // WIFI已断开，移动数据已断开
                pause();
            }
        }

        public void registerReceiver(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(this, intentFilter);
        }

        public void unregisterReceiver(Context context) {
            context.unregisterReceiver(this);
        }
    }

    /**
     * 升级服务Binder
     */
    public class UpgradeServiceBinder extends Binder {

        public UpgradeService getUpgradeService() {
            return UpgradeService.this;
        }
    }


}
