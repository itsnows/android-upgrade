package com.itsnows.upgrade.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Observable;
import java.util.Observer;

/**
 * Author: itsnows
 * E-mail: xue.com.fei@outlook.com
 * CreatedTime: 19-8-9 上午11:50
 * <p>
 * NetworkStateReceiver
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final int NETWORK_STATE_CONNECTED = 9;
    private static final int NETWORK_STATE_DISCONNECTED = -1;
    private Context context;
    private Observable observable;

    public NetworkStateReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo ethernetNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        // wifi,mobile data,ethernet connected.
        if (isConnected(wifiNetworkInfo) ||
                isConnected(mobileNetworkInfo) ||
                isConnected(ethernetNetworkInfo)) {
            observable.notifyObservers(NETWORK_STATE_CONNECTED);
            return;
        }

        // wifi,mobile data,ethernet disconnected.
        observable.notifyObservers(NETWORK_STATE_DISCONNECTED);
    }

    /**
     * Register network state change listener interface.
     *
     * @param listener
     */
    public OnNetworkStateListener registerListener(OnNetworkStateListener listener) {
        if (observable == null) {
            observable = createObservable();
            registerReceiver();
        }
        if (listener != null) {
            observable.addObserver(listener);
        }
        return listener;
    }

    /**
     * Unregister network state change listener interface.
     *
     * @param listener
     */
    public void unregisterListener(OnNetworkStateListener listener) {
        if (observable == null) {
            return;
        }
        if (listener != null) {
            observable.deleteObserver(listener);
        }
        if (observable.countObservers() == 0) {
            unregisterReceiver();
        }
    }

    /**
     * Unregister all network state change listener interface.
     */
    public void unregisterAllListener() {
        if (observable != null) {
            observable.deleteObservers();
            unregisterReceiver();
        }
    }

    /**
     * Register network state change BroadcastReceiver.
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister network state change BroadcastReceiver.
     */
    private void unregisterReceiver() {
        context.unregisterReceiver(this);
    }

    /**
     * The current network is connected.
     *
     * @param info
     * @return
     */
    private boolean isConnected(NetworkInfo info) {
        if (info == null) {
            return false;
        }
        return info.isConnected();
    }

    /**
     * Create network state change {@link NetworkStateObservable}.
     *
     * @return
     */
    private Observable createObservable() {
        return new NetworkStateObservable();
    }

    /**
     * The network state change {@link Observable}.
     */
    private static class NetworkStateObservable extends Observable {

        @Override
        public void notifyObservers(Object arg) {
            setChanged();
            super.notifyObservers(arg);
        }
    }

    /**
     * The network state change listener interface {@link OnNetworkStateListener}.
     */
    public static abstract class OnNetworkStateListener implements Observer {

        @Override
        public void update(Observable observable, Object o) {
            if (o instanceof Integer) {
                int state = (int) o;
                switch (state) {
                    case NETWORK_STATE_CONNECTED:
                        onConnected();
                        break;
                    case NETWORK_STATE_DISCONNECTED:
                        onDisconnected();
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * The network connected calls the invoke method.
         */
        public abstract void onConnected();

        /**
         * The network disconnected calls the invoke method.
         */
        public abstract void onDisconnected();
    }

}
