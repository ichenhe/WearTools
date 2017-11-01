package cc.chenhe.lib.weartools.demo;

import android.util.Log;

import com.mobvoi.android.wearable.DataMap;

import cc.chenhe.lib.weartools.listener.WTListenerService;

/**
 * Created by 晨鹤 on 2017/10/31.
 * 监听手机消息的服务。自动管理生命周期，有新消息会被自动唤醒。
 */

public class ListenerService extends WTListenerService {
    private static final String TAG = "ListenerService";

    @Override
    public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
        if (bothwayId == null) {
            Log.i(TAG, "Receive msg: " + new String(data));
        } else {
            Log.i(TAG, "Receive bothway request msg: " + new String(data));
        }
    }

    @Override
    public void onDataChanged(String path, DataMap dataMap) {
        if (path.equals("/image")) {
            Log.i(TAG, "Receive image.");
            return;
        }

        String s = dataMap.getString("test");
        Log.i(TAG, "Receive data: " + s);
    }

    @Override
    public void onDataDeleted(String path) {
        Log.i(TAG, "Del data: " + path);
    }
}
