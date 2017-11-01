package cc.chenhe.lib.weartools;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.PendingResult;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.PutDataMapRequest;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于向手表发送数据。
 * Created by 晨鹤 on 2016/11/24.
 */

public class WTSender {
    private static final String TAG = "WTSender";

    public static final int RESULT_FAILED_CONNECT_API_CLIENT = -1;
    public static final int RESULT_FAILED_TIME_OUT = -3;
    public static final int RESULT_FAILED_CANCELED = -4;
    public static final int RESULT_FAILED_INTERRUPTED = -5;
    public static final int RESULT_FAILED_NO_DEVICE_CONNECTED = -6;//仅msg返回
    public static final int RESULT_FAILED_UNKNOWN = -7;

    /**
     * 发送Message结果回调
     */
    public interface SendMsgCallback {
        /*此处不代表对方已经收到*/
        void onSuccess();

        void onFailed(int resultCode);
    }

    /**
     * 删除Data结果回调
     */
    public interface DeleteDataCallback {
        /*此处不代表对方已经收到*/
        void onSuccess();

        void onFailed(int resultCode);
    }

    /**
     * 发送Data结果回调
     */
    public interface SendDataCallback {
        /*此处不代表对方已经收到*/
        void onSuccess(Uri uri);

        void onFailed(int resultCode);
    }

    /**
     * 发送message给指定设备。
     *
     * @param data String
     */
    public static void sendMessage(
            Context context, @NonNull final String nodeId, @NonNull final String path,
            @NonNull final String data, @Nullable final WTSender.SendMsgCallback callback) {
        sendMessage(context, nodeId, path, data.getBytes(), WTUtils.getTimeOut(), callback);
    }

