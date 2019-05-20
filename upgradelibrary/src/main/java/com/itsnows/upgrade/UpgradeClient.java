package com.itsnows.upgrade;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.itsnows.upgrade.model.bean.UpgradeOptions;
import com.itsnows.upgrade.service.UpgradeService;

import java.lang.ref.WeakReference;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 2018/3/19 11:09
 * <p>
 * UpgradeClient
 */
public class UpgradeClient {
    private static final String TAG = UpgradeClient.class.getSimpleName();
    private Context context;
    private UpgradeOptions options;
    private Messenger client;
    private Messenger server;
    private ServiceConnection connection;
    private onConnectLisenter onConnectLisenter;
    private OnDownloadListener onDownloadListener;
    private OnInstallListener onInstallListener;
    private boolean isConnected;

    /**
     * 添加客户端实例，同时绑定活动
     *
     * @param context
     * @param options
     * @return
     */
    public static UpgradeClient add(Context context, UpgradeOptions options) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null");
        }
        return new UpgradeClient(context, options);
    }

    private UpgradeClient(Context context, UpgradeOptions options) {
        this.context = context;
        this.options = options;
        this.client = new Messenger(new ClientHandler(this));
        this.connection = new UpgradeServiceConnection();
    }

    /**
     * 注入连接监听回调接口
     *
     * @param onConnectLisenter
     */
    public void setOnConnectLisenter(onConnectLisenter onConnectLisenter) {
        this.onConnectLisenter = onConnectLisenter;
    }

    /**
     * 注入下载监听回调接口
     *
     * @param onDownloadListener
     */
    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    /**
     * 注入安装监听回调接口
     *
     * @param onInstallListener
     */
    public void setOnInstallListener(OnInstallListener onInstallListener) {
        this.onInstallListener = onInstallListener;
    }

    /**
     * 销毁客户端实例，同时解除绑定活动
     */
    public void remove() {
        disconnect();
    }

    /**
     * 开始
     */
    public void start() {
        if (!isConnected) {
            bind();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (isConnected) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_DOWNLOAD_PAUSE_REQ, null);
        }
    }

    /**
     * 继续
     */
    public void resume() {
        if (isConnected) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_DOWNLOAD_RESUME_REQ, null);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        if (isConnected) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_DOWNLOAD_RESUME_REQ, null);
        }
    }

    /**
     * 安装
     */
    public void install() {
        if (isConnected) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_INSTALL_START_REQ, null);
        }
    }

    /**
     * 绑定升级服务
     */
    private void bind() {
        if (context instanceof Activity || context instanceof Service) {
            UpgradeService.start(context, options, connection);
            return;
        }
        UpgradeService.start(context, options);
    }

    /**
     * 解绑升级服务
     */
    private void unbind() {
        if (context != null && connection != null) {
            context.unbindService(connection);
        }
    }

    /**
     * 连接升级服务
     */
    private void connect() {
        if (!isConnected) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_CONNECT_REQ, null);
        }
    }

    /**
     * 断开升级服务
     */
    private void disconnect() {
        if (isConnected && client != null && server != null) {
            sendMessageToServer(UpgradeConstant.MSG_KEY_DISCONNECT_REQ, null);
        }
    }

    /**
     * 是否连接到升级服务
     *
     * @return
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 发送消息到服务端
     *
     * @param key
     * @param data
     */
    private void sendMessageToServer(int key, Bundle data) {
        try {
            Message message = Message.obtain();
            message.replyTo = client;
            message.what = key;
            message.setData(data == null ? new Bundle() : data);
            server.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Handler {
        private WeakReference<UpgradeClient> reference;

        private ClientHandler(UpgradeClient client) {
            this.reference = new WeakReference<>(client);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeClient client = reference.get();
            if (client == null) {
                return;
            }
            Bundle data = msg.getData();
            int code = data.getInt("code");
            String message = data.getString("message");
            switch (msg.what) {
                case UpgradeConstant.MSG_KEY_CONNECT_RESP:
                    if (code == 0) {
                        client.isConnected = true;
                        if (client.onConnectLisenter != null) {
                            client.onConnectLisenter.onConnected();
                        }
                    }
                    Log.d(TAG, message);
                    break;
                case UpgradeConstant.MSG_KEY_DISCONNECT_RESP:
                    if (code == 0) {
                        if (client.onConnectLisenter != null) {
                            client.onConnectLisenter.onDisconnected();
                            client.isConnected = false;
                        }
                    }
                    Log.d(TAG, message);
                    client.unbind();
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_START_RESP:
                    Log.d(TAG, "Download：onStart");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onStart();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_PROGRESS_RESP:
                    Log.d(TAG, "Download：onProgress：");
                    long max = data.getLong("max");
                    long progress = data.getLong("progress");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onProgress(max, progress);
                    }
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_PAUSE_RESP:
                    Log.d(TAG, "Download：onPause");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onPause();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_CANCEL_RESP:
                    Log.d(TAG, "Download：onCancel");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onCancel();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_ERROR_RESP:
                    Log.d(TAG, "Download：onError");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onError(new UpgradeException());
                    }
                    break;
                case UpgradeConstant.MSG_KEY_DOWNLOAD_COMPLETE_RESP:
                    Log.d(TAG, "Download：onComplete");
                    if (client.onDownloadListener != null) {
                        client.onDownloadListener.onComplete();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_CHECK_RESP:
                    Log.d(TAG, "Install：onCheck");
                    if (client.onInstallListener != null) {
                        client.onInstallListener.onCheck();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_START_RESP:
                    Log.d(TAG, "Install：onStart");
                    if (client.onInstallListener != null) {
                        client.onInstallListener.onStart();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_CANCEL_RESP:
                    Log.d(TAG, "Install：onCancel");
                    if (client.onInstallListener != null) {
                        client.onInstallListener.onCancel();
                    }
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_ERROR_RESP:
                    Log.d(TAG, "Install：onError");
                    if (client.onInstallListener != null) {
                        client.onInstallListener.onError(new UpgradeException(code));
                    }
                    break;
                case UpgradeConstant.MSG_KEY_INSTALL_COMPLETE_RESP:
                    Log.d(TAG, "Install：onComplete");
                    if (client.onInstallListener != null) {
                        client.onInstallListener.onComplete();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private class UpgradeServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            server = new Messenger(service);
            connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            server = null;
            client = null;
            isConnected = false;
        }
    }

}
