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

import com.upgradelibrary.R;
import com.upgradelibrary.Util;
import com.upgradelibrary.data.UpgradeRepository;
import com.upgradelibrary.data.bean.UpgradeBuffer;
import com.upgradelibrary.data.bean.UpgradeOptions;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: SXF
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/12 9:33
 * <p>
 * 应用更新服务
 */

@SuppressWarnings("deprecation")
public class UpgradeService extends Service {
    private static final String TAG = UpgradeService.class.getSimpleName();

    /**
     * 连接超时时长
     */
    public static final int CONNECT_TIMEOUT = 60 * 1000;

    /**
     * 读取超时时长
     */
    public static final int READ_TIMEOUT = 60 * 1000;

    /**
     * 下载开始
     */
    public static final int STATUS_START = 0x1001;

    /**
     * 下载进度
     */
    public static final int STATUS_PROGRESS = 0x1002;

    /**
     * 下载暂停
     */
    public static final int STATUS_PAUSE = 0x1003;

    /**
     * 下载取消
     */
    public static final int STATUS_CANCEL = 0x1004;

    /**
     * 下载错误
     */
    public static final int STATUS_ERROR = 0x1005;

    /**
     * 下载完成
     */
    public static final int STATUS_COMPLETE = 0x1006;

    /**
     * 通知栏ID
     */
    private static final int NOTIFY_ID = 0x6710;

    /**
     * 升级进度通知栏
     */
    private Notification.Builder builder;

    /**
     * 升级进度通知栏管理
     */
    private NotificationManager notificationManager;

    /**
     * 升级选项
     */
    private UpgradeOptions upgradeOption;

    /**
     * 下载缓存
     */
    private UpgradeBuffer upgradeBuffer;

    /**
     * 下载升级仓库
     */
    private UpgradeRepository repository;

    /**
     * 任务线程
     */
    private ScheduleThread scheduleThread;

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
    private volatile int status;

    /**
     * 双击取消标记
     */
    private volatile boolean isCancel;

    /**
     * 下载偏移量
     */
    private volatile int offset;

    /**
     * 下载最大进度
     */
    private volatile long maxProgress;

    /**
     * 下载进度
     */
    private volatile AtomicLong progress;

    /**
     * 启动
     *
     * @param context
     * @param upgradeOption 升级选项
     */
    public static void launch(Context context, UpgradeOptions upgradeOption) {
        launch(context, upgradeOption, null);
    }

    /**
     * 启动
     *
     * @param context           Context
     * @param upgradeOption     升级选项
     * @param serviceConnection 升级服务连接
     */
    public static void launch(Context context, UpgradeOptions upgradeOption, ServiceConnection serviceConnection) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        if (upgradeOption == null) {
            throw new IllegalArgumentException("UpgradeOption can not be null");
        }

        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra("upgrade_option", upgradeOption);
        if (!Util.isServiceRunning(context, UpgradeService.class.getName())) {
            context.startService(intent);
        }
        if (serviceConnection != null) {
            context.bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (downloadHandler == null) {
            downloadHandler = new DownloadHandler(this);
        }

        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
            netWorkStateReceiver.registerReceiver(this);
        }