    /**
     * 发送message给指定设备。
     *
     * @param context  c
     * @param nodeId   目标设备id
     * @param path     必须以/开头
     * @param data     byte[]
     * @param timeout  发送超时
     * @param callback 结果回调
     */
    public static void sendMessage(
            Context context, @NonNull final String nodeId, @NonNull final String path,
            @NonNull final byte[] data, final long timeout,
            @Nullable final WTSender.SendMsgCallback callback) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(final MobvoiApiClient client) {
                //获得所有节点
                Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        List<Node> nodes = getConnectedNodesResult.getNodes();
                        //检查目标节点是否存在
                        boolean exist = false;
                        for (Node node : nodes) {
                            if (node.getId().equals(nodeId)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            //目标节点不存在
                            if (callback != null)
                                callback.onFailed(WTSender.RESULT_FAILED_NO_DEVICE_CONNECTED);
                            return;
                        }
                        //发送并设置结果回调
                        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(client, nodeId, path, data);
                        result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult result) {
                                if (callback != null) {
                                    if (result.getStatus().isSuccess()) {
                                        callback.onSuccess();
                                    } else if (result.getStatus().isTimeout()) {
                                        callback.onFailed(WTSender.RESULT_FAILED_TIME_OUT);
                                    } else if (result.getStatus().isCanceled()) {
                                        callback.onFailed(WTSender.RESULT_FAILED_CANCELED);
                                    } else if (result.getStatus().isInterrupted()) {
                                        callback.onFailed(WTSender.RESULT_FAILED_INTERRUPTED);
                                    } else {
                                        callback.onFailed(WTSender.RESULT_FAILED_UNKNOWN);
                                    }
                                }
                            }
                        }, timeout, TimeUnit.MILLISECONDS);
                    }
                });
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                if (callback != null)
                    callback.onFailed(WTSender.RESULT_FAILED_CONNECT_API_CLIENT);
            }
        });
    }

    /**
     * 发送message给所有当前连接的设备。以发送到第一个设备的结果作为最终结果。
     *
     * @param data String类型
     */
    public static void sendMessage(
            Context context, @NonNull final String path, @NonNull final String data,
            @Nullable final WTSender.SendMsgCallback callback) {
        sendMessage(context, path, data.getBytes(), WTUtils.getTimeOut(), callback);
    }

    /**
     * 发送message给所有当前连接的设备。以发送到第一个设备的结果作为最终结果。
     *
     * @param context  c
     * @param path     必须以/开头
     * @param data     byte[]类型
     * @param timeout  发送超时
     * @param callback 结果回调
     */
    public static void sendMessage(
            Context context, @NonNull final String path, @NonNull final byte[] data,
            final long timeout, @Nullable final WTSender.SendMsgCallback callback) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(final MobvoiApiClient client) {
                //取得所有节点
                Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        List<String> ids = new ArrayList<>();
                        for (Node node : getConnectedNodesResult.getNodes())
                            ids.add(node.getId());
                        if (ids.size() == 0) {
                            //没有节点连接
                            if (callback != null)
                                callback.onFailed(WTSender.RESULT_FAILED_NO_DEVICE_CONNECTED);
                            return;
                        }
                        //循环发送，以第一个结果作为最终结果通知回调
                        for (int i = 0; i < ids.size(); i++) {
                            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(client, ids.get(i), path, data);
                            if (i == 0) {
                                result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                    @Override
                                    public void onResult(MessageApi.SendMessageResult result) {
                                        if (callback != null) {
                                            if (result.getStatus().isTimeout()) {
                                                callback.onFailed(WTSender.RESULT_FAILED_TIME_OUT);
                                            } else if (result.getStatus().isCanceled()) {
                                                callback.onFailed(WTSender.RESULT_FAILED_CANCELED);
                                            } else if (result.getStatus().isInterrupted()) {
                                                callback.onFailed(WTSender.RESULT_FAILED_INTERRUPTED);
                                            } else if (result.getStatus().isSuccess()) {
                                                callback.onSuccess();
                                            } else {
                                                callback.onFailed(WTSender.RESULT_FAILED_UNKNOWN);
                                            }
                                        }
                                    }
                                }, timeout, TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                });


            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                if (callback != null)
                    callback.onFailed(WTSender.RESULT_FAILED_CONNECT_API_CLIENT);
            }
        });
    }

    /**
     * 获取发送data所需的DataMapReq。
     *
     * @param path 必须以/开头
     * @return 可调用getDataMap()获取map对象来添加数据。
     */
    public static PutDataMapRequest getPutDataMapRequest(@NonNull String path) {
        return PutDataMapRequest.create(path);
    }

    /**
     * 发送data，使用全局超时设置。
     */
    public static void sendData(
            Context context, @NonNull final PutDataMapRequest putDataMapRequest,
            @Nullable final WTSender.SendDataCallback callback) {
        sendData(context, putDataMapRequest, callback, WTUtils.getTimeOut());
    }

    /**
     * 发送data
     *
     * @param context           c
     * @param putDataMapRequest 使用getPutDataMapRequest()取得
     * @param callback          结果回调
     * @param timeOut           设置超时
     */
    private static void sendData(
            Context context, @NonNull final PutDataMapRequest putDataMapRequest,
            @Nullable final WTSender.SendDataCallback callback, final long timeOut) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(final MobvoiApiClient client) {
                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> result = Wearable.DataApi.putDataItem(client, putDataRequest);
                result.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (callback != null) {
                            if (dataItemResult.getStatus().isTimeout()) {
                                callback.onFailed(WTSender.RESULT_FAILED_TIME_OUT);
                            } else if (dataItemResult.getStatus().isCanceled()) {
                                callback.onFailed(WTSender.RESULT_FAILED_CANCELED);
                            } else if (dataItemResult.getStatus().isInterrupted()) {
                                callback.onFailed(WTSender.RESULT_FAILED_INTERRUPTED);
                            } else if (dataItemResult.getStatus().isSuccess()) {
                                callback.onSuccess(dataItemResult.getDataItem().getUri());
                            } else {
                                callback.onFailed(WTSender.RESULT_FAILED_UNKNOWN);
                            }
                        }
                    }
                }, timeOut, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                if (callback != null)
                    callback.onFailed(WTSender.RESULT_FAILED_CONNECT_API_CLIENT);
            }
        });
    }

    /**
     * 发送删除data信息。手表将收到相关回调。此函数不能与sendData()相抵消。使用全局超时设置
     *
     * @param context  c
     * @param uri      由putDataMapRequest.getUri()获得
     * @param callback 结果回调
     */
    public static void deleteData(
            Context context, @NonNull final Uri uri, @Nullable final DeleteDataCallback callback) {
        deleteData(context, uri, WTUtils.getTimeOut(), callback);
    }

    /**
     * 发送删除data信息。手表将收到相关回调。此函数不能与sendData()相抵消。
     *
     * @param context  c
     * @param uri      putDataMapRequest.getUri()
     * @param timeout  超时
     * @param callback 结果回调
     */
    public static void deleteData(
            Context context, @NonNull final Uri uri, final long timeout,
            @Nullable final DeleteDataCallback callback) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager.GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.DataApi.deleteDataItems(client, uri).setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
                    @Override
                    public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                        if (callback != null) {
                            if (deleteDataItemsResult.getStatus().isSuccess()) {
                                callback.onSuccess();
                            } else if (deleteDataItemsResult.getStatus().isTimeout()) {
                                callback.onFailed(WTSender.RESULT_FAILED_TIME_OUT);
                            } else if (deleteDataItemsResult.getStatus().isCanceled()) {
                                callback.onFailed(WTSender.RESULT_FAILED_CANCELED);
                            } else if (deleteDataItemsResult.getStatus().isInterrupted()) {
                                callback.onFailed(WTSender.RESULT_FAILED_INTERRUPTED);
                            } else {
                                callback.onFailed(WTSender.RESULT_FAILED_UNKNOWN);
                            }
                        }
                    }
                }, timeout, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onConnectionFailed(MobvoiApiClient client, ConnectionResult connectionResult) {
                if (callback != null)
                    callback.onFailed(WTSender.RESULT_FAILED_CONNECT_API_CLIENT);
            }
        });
    }
}
