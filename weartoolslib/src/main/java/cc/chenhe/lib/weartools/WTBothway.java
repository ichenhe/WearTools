package cc.chenhe.lib.weartools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.util.LongSparseArray;

import com.mobvoi.android.wearable.DataMap;
import com.mobvoi.android.wearable.PutDataMapRequest;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import cc.chenhe.lib.weartools.bean.ResponseDataBean;
import cc.chenhe.lib.weartools.listener.WTResponseDataListener;
import cc.chenhe.lib.weartools.listener.WTResponseMsgListener;

/**
 * Created by 晨鹤 on 2016/11/26.
 * 双向通讯，请求/响应模型。有超时判断。
 */
public class WTBothway {
    private static final String TAG = "WTBothway";

    public static final int RESULT_FAILED_SEND = -1;
    public static final int RESULT_FAILED_WAIT_TIME_OUT = -2;

    private static final int HANDLER_SEND_FAILED = 0;
    private static final int HANDLER_RECEIVE_RESPONSE = 1;
    private static final int HANDLER_RESPONSE_TIME_OUT = 2;

    public static final String BOTHWAY_ID_KEY = "WTBothwayId";
    public static final String BOTHWAY_TYPE_KEY = "WTBothwayType";
    public static final byte[] BOTHWAY_REQUEST_FLAG = {
            0x1, 0x1, 0x1, 0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0};
    public static final byte[] BOTHWAY_REPLY_FLAG = {
            0x1, 0x1, 0x1, 0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x1};

    private static LongSparseArray<WTResponseMsgListener> msgListenerMap = new LongSparseArray<>();
    private static LongSparseArray<WTResponseDataListener> dataListenerMap = new LongSparseArray<>();

    /**
     * @param data String
     */
    public static void request(final Context context, String path, String data, final BothwayCallback callback) {
        request(context, path, data.getBytes(), WTUtils.getBothwayTimeOut(), callback);
    }

    /**
     * @param data byte[]
     */
    public static void request(final Context context, String path, byte[] data, final BothwayCallback callback) {
        request(context, path, data, WTUtils.getBothwayTimeOut(), callback);
    }

