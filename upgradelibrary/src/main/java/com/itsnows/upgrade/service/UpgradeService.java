package com.itsnows.upgrade.service;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.itsnows.upgrade.R;
import com.itsnows.upgrade.UpgradeConstant;
import com.itsnows.upgrade.UpgradeException;
import com.itsnows.upgrade.UpgradeUtil;
import com.itsnows.upgrade.model.UpgradeRepository;
import com.itsnows.upgrade.model.bean.UpgradeBuffer;
import com.itsnows.upgrade.model.bean.UpgradeOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/1/12 9:33
 * <p>
 * 应用更新服务
 */

@SuppressWarnings("deprecation")
public class UpgradeService extends Service {
    private static final String TAG = UpgradeService.class.getSimpleName();
    private static final String PARAMS_UPGRADE_OPTION = "upgrade_option";

    /**
     * 连接超时时长
     */
    private static final int CONNECT_TIMEOUT = 60 * 1000;

    /**
     * 读取超时时长
     */
    private static final int READ_TIMEOUT = 60 * 1000;

    /**
     * 下载开始
     */
    private static final int STATUS_DOWNLOAD_START = 0x1001;

    /**
     * 下载进度
     */
    private static final int STATUS_DOWNLOAD_PROGRESS = 0x1002;

    /**
     * 下载暂停
     */
    private static final int STATUS_DOWNLOAD_PAUSE = 0x1003;

    /**
     * 下载取消
     */
    private static final int STATUS_DOWNLOAD_CANCEL = 0x1004;

    /**
     * 下载错误
     */
    private static final int STATUS_DOWNLOAD_ERROR = 0x1005;

    /**
     * 下载完成
     */
    private static final int STATUS_DOWNLOAD_COMPLETE = 0x1006;

    /**
     * 安装效验
     */
    private static final int STATUS_INSTALL_CHECK = 0x2001;

    /**
     * 安装开始
     */
    private static final int STATUS_INSTALL_START = 0x2002;

    /**
     * 安装取消
     */
    private static final int STATUS_INSTALL_CANCEL = 0x2003;

    /**
     * 安装错误
     */
    private static final int STATUS_INSTALL_ERROR = 0x2004;

    /**
     * 安装完成
     */
    private static final int STATUS_INSTALL_COMPLETE = 0x2005;

    /**
     * 通知栏ID
     */
    private static final int NOTIFY_ID = 0x6710;

    /**
     * 延时
     */
    private static final int DELAY = 200;

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
     * 升级缓存
     */
    private UpgradeBuffer upgradeBuffer;

    /**
     * 升级仓库
     */
    private UpgradeRepository repository;

    /**
     * 调度线程
     */
    private ScheduleThread scheduleThread;

    /**
     * 消息处理
     */
    private MessageHandler messageHandler;

    /**
     * 网络状态变化广播
     */
    private NetWorkStateReceiver netWorkStateReceiver;

    /**
     * 网络状态变化广播
     */
    private PackagesReceiver packagesReceiver;

    /**
     * 服务端
     */
    private Messenger server;

    /**
     * 客服端
     */
    private List<Messenger> clients;

    /**
     * 双击取消标记
     */
    private boolean isCancel;

    /**
     * 状态
     */
    private volatile int status;

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
     * @param options 升级选项
     */
    public static void start(Context context, UpgradeOptions options) {
        start(context, options, null);
    }