        if (repository == null) {
            repository = new UpgradeRepository(this);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (status == STATUS_START || status == STATUS_PROGRESS) {
            pause();
            return super.onStartCommand(intent, flags, startId);
        }

        if (status == STATUS_PAUSE) {
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

        if (status == STATUS_ERROR) {
            resume();
            return super.onStartCommand(intent, flags, startId);
        }

        if (status == STATUS_COMPLETE) {
            complete();
            return super.onStartCommand(intent, flags, startId);
        }

        UpgradeOptions upgradeOptions = intent.getParcelableExtra("upgrade_option");
        if (upgradeOptions != null) {
            this.upgradeOption = new UpgradeOptions.Builder()
                    .setIcon(upgradeOptions.getIcon() == null ?
                            Util.getAppIcon(this) : upgradeOptions.getIcon())
                    .setTitle(upgradeOptions.getTitle() == null ?
                            Util.getAppName(this) : upgradeOptions.getTitle())
                    .setDescription(upgradeOptions.getDescription())
                    .setStorage(upgradeOptions.getStorage() == null ?
                            new File(Environment.getExternalStorageDirectory(), getPackageName() + ".apk") : upgradeOptions.getStorage())
                    .setUrl(upgradeOptions.getUrl())
                    .setMd5(upgradeOptions.getMd5())
                    .setMultithreadEnabled(upgradeOptions.isMultithreadEnabled())
                    .setMultithreadPools(upgradeOptions.isMultithreadEnabled() ?
                            upgradeOptions.getMultithreadPools() == 0 ? 100 : upgradeOptions.getMultithreadPools() : 0)
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
            builder = new Notification.Builder(this, String.valueOf(NOTIFY_ID))
                    .setGroup(String.valueOf(NOTIFY_ID))
                    .setGroupSummary(false)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(upgradeOption.getIcon())
                    .setContentIntent(getDefalutIntent(PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle(upgradeOption.getTitle())
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setDefaults(Notification.FLAG_AUTO_CANCEL);
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
        if (status == STATUS_START) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else if (status == STATUS_PROGRESS) {
            builder.setProgress(100, offset, false);
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else {
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        }
        builder.setContentText(description);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    /**
     * 清除通知栏
     */
    public void clearNotify() {
        notificationManager.cancel(NOTIFY_ID);
    }

    /**
     * 通知栏意图
     *
     * @param flags
     * @return
     */
    public PendingIntent getDefalutIntent(int flags) {
        Intent intent = new Intent(this, UpgradeService.class);
        return PendingIntent.getService(this, 0, intent, flags);
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
        if (scheduleThread != null) {
            if (scheduleThread.isAlive() || !scheduleThread.isInterrupted()) {
                status = STATUS_CANCEL;
            }
            scheduleThread = null;
        }
        status = STATUS_START;
        scheduleThread = new ScheduleThread();
        scheduleThread.start();
    }

    /**
     * 暂停
     */
    public void pause() {
        status = STATUS_PAUSE;
    }

    /**
     * 继续
     */
    public void resume() {
        status = STATUS_START;
        start();
    }

    /**
     * 取消
     */
    public void cancel() {
        status = STATUS_CANCEL;
    }

    /**
     * 完成
     */
    public void complete() {
        status = STATUS_COMPLETE;
        clearNotify();
        install();
        stopSelf();
    }

    /**
     * 注入下载回调接口
     *
     * @param onDownloadListener OnDownloadListener
     */
    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
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
     * 下载处理
     */
    private static class DownloadHandler extends Handler {
        private SoftReference<UpgradeService> reference;

        public DownloadHandler(UpgradeService upgradeService) {
            reference = new SoftReference<>(upgradeService);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeService upgradeService = reference.get();
            if (upgradeService == null) {
                return;
            }
            switch (msg.what) {
                case STATUS_START:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_start));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onStart();
                    }
                    break;
                case STATUS_PROGRESS:
                    upgradeService.setNotify(Util.formatByte(upgradeService.progress.get()) + "/" + Util.formatByte(upgradeService.maxProgress));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onProgress(upgradeService.progress.get(), upgradeService.maxProgress);
                    }
                    break;
                case STATUS_PAUSE:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_pause));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onPause();
                    }
                    break;
                case STATUS_CANCEL:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_cancel));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onCancel();
                    }
                    break;
                case STATUS_ERROR:
                    upgradeService.setNotify(upgradeService.getString(R.string.download_error));
                    if (upgradeService.onDownloadListener != null) {
                        upgradeService.onDownloadListener.onError();
                    }
                    break;
                case STATUS_COMPLETE:
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
     * 任务线程
     */
    private class ScheduleThread extends Thread {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(500);
                downloadHandler.sendEmptyMessage(STATUS_START);
                long startLength = 0;
                long endLength = -1;
                File targetFile = upgradeOption.getStorage();
                if (targetFile.exists()) {
                    UpgradeBuffer upgradeBuffer = repository.getUpgradeBuffer(upgradeOption.getUrl());
                    if (upgradeBuffer != null && upgradeBuffer.getBufferLength() <= targetFile.length()) {
                        progress = new AtomicLong(upgradeBuffer.getBufferLength());
                        maxProgress = upgradeBuffer.getFileLength();
                        long expiryDate = Math.abs(System.currentTimeMillis() - upgradeBuffer.getLastModified());
                        if (expiryDate <= UpgradeBuffer.EXPIRY_DATE) {
                            if (upgradeBuffer.getBufferLength() == upgradeBuffer.getFileLength()) {
                                status = STATUS_PROGRESS;
                                downloadHandler.sendEmptyMessage(STATUS_PROGRESS);

                                status = STATUS_COMPLETE;
                                downloadHandler.sendEmptyMessage(STATUS_COMPLETE);
                                return;
                            }
                            List<UpgradeBuffer.BufferPart> bufferParts = upgradeBuffer.getBufferParts();
                            for (int id = 0; id < bufferParts.size(); id++) {
                                startLength = bufferParts.get(id).getStartLength();
                                endLength = bufferParts.get(id).getEndLength();
                                submit(id, startLength, endLength);
                            }
                            return;
                        }
                    }
                    targetFile.delete();
                }

                File parentFile = targetFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if ((endLength = length(upgradeOption.getUrl())) == -1) {
                    downloadHandler.sendEmptyMessage(STATUS_ERROR);
                    return;
                }
                progress = new AtomicLong(startLength);
                maxProgress = endLength;
                if (!upgradeOption.isMultithreadEnabled()) {
                    submit(0, startLength, endLength);
                    return;
                }
                int part = 5 * 1024 * 1024;
                int pools = (int) (endLength / part);
                if (pools > upgradeOption.getMultithreadPools()) {
                    pools = upgradeOption.getMultithreadPools();
                    part = (int) (endLength / pools);
                }
                long tempStartLength = 0;
                long tempEndLength = 0;
                for (int id = 1; id <= pools; id++) {
                    tempStartLength = (id - 1) * part;
                    tempEndLength = tempStartLength + part - 1;
                    if (id == pools) {
                        tempEndLength = endLength;
                    }
                    submit(id - 1, tempStartLength, tempEndLength);
                }
            } catch (Exception e) {
                e.printStackTrace();
                downloadHandler.sendEmptyMessage(STATUS_ERROR);
            }
        }

        /**
         * 下载文件长度
         *
         * @param url 下载文件地址
         * @return
         */
        private long length(String url) throws IOException {
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
            } finally {
                if (readConnection != null) {
                    readConnection.disconnect();
                }
            }
            return -1;
        }

        /**
         * 提交下载任务
         *
         * @param id          线程ID
         * @param startLength 开始下载位置
         * @param entLength   结束下载位置
         */
        private void submit(int id, long startLength, long entLength) {
            Thread thread = new DownloadThread(id, startLength, entLength);
            thread.start();
        }
    }

    /**
     * 下载线程
     */
    private class DownloadThread extends Thread {
        private int id;
        private long startLength;
        private long endLength;

        public DownloadThread(int id, long startLength, long endLength) {
            this.id = id;
            this.startLength = startLength;
            this.endLength = endLength;
            setName("DownloadThread-" + id);
            setPriority(Thread.NORM_PRIORITY);
            setDaemon(false);
            Log.d(TAG, "DownloadThread initialized");
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run() {
            super.run();
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {
                URL url = new URL(upgradeOption.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                File file = upgradeOption.getStorage();
                connection.setRequestProperty("Range", "bytes=" + startLength + "-" + endLength);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                    status = STATUS_ERROR;
                    downloadHandler.sendEmptyMessage(STATUS_ERROR);
                    return;
                }

                inputStream = connection.getInputStream();
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(startLength);
                byte[] buffer = new byte[1024];
                int len = -1;
                int tempOffset = 0;
                do {
                    if (status == STATUS_CANCEL) {
                        downloadHandler.sendEmptyMessage(STATUS_CANCEL);
                        break;
                    }

                    if (status == STATUS_PAUSE) {
                        downloadHandler.sendEmptyMessage(STATUS_PAUSE);
                        break;
                    }

                    if ((len = inputStream.read(buffer)) == -1) {

                        if (progress.get() < maxProgress) {
                            break;
                        }

                        if (status == STATUS_COMPLETE) {
                            break;
                        }

                        if (!check()) {
                            status = STATUS_ERROR;
                            downloadHandler.sendEmptyMessage(STATUS_ERROR);
                            break;
                        }

                        status = STATUS_COMPLETE;
                        downloadHandler.sendEmptyMessage(STATUS_COMPLETE);
                        break;
                    }

                    if (status == STATUS_START) {
                        status = STATUS_PROGRESS;
                    }

                    randomAccessFile.write(buffer, 0, len);
                    startLength += len;
                    progress.addAndGet(len);
                    tempOffset = (int) (((float) progress.get() / maxProgress) * 100);
                    if (tempOffset > offset) {
                        offset = tempOffset;
                        downloadHandler.sendEmptyMessage(STATUS_PROGRESS);
                        mark();
                        Log.d(TAG, "Thread：" + getName() + " Position：" + startLength + "-" + endLength + " Download：" + offset + "% " + progress + "Byte/" + maxProgress + "Byte");
                    }
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
                status = STATUS_ERROR;
                downloadHandler.sendEmptyMessage(STATUS_ERROR);
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

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        /**
         * 标记下载位置
         */
        private void mark() {
            if (upgradeBuffer == null) {
                upgradeBuffer = new UpgradeBuffer();
                upgradeBuffer.setDownloadUrl(upgradeOption.getUrl());
                upgradeBuffer.setFileMd5(upgradeOption.getMd5());
                upgradeBuffer.setBufferLength(progress.get());
                upgradeBuffer.setFileLength(maxProgress);
                upgradeBuffer.setBufferParts(new CopyOnWriteArrayList<UpgradeBuffer.BufferPart>());
                upgradeBuffer.setLastModified(System.currentTimeMillis());
            }
            upgradeBuffer.setBufferLength(progress.get());
            upgradeBuffer.setLastModified(System.currentTimeMillis());
            int index = -1;
            for (int i = 0; i < upgradeBuffer.getBufferParts().size(); i++) {
                if (upgradeBuffer.getBufferParts().get(i).getEndLength() == endLength) {
                    index = i;
                    break;
                }
            }
            UpgradeBuffer.BufferPart bufferPart = new UpgradeBuffer.BufferPart(startLength, endLength);
            if (index == -1) {
                upgradeBuffer.getBufferParts().add(bufferPart);
            } else {
                upgradeBuffer.getBufferParts().set(index, bufferPart);
            }
            repository.putUpgradeBuffer(upgradeBuffer);
        }

        /**
         * 检测文件完整性
         *
         * @return
         */
        private boolean check() throws IOException {
            if (!TextUtils.isEmpty(upgradeOption.getMd5())) {
                MessageDigest messageDigest = null;
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(upgradeOption.getStorage());
                    messageDigest = MessageDigest.getInstance("MD5");
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        messageDigest.update(buffer, 0, len);
                    }
                    BigInteger bigInteger = new BigInteger(1, messageDigest.digest());
                    return TextUtils.equals(bigInteger.toString(), upgradeOption.getMd5());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
            return true;
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

            // WIFI已连接，移动数据已连接
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                if (status == STATUS_PAUSE) {
                    start();
                }
                return;
            }

            // WIFI已连接，移动数据已断开
            if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                if (status == STATUS_PAUSE) {
                    start();
                }
                return;
            }

            // WIFI已断开，移动数据已连接
            if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                if (status == STATUS_PAUSE) {
                    start();
                }
                return;
            }

            // WIFI已断开，移动数据已断开
            pause();
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