    /**
     * 发出请求，监听Msg响应，若指定时间内没有收到返回则超时。
     *
     * @param context  c
     * @param path     必须以/开头
     * @param data     byte[]
     * @param timeout  超时
     * @param callback 主线程回调，可null
     */
    public static void request(
            final Context context, @NonNull final String path, @NonNull byte[] data,
            final long timeout, @NonNull final BothwayCallback callback) {
        final Timer timer = new Timer();
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                timer.cancel();
                switch (msg.what) {
                    case HANDLER_SEND_FAILED:
                        WTLog.e(TAG, "Send bothway msg failed.");
                        callback.onFailed(RESULT_FAILED_SEND);
                        break;
                    case HANDLER_RECEIVE_RESPONSE:
                        Bundle bundle = msg.getData();
                        WTLog.v(TAG, "Receive msg response. path:" + bundle.getString("path"));
                        callback.onRespond(bundle.getByteArray("data"));
                        break;
                    case HANDLER_RESPONSE_TIME_OUT:
                        WTLog.e(TAG, "Bothway send or wait timeout.");
                        callback.onFailed(RESULT_FAILED_WAIT_TIME_OUT);
                        break;
                }
            }
        };

        //-------发送请求
        //nanoTime作为listener的key，不可用于超时判断。
        final long nanoTime = System.nanoTime();
        WTLog.v(TAG, "Send bothway msg. path:" + path);
        WTSender.sendMessage(context, path, mergeByte(BOTHWAY_REQUEST_FLAG, long2Bytes(nanoTime), data), timeout, new WTSender.SendMsgCallback() {
            @Override
            public void onSuccess() {
                regResponseMsgListener(context, nanoTime, handler, long2Bytes(nanoTime));
            }

            @Override
            public void onFailed(int resultCode) {
                handler.sendEmptyMessage(HANDLER_SEND_FAILED);
            }
        });

        //设置超时
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                if (msgListenerMap.get(nanoTime) != null) {
                    WTRegister.removeMessageListener(context, msgListenerMap.get(nanoTime));
                    msgListenerMap.remove(nanoTime);
                }
                handler.sendEmptyMessage(HANDLER_RESPONSE_TIME_OUT);
            }
        }, timeout);
    }

    /**
     * @param data String
     */
    public static void request(final Context context, String path, String data, final BothwayCallback4DataMap callback) {
        request(context, path, data.getBytes(), WTUtils.getBothwayTimeOut(), callback);
    }

    /**
     * @param data byte[]
     */
    public static void request(final Context context, String path, byte[] data, final BothwayCallback4DataMap callback) {
        request(context, path, data, WTUtils.getBothwayTimeOut(), callback);
    }

    /**
     * 发出请求,监听dataMap响应，若指定时间内没有收到返回则超时。
     *
     * @param context  c
     * @param path     必须以/开头
     * @param data     byte[]
     * @param timeout  超时
     * @param callback 主线程回调，可null
     */
    public static void request(
            final Context context, @NonNull final String path, @NonNull final byte[] data,
            final long timeout, @NonNull final BothwayCallback4DataMap callback) {
        final Timer timer = new Timer();
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                timer.cancel();
                switch (msg.what) {
                    case HANDLER_SEND_FAILED:
                        WTLog.e(TAG, "Send bothway msg failed.");
                        callback.onFailed(RESULT_FAILED_SEND);
                        break;
                    case HANDLER_RECEIVE_RESPONSE:
                        final ResponseDataBean dataBean = (ResponseDataBean) msg.obj;
                        WTLog.v(TAG, "Receive data map response. path:" + dataBean.path);
                        callback.onRespond(dataBean.dataMap);
                        break;
                    case HANDLER_RESPONSE_TIME_OUT:
                        WTLog.e(TAG, "Bothway send or wait timeout.");
                        callback.onFailed(RESULT_FAILED_WAIT_TIME_OUT);
                        break;
                }
            }
        };

        //发送请求
        final long nanoTime = System.nanoTime();
        WTLog.v(TAG, "Send bothway msg. path:" + path);
        WTSender.sendMessage(context, path, mergeByte(BOTHWAY_REQUEST_FLAG, long2Bytes(nanoTime), data), timeout, new WTSender.SendMsgCallback() {
            @Override
            public void onSuccess() {
                regResponseDataListener(context, nanoTime, handler, long2Bytes(nanoTime));
            }

            @Override
            public void onFailed(int resultCode) {
                handler.sendEmptyMessage(HANDLER_SEND_FAILED);
            }
        });

        //设置超时
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                if (dataListenerMap.get(nanoTime) != null) {
                    WTLog.v(TAG, "Remove response data listener.");
                    WTRegister.removeDataListener(context, dataListenerMap.get(nanoTime));
                    dataListenerMap.remove(nanoTime);
                }
                handler.sendEmptyMessage(HANDLER_RESPONSE_TIME_OUT);
            }
        }, timeout);
    }

    /**
     * @param data     String
     * @param callback 发送响应Msg回调
     */
    public static void response(
            Context context, @NonNull String nodeId, @NonNull String path, @NonNull byte[] bothwayId,
            @NonNull String data, @Nullable WTSender.SendMsgCallback callback) {
        response(context, nodeId, path, bothwayId, data.getBytes(), callback);
    }

    /**
     * 响应请求
     *
     * @param context   c
     * @param nodeId    发出请求的节点id
     * @param path      原始path
     * @param bothwayId 请求id
     * @param data      byte[]
     * @param callback  发送响应Msg回调
     */
    public static void response(
            Context context, @NonNull String nodeId, @NonNull String path, @NonNull byte[] bothwayId,
            @NonNull byte[] data, @Nullable WTSender.SendMsgCallback callback) {
        WTSender.sendMessage(context, nodeId, path, mergeByte(
                BOTHWAY_REPLY_FLAG, bothwayId, data),
                WTUtils.getTimeOut(), callback);
    }


    /**
     * 获取发送data所需的DataMapReq.
     *
     * @param path 请求id
     * @return 可调用getDataMap()获取map对象来添加数据。
     */
    public static PutDataMapRequest getPutDataMapRequest(String path) {
        return WTSender.getPutDataMapRequest(path);
    }

    /**
     * 以DataMap形式响应请求，适用于传输多个数据或大文件。
     *
     * @param context           c
     * @param bothwayId         请求id
     * @param putDataMapRequest 用getPutDataMapRequest()获得
     * @param callback          发送响应Data回调
     */
    public static void responseDataItem(
            final Context context, @NonNull byte[] bothwayId,
            @NonNull final PutDataMapRequest putDataMapRequest,
            @Nullable final WTSender.SendDataCallback callback) {
        putDataMapRequest.getDataMap().putByteArray(BOTHWAY_ID_KEY, bothwayId);
        putDataMapRequest.getDataMap().putByteArray(BOTHWAY_TYPE_KEY, BOTHWAY_REPLY_FLAG);
        WTSender.sendData(context, putDataMapRequest, callback);
    }

    /**
     * 注册监听响应的msg.
     *
     * @param context        c
     * @param msgListenerKey listerId 用于在map中标识监听器
     * @param handler        用于回传响应的数据
     * @param sendBothwayId  发送时的BothwayId.
     */
    private static void regResponseMsgListener(
            final Context context, final long msgListenerKey,
            @NonNull final Handler handler, @NonNull final byte[] sendBothwayId) {
        WTResponseMsgListener listener = new WTResponseMsgListener() {
            @Override
            public void onResponseMsgReceived(String path, byte[] data, byte[] bothwayId) {
                //判断与发送时的path是否对应，避免同时发送2个请求时响应混乱。
                if (!Arrays.equals(sendBothwayId, bothwayId)) return;

                //双向通讯响应path格式为：/RE/WTBothway/{nanoTime}/xxx
                Message msg = handler.obtainMessage(HANDLER_RECEIVE_RESPONSE);
                Bundle bundle = new Bundle();
                bundle.putByteArray("data", data);
                bundle.putString("path", path);
                msg.setData(bundle);
                handler.sendMessage(msg);
                if (msgListenerMap.get(msgListenerKey) != null) {
                    WTLog.v(TAG, "Remove response msg listener.");
                    WTRegister.removeMessageListener(context, msgListenerMap.get(msgListenerKey));
                    msgListenerMap.remove(msgListenerKey);
                }
            }
        };
        WTLog.v(TAG, "Register response msg listener.");
        msgListenerMap.put(msgListenerKey, listener);
        WTRegister.addMessageListener(context, listener);
    }

    /**
     * 注册监听响应的DataMap.
     *
     * @param context         c
     * @param dataListenerKey listerId 用于在map中标识监听器
     * @param handler         用于回传响应的数据
     * @param sendBothwayId   发送时的BothwayId.
     */
    private static void regResponseDataListener(
            final Context context, final long dataListenerKey,
            @NonNull final Handler handler, @NonNull final byte[] sendBothwayId) {
        WTResponseDataListener listener = new WTResponseDataListener() {
            @Override
            public void onResponseDataChanged(byte[] bothwayId, String path, DataMap dataMap) {
                //判断与发送时的path是否对应，避免同时发送2个请求时响应混乱。
                if (!Arrays.equals(sendBothwayId, bothwayId)) return;

                //双向通讯响应path格式为：/RE/WTBothway/{nanoTime}/xxx
                Message msg = handler.obtainMessage(HANDLER_RECEIVE_RESPONSE);
                msg.obj = new ResponseDataBean(dataMap, path);
                handler.sendMessage(msg);
                if (dataListenerMap.get(dataListenerKey) != null) {
                    WTRegister.removeDataListener(context, dataListenerMap.get(dataListenerKey));
                    dataListenerMap.remove(dataListenerKey);
                }
            }
        };
        dataListenerMap.put(dataListenerKey, listener);
        WTRegister.addDataListener(context, listener);
    }

    /**
     * 合并三个byte数组
     *
     * @param data1 第一个数组
     * @param data2 第二个数组
     * @param data3 第三个数组
     * @return 合并后的数组
     */
    private static byte[] mergeByte(@NonNull byte[] data1, @NonNull byte[] data2,
                                    @NonNull byte[] data3) {
        byte[] d = new byte[data1.length + data2.length + data3.length];
        System.arraycopy(data1, 0, d, 0, data1.length);
        System.arraycopy(data2, 0, d, data1.length, data2.length);
        System.arraycopy(data3, 0, d, data1.length + data2.length, data3.length);
        return d;
    }

    /**
     * 将long转换为byte数组
     *
     * @param values 要转换的数据
     * @return 转换后的数组
     */
    public static byte[] long2Bytes(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public interface BothwayCallback4DataMap {
        @UiThread
        void onRespond(DataMap data);

        @UiThread
        void onFailed(int resultCode);
    }

    public interface BothwayCallback {
        @UiThread
        void onRespond(byte[] data);

        @UiThread
        void onFailed(int resultCode);
    }

}