    /**
     * 启动
     *
     * @param context    Context
     * @param options    升级选项
     * @param connection 升级服务连接
     */
    public static void start(Context context, UpgradeOptions options, ServiceConnection connection) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        if (options == null) {
            throw new IllegalArgumentException("UpgradeOption can not be null");
        }

        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra(PARAMS_UPGRADE_OPTION, options);
        if (!UpgradeUtil.isServiceRunning(context, UpgradeService.class.getName())) {
            context.startService(intent);
        }
        if (connection != null) {
            context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = super.onStartCommand(intent, flags, startId);
        if (status == STATUS_DOWNLOAD_START || status == STATUS_DOWNLOAD_PROGRESS) {
            pause();
            return command;
        }

        if (status == STATUS_DOWNLOAD_PAUSE) {
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
            return command;
        }

        if (status == STATUS_DOWNLOAD_ERROR) {
            resume();
            return command;
        }

        if (status == STATUS_DOWNLOAD_COMPLETE) {
            complete();
            return command;
        }

        UpgradeOptions upgradeOptions = intent.getParcelableExtra(PARAMS_UPGRADE_OPTION);
        if (upgradeOptions != null) {
            this.upgradeOption = new UpgradeOptions.Builder()
                    .setIcon(upgradeOptions.getIcon() == null ?
                            UpgradeUtil.getAppIcon(this) : upgradeOptions.getIcon())
                    .setTitle(upgradeOptions.getTitle() == null ?
                            UpgradeUtil.getAppName(this) : upgradeOptions.getTitle())
                    .setDescription(upgradeOptions.getDescription())
                    .setStorage(upgradeOptions.getStorage() == null ?
                            new File(Environment.getExternalStorageDirectory(),
                                    getPackageName() + ".apk") : upgradeOptions.getStorage())
                    .setUrl(upgradeOptions.getUrl())
                    .setMd5(upgradeOptions.getMd5())
                    .setMultithreadEnabled(upgradeOptions.isMultithreadEnabled())
                    .setMultithreadPools(upgradeOptions.isMultithreadEnabled() ?
                            upgradeOptions.getMultithreadPools() == 0 ? 100 :
                                    upgradeOptions.getMultithreadPools() : 0)
                    .setAutocleanEnabled(upgradeOptions.isAutocleanEnabled())
                    .build();
            initNotify();
            start();
        }
        return command;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messageHandler != null) {
            messageHandler.removeCallbacksAndMessages(null);
        }
        if (netWorkStateReceiver != null) {
            netWorkStateReceiver.unregisterReceiver(this);
        }
        if (packagesReceiver != null) {
            packagesReceiver.unregisterReceiver(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return server.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
        messageHandler.sendEmptyMessageDelayed(STATUS_DOWNLOAD_PROGRESS, DELAY);
        Message msg = Message.obtain();
        msg.what = status;
        msg.arg1 = -1;
        messageHandler.sendMessageDelayed(msg, DELAY);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /**
     * 初始化
     */
    private void init() {
        if (server == null) {
            server = new Messenger(ServeHandler.create(this));
        }

        if (clients == null) {
            clients = new CopyOnWriteArrayList<>();
        }

        if (messageHandler == null) {
            messageHandler = new MessageHandler(this);
        }

        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
            netWorkStateReceiver.registerReceiver(this);
        }

        if (packagesReceiver == null) {
            packagesReceiver = new PackagesReceiver();
            packagesReceiver.registerReceiver(this);
        }

        if (repository == null) {
            repository = UpgradeRepository.getInstance(this);
        }
    }

    /**
     * 初始化通知栏
     */
    private void initNotify() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(NOTIFY_ID),
                    upgradeOption.getTitle(), NotificationManager.IMPORTANCE_DEFAULT);
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
        if (status == STATUS_DOWNLOAD_START) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
        } else if (status == STATUS_DOWNLOAD_PROGRESS) {
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
    private void clearNotify() {
        notificationManager.cancel(NOTIFY_ID);
    }

    /**
     * 通知栏意图
     *
     * @param flags
     * @return
     */
    private PendingIntent getDefalutIntent(int flags) {
        Intent intent = new Intent(this, UpgradeService.class);
        return PendingIntent.getService(this, 0, intent, flags);
    }

    /**
     * 删除安装包
     *
     * @return
     */
    private boolean deletePackage() {
        File packageFile = upgradeOption.getStorage();
        if (packageFile.exists()) {
            return packageFile.delete();
        }
        return false;
    }

    /**
     * 安装
     */
    private void install() {
        Thread thread = new InstallThread();
        thread.start();
    }

    /**
     * 开始
     */
    private void start() {
        if (scheduleThread != null) {
            if (scheduleThread.isAlive() || !scheduleThread.isInterrupted()) {
                status = STATUS_DOWNLOAD_CANCEL;
            }
            scheduleThread = null;
        }
        status = STATUS_DOWNLOAD_START;
        scheduleThread = new ScheduleThread();
        scheduleThread.start();
    }

    /**
     * 暂停
     */
    private void pause() {
        status = STATUS_DOWNLOAD_PAUSE;
    }

    /**
     * 继续
     */
    private void resume() {
        status = STATUS_DOWNLOAD_START;
        start();
    }

    /**
     * 取消
     */
    private void cancel() {
        status = STATUS_DOWNLOAD_CANCEL;
    }

    /**
     * 下载完成
     */
    private void complete() {
        status = STATUS_DOWNLOAD_COMPLETE;
        clearNotify();
        install();
    }

    /**
     * 发送消息到客户端
     *
     * @param key
     * @param data
     */
    private void sendMessageToClient(int key, Bundle data) {
        Iterator<Messenger> iterator = clients.iterator();
        Messenger client = null;
        while (iterator.hasNext()) {
            client = iterator.next();
            if (client == null) {
                iterator.remove();
                continue;
            }
            sendMessageToClient(client, key, data);
        }
    }

    /**
     * 发送消息到客户端
     *
     * @param client
     * @param key
     * @param data
     */
    private void sendMessageToClient(Messenger client, int key, Bundle data) {
        try {
            Message message = Message.obtain();
            message.replyTo = server;
            message.what = key;
            message.setData(data == null ? new Bundle() : data);
            client.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务端消息
     */
    private static class ServeHandler extends Handler {
        private SoftReference<UpgradeService> reference;

        private static Handler create(UpgradeService service) {
            HandlerThread thread = new HandlerThread("Messenger");
            thread.start();
            return new ServeHandler(thread.getLooper(), service);
        }

        private ServeHandler(Looper looper, UpgradeService service) {
            super(looper);
            this.reference = new SoftReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeService service = reference.get();
            if (service == null) {
                return;
            }
            Messenger clint = msg.replyTo;
            Bundle response = new Bundle();
            Bundle request = msg.getData();
            switch (msg.what) {
                case UpgradeConstant.MSG_KEY_CONNECT_REQ:
                    if (!service.clients.contains(clint)) {
                        service.clients.add(clint);
                        response.putInt("code", 0);
                        response.putString("message",
                                service.getString(R.string.message_connect_success));
                    } else {
                        response.putInt("code", UpgradeException.ERROR_CODE_UNKNOWN);
                        response.putString("message",
                                service.getString(R.string.message_connect_failure));
                    }
                    service.sendMessageToClient(clint, UpgradeConstant.MSG_KEY_CONNECT_RESP, response);
                    break;
                case UpgradeConstant.MSG_KEY_DISCONNECT_REQ:
                    boolean success = service.clients.remove(clint);
                    if (success) {
                        response.putInt("code", 0);
                        response.putString("message",
                                service.getString(R.string.message_disconnect_success));
                    } else {
                        response.putInt("key", UpgradeException.ERROR_CODE_UNKNOWN);
                        response.putString("message",
                                service.getString(R.string.message_disconnect_failure));
                    }
                    service.sendMessageToClient(clint, UpgradeConstant.MSG_KEY_DISCONNECT_RESP, response);
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_PAUSE_REQ:
                    service.pause();
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_RESUME_REQ:
                    service.resume();
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_START_REQ:
                    service.install();
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * 消息处理
     */
    private static class MessageHandler extends Handler {
        private WeakReference<UpgradeService> reference;

        private MessageHandler(UpgradeService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeService service = reference.get();
            if (service == null) {
                return;
            }
            Bundle response = new Bundle();
            switch (msg.what) {
                case STATUS_DOWNLOAD_START:
                    service.setNotify(service.getString(R.string.message_download_start));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_START_RESP, response);
                    break;
                case STATUS_DOWNLOAD_PROGRESS:
                    service.setNotify(UpgradeUtil.formatByte(service.progress.get()) + "/" +
                            UpgradeUtil.formatByte(service.maxProgress));
                    response.putLong("max", service.maxProgress);
                    response.putLong("progress", service.progress.get());
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_PROGRESS_RESP, response);
                    break;
                case STATUS_DOWNLOAD_PAUSE:
                    service.setNotify(service.getString(R.string.message_download_pause));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_PAUSE_RESP, response);
                    break;
                case STATUS_DOWNLOAD_CANCEL:
                    service.setNotify(service.getString(R.string.message_download_cancel));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_CANCEL_RESP, response);
                    break;
                case STATUS_DOWNLOAD_ERROR:
                    service.setNotify(service.getString(R.string.message_download_error));
                    response.putInt("code", UpgradeException.ERROR_CODE_UNKNOWN);
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_ERROR_RESP, response);
                    break;
                case STATUS_DOWNLOAD_COMPLETE:
                    service.setNotify(service.getString(R.string.message_download_complete));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_DOWNLOAD_COMPLETE_RESP, response);
                    if (msg.arg1 != -1) {
                        service.install();
                    }
                    break;
                case STATUS_INSTALL_CHECK:
                    service.setNotify(service.getString(R.string.message_install_check));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_CHECK_RESP, response);
                    break;
                case STATUS_INSTALL_START:
                    service.setNotify(service.getString(R.string.message_install_start));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_START_RESP, response);
                    break;
                case STATUS_INSTALL_CANCEL:
                    service.setNotify(service.getString(R.string.message_install_cancel));
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_CANCEL_RESP, response);
                    break;
                case STATUS_INSTALL_ERROR:
                    if (msg.arg1 == UpgradeException.ERROR_CODE_PACKAGE_INVALID) {
                        service.setNotify(service.getString(R.string.message_install_package_invalid));
                        response.putInt("code", UpgradeException.ERROR_CODE_PACKAGE_INVALID);
                        service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_ERROR_RESP, response);
                        return;
                    }
                    if (msg.arg1 == UpgradeException.ERROR_CODE_PACKAGE_NO_ROOT) {
                        service.setNotify(service.getString(R.string.message_install_not_root));
                        response.putInt("code", UpgradeException.ERROR_CODE_PACKAGE_NO_ROOT);
                        service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_ERROR_RESP, response);
                        return;
                    }
                    service.setNotify(service.getString(R.string.message_install_error));
                    response.putInt("code", UpgradeException.ERROR_CODE_UNKNOWN);
                    service.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_ERROR_RESP, response);
                    break;
                case STATUS_INSTALL_COMPLETE:
                    service.setNotify(service.getString(R.string.message_install_complete));
                    // upgradeService.sendMessageToClient(UpgradeConstant.MSG_KEY_INSTALL_COMPLETE_RESP, response);
                    if (msg.arg1 == -1 && service.deletePackage()) {
                        Toast.makeText(service, service.getString(
                                R.string.message_install_package_delete), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 调度线程
     */
    private class ScheduleThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(DELAY);
                messageHandler.sendEmptyMessage(STATUS_DOWNLOAD_START);
                long startLength = 0;
                long endLength = -1;
                File targetFile = upgradeOption.getStorage();
                if (targetFile.exists()) {
                    UpgradeBuffer upgradeBuffer = repository.getUpgradeBuffer(upgradeOption.getUrl());
                    if (upgradeBuffer != null) {
                        if (upgradeBuffer.getBufferLength() <= targetFile.length()) {
                            if ((endLength = length(upgradeOption.getUrl())) != -1 &&
                                    endLength == upgradeBuffer.getFileLength()) {
                                progress = new AtomicLong(upgradeBuffer.getBufferLength());
                                maxProgress = upgradeBuffer.getFileLength();
                                long expiryDate = Math.abs(System.currentTimeMillis() - upgradeBuffer.getLastModified());
                                if (expiryDate <= UpgradeBuffer.EXPIRY_DATE) {
                                    if (upgradeBuffer.getBufferLength() == upgradeBuffer.getFileLength()) {
                                        status = STATUS_DOWNLOAD_PROGRESS;
                                        messageHandler.sendEmptyMessage(status);

                                        status = STATUS_DOWNLOAD_COMPLETE;
                                        messageHandler.sendEmptyMessage(status);
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
                        }
                    }
                    targetFile.delete();
                }

                boolean parentFileExists = true;
                File parentFile = targetFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFileExists = parentFile.mkdirs();
                }

                if (!parentFileExists) {
                    status = STATUS_DOWNLOAD_ERROR;
                    messageHandler.sendEmptyMessage(status);
                    return;
                }

                if ((endLength = length(upgradeOption.getUrl())) == -1) {
                    status = STATUS_DOWNLOAD_ERROR;
                    messageHandler.sendEmptyMessage(status);
                    return;
                }
                progress = new AtomicLong(startLength);
                maxProgress = endLength;
                if (!upgradeOption.isMultithreadEnabled()) {
                    submit(0, startLength, endLength);
                    return;
                }
                int part = 5 * 1024 * 1024;
                int pools = 1;
                if (endLength >= part) {
                    pools = (int) (endLength / part);
                }
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
                status = STATUS_DOWNLOAD_ERROR;
                messageHandler.sendEmptyMessage(status);
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
            Thread thread = null;
            if (!upgradeOption.isMultithreadEnabled()) {
                thread = new DownloadThread(id);
            } else {
                thread = new DownloadThread(id, startLength, entLength);
            }
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

        public DownloadThread(int id) {
            this(id, 0, 0);
        }

        private DownloadThread(int id, long startLength, long endLength) {
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

                if (endLength == 0) {
                    connection.connect();
                } else {
                    connection.setRequestProperty("Range", "bytes=" + startLength + "-" + endLength);
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                        status = STATUS_DOWNLOAD_ERROR;
                        messageHandler.sendEmptyMessage(status);
                        return;
                    }
                }

                inputStream = connection.getInputStream();
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(startLength);
                byte[] buffer = new byte[1024];
                int len = -1;
                int tempOffset = 0;
                do {
                    if (status == STATUS_DOWNLOAD_CANCEL) {
                        messageHandler.sendEmptyMessage(status);
                        break;
                    }

                    if (status == STATUS_DOWNLOAD_PAUSE) {
                        messageHandler.sendEmptyMessage(status);
                        break;
                    }

                    if ((len = inputStream.read(buffer)) == -1) {
                        if (progress.get() < maxProgress) {
                            break;
                        }

                        if (status == STATUS_DOWNLOAD_COMPLETE) {
                            break;
                        }

                        status = STATUS_DOWNLOAD_COMPLETE;
                        messageHandler.sendEmptyMessage(status);
                        break;
                    }

                    if (status == STATUS_DOWNLOAD_START) {
                        status = STATUS_DOWNLOAD_PROGRESS;
                    }

                    randomAccessFile.write(buffer, 0, len);
                    startLength += len;
                    progress.addAndGet(len);
                    tempOffset = (int) (((float) progress.get() / maxProgress) * 100);
                    if (tempOffset > offset) {
                        offset = tempOffset;
                        messageHandler.sendEmptyMessage(STATUS_DOWNLOAD_PROGRESS);
                        mark();
                        Log.d(TAG, "Thread：" + getName()
                                + " Position：" + startLength + "-" + endLength
                                + " Download：" + offset + "% " + progress + "Byte/" + maxProgress + "Byte");
                    }
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
                status = STATUS_DOWNLOAD_ERROR;
                messageHandler.sendEmptyMessage(status);
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
            repository.setUpgradeBuffer(upgradeBuffer);
        }
    }

    /**
     * 安装线程
     */
    private class InstallThread extends Thread {
        private Timer timer;

        @Override
        public void run() {
            super.run();

            try {
                if (upgradeOption.getMd5() != null) {
                    status = STATUS_INSTALL_CHECK;
                    messageHandler.sendEmptyMessage(STATUS_INSTALL_CHECK);
                    if (!check()) {
                        status = STATUS_INSTALL_ERROR;
                        Message message = new Message();
                        message.what = status;
                        message.arg1 = UpgradeException.ERROR_CODE_PACKAGE_INVALID;
                        messageHandler.sendMessage(message);
                        return;
                    }
                }

                // status = STATUS_INSTALL_START;
                // messageHandler.sendEmptyMessage(STATUS_INSTALL_START);
                if (upgradeOption.isAutomountEnabled() && UpgradeUtil.isRooted()) {
                    boolean success = UpgradeUtil.installApk(upgradeOption.getStorage().getPath());
                    if (!success) {
                        status = STATUS_INSTALL_ERROR;
                        messageHandler.sendEmptyMessage(status);
                        return;
                    }

                    status = STATUS_INSTALL_ERROR;
                    Message message = Message.obtain();
                    message.what = status;
                    message.arg1 = UpgradeException.ERROR_CODE_PACKAGE_NO_ROOT;
                    messageHandler.sendMessage(message);
                    return;
                }
                UpgradeUtil.installApk(UpgradeService.this, upgradeOption.getStorage().getPath());
                // startTimer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 检测文件完整性
         *
         * @return
         */
        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        private boolean check() throws IOException {
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
            return false;
        }

        /**
         * 开始检测安装计时器
         */
        private void startTimer() {
            if (timer != null) {
                timer = null;
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (UpgradeUtil.isActivityTop(UpgradeService.this, getPackageName())) {
                        status = STATUS_INSTALL_CANCEL;
                        messageHandler.sendEmptyMessage(STATUS_INSTALL_CANCEL);
                        stopTimer();
                    }
                }
            }, 3000, 1000);
        }

        /**
         * 结束检测安装计时器
         */
        private void stopTimer() {
            if (timer != null) {
                timer.cancel();
            }
            timer = null;
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
            NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            // WIFI已连接，移动数据已连接
            if (wifiNetworkInfo.isConnected() && mobileNetworkInfo.isConnected()) {
                if (status == STATUS_DOWNLOAD_PAUSE) {
                    start();
                }
                return;
            }

            // WIFI已连接，移动数据已断开
            if (wifiNetworkInfo.isConnected() && !mobileNetworkInfo.isConnected()) {
                if (status == STATUS_DOWNLOAD_PAUSE) {
                    start();
                }
                return;
            }

            // WIFI已断开，移动数据已连接
            if (!wifiNetworkInfo.isConnected() && mobileNetworkInfo.isConnected()) {
                if (status == STATUS_DOWNLOAD_PAUSE) {
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
     * 程序状态变化广播
     */
    private class PackagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = null;
            if (intent.getData() != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }

            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                Log.i(TAG, "onReceive：Added " + packageName);
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                Log.i(TAG, "onReceive：Replaced " + packageName);

                status = STATUS_INSTALL_COMPLETE;
                Message message = Message.obtain();
                message.what = status;
                if (upgradeOption.isAutocleanEnabled()) {
                    message.arg1 = -1;
                }
                messageHandler.sendMessage(message);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                Log.i(TAG, "onReceive：Removed " + packageName);
            }
        }

        public void registerReceiver(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            intentFilter.addDataScheme("package");
            context.registerReceiver(this, intentFilter);
        }

        public void unregisterReceiver(Context context) {
            context.unregisterReceiver(this);
        }
    }

}
