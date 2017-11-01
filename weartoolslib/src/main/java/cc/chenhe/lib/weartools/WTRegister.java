package cc.chenhe.lib.weartools;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Wearable;

import cc.chenhe.lib.weartools.listener.WTDataListener;
import cc.chenhe.lib.weartools.listener.WTMessageListener;
import cc.chenhe.lib.weartools.listener.WTResponseDataListener;
import cc.chenhe.lib.weartools.listener.WTResponseMsgListener;

/**
 * Created by 晨鹤 on 2016/11/26.
 * 用于注册/移除各种监听器。
 */

public class WTRegister {
    private static final String TAG = "WTRegister";

    /**
     * 添加data监听器。
     *
     * @param context      c
     * @param dataListener 监听器
     */
    private static void addUDataListener(
            Context context, @NonNull final DataApi.DataListener dataListener) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.DataApi.addListener(client, dataListener);
                WTLog.v(TAG, "Add data listener success.");
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                WTLog.e(TAG, "Add data listener fail: client connect failed: error code: "
                        + connectionResult.getErrorCode());
            }
        });
    }

    /**
     * 添加data监听器。
     */
    public static void addDataListener(Context context, @NonNull WTDataListener listener) {
        addUDataListener(context, listener);
    }

    /**
     * 添加data监听器。
     */
    public static void addDataListener(Context context, @NonNull WTResponseDataListener listener) {
        addUDataListener(context, listener);
    }

    /**
     * 移除data监听器。
     *
     * @param context      必须与添加时相同
     * @param dataListener 要移除的监听器
     */
    private static void removeUDataListener(
            Context context, @NonNull final DataApi.DataListener dataListener) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.DataApi.removeListener(client, dataListener);
                WTLog.v(TAG, "Remove data listener success.");
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                WTLog.e(TAG, "Remove data listener fail: client connect failed: error code: "
                        + connectionResult.getErrorCode());
            }
        });
    }

    /**
     * 移除data监听器。
     */
    public static void removeDataListener(Context context, @NonNull WTDataListener listener) {
        removeUDataListener(context, listener);
    }

    /**
     * 移除data监听器。
     */
    public static void removeDataListener(Context context, @NonNull WTResponseDataListener listener) {
        removeUDataListener(context, listener);
    }

    /**
     * 添加message监听器。
     *
     * @param context         c
     * @param messageListener 监听器
     */
    private static void addUMessageListener(
            Context context, @NonNull final MessageApi.MessageListener messageListener) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.MessageApi.addListener(client, messageListener);
                WTLog.v(TAG, "Add msg listener success.");
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                WTLog.e(TAG, "Add msg listener fail: client connect failed: error code: " +
                        connectionResult.getErrorCode());
            }
        });
    }

    /**
     * 添加message监听器。
     */
    public static void addMessageListener(
            Context context, @NonNull final WTMessageListener messageListener) {
        addUMessageListener(context, messageListener);
    }

    /**
     * 添加message监听器。
     */
    public static void addMessageListener(
            Context context, @NonNull final WTResponseMsgListener messageListener) {
        addUMessageListener(context, messageListener);
    }

    /**
     * 移除message监听器。
     *
     * @param context         必须与添加时相同
     * @param messageListener 要移除的监听器
     */
    private static void removeUMessageListener(
            Context context, @NonNull final MessageApi.MessageListener messageListener) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.MessageApi.removeListener(client, messageListener);
                WTLog.v(TAG, "Remove msg listener success.");
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                WTLog.e(TAG, "Remove msg listener fail: client connect failed: error code: " +
                        connectionResult.getErrorCode());
            }
        });
    }

    /**
     * 移除message监听器。
     *
     * @param context         必须与添加时相同
     * @param messageListener 要移除的监听器
     */
    public static void removeMessageListener(
            Context context, @NonNull final WTMessageListener messageListener) {
        removeUMessageListener(context, messageListener);
    }

    /**
     * 移除message监听器。
     *
     * @param context         必须与添加时相同
     * @param messageListener 要移除的监听器
     */
    public static void removeMessageListener(
            Context context, @NonNull final WTResponseMsgListener messageListener) {
        removeUMessageListener(context, messageListener);
    }
}
