package cc.chenhe.lib.weartools;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 晨鹤 on 2016/11/23.
 * 维护全局唯一的MobvoiApiClient。
 * 线程安全。
 */

public class WTApiClientManager {
    private static final String TAG = "WTApiClientManager";

    private static final int GET_CLIENT_CALLBACKS_QUEUE_SIZE = 50;

    private MobvoiApiClient apiClient;
    /**
     * 获取ApiClient回调列表
     */
    private LinkedBlockingQueue<GetClientCallback> callbacks;
    /**
     * Client connect成功回调
     */
    private MobvoiApiClient.ConnectionCallbacks connectionCallbacks = null;
    /**
     * Client connect失败回调
     */
    private MobvoiApiClient.OnConnectionFailedListener connectionFailedListener = null;


    private WTApiClientManager() {
        callbacks = new LinkedBlockingQueue<>(GET_CLIENT_CALLBACKS_QUEUE_SIZE);
    }

    /**
     * 内部类实现单例模式
     */
    private static class Holder {
        private static WTApiClientManager instance = new WTApiClientManager();
    }

    public static WTApiClientManager getInstance() {
        return Holder.instance;
    }

    /**
     * 获取client实例，添加回调。
     *
     * @param context  c
     * @param callback Client connect回调
     * @return 返回的client不一定连接成功。若Callback queue已满，则返回null.
     */
    public MobvoiApiClient getClient(@NonNull final Context context, @NonNull final GetClientCallback callback) {
        if (!callbacks.offer(callback)) {
            //队列已满
            callback.onConnectionFailed(null, null);
            return null;
        }
        return crateClient(context);
    }

    /**
     * 获取client实例，并通知所有回调。
     *
     * @param context c
     * @return MobvoiApiClient
     */
    private MobvoiApiClient crateClient(final Context context) {
        if (apiClient == null) {
            //client不存在。创建。
            WTLog.d(TAG, "crate ApiClient.");
            initConnectCallbacks();
            apiClient = new MobvoiApiClient.Builder(context.getApplicationContext())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
            WTLog.d(TAG, "ApiClient connecting...");
            apiClient.connect();
        } else {
            //正在连接，直接返回。
            if (apiClient.isConnecting()) return apiClient;
            /*
            已过时.
            若已经连接则直接通知所有回调。
            Callback数量!=1表示刚刚连接成功，暂未通知所有回调，
            应该在Callback list中排队等待通知。
             */
            if (apiClient.isConnected()) {
                WTLog.d(TAG, "Already connected, return ApiClient directly.");

                GetClientCallback callback;
                while ((callback = callbacks.poll()) != null)
                    callback.onConnected(apiClient);
            } else {
                initConnectCallbacks();
                apiClient.registerConnectionCallbacks(connectionCallbacks);
                apiClient.registerConnectionFailedListener(connectionFailedListener);
                apiClient.connect();
            }
        }
        return apiClient;
    }

    /**
     * 初始化Client连接回调
     */
    private void initConnectCallbacks() {
        if (connectionCallbacks == null)
            connectionCallbacks = new MobvoiApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    WTLog.d(TAG, "MobvoiApiClient connected.");
                    apiClient.unregisterConnectionCallbacks(this);
                    apiClient.unregisterConnectionFailedListener(connectionFailedListener);

                    GetClientCallback callback;
                    while ((callback = callbacks.poll()) != null)
                        callback.onConnected(apiClient);
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            };

        if (connectionFailedListener == null)
            connectionFailedListener = new MobvoiApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    WTLog.e(TAG, "MobvoiApiClient connect failed.");
                    apiClient.unregisterConnectionCallbacks(connectionCallbacks);
                    apiClient.unregisterConnectionFailedListener(this);

                    GetClientCallback callback;
                    while ((callback = callbacks.poll()) != null)
                        callback.onConnectionFailed(apiClient, connectionResult);
                }
            };
    }

    public interface GetClientCallback {
        void onConnected(MobvoiApiClient client);

        /**
         * connect失败。如果是Callback queue已满导致的失败则参数全为null。
         *
         * @param client           ApiClient
         * @param connectionResult Result
         */
        void onConnectionFailed(@Nullable MobvoiApiClient client, @Nullable ConnectionResult connectionResult);
    }
}
