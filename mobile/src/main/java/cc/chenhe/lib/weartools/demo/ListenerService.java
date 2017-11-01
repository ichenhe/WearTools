package cc.chenhe.lib.weartools.demo;

import android.net.Uri;
import android.util.Log;

import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataMap;
import com.mobvoi.android.wearable.PutDataMapRequest;

import java.io.IOException;
import java.io.InputStream;

import cc.chenhe.lib.weartools.WTBothway;
import cc.chenhe.lib.weartools.WTSender;
import cc.chenhe.lib.weartools.listener.WTListenerService;

/**
 * Created by 晨鹤 on 2017/11/1.
 * 监听手表消息的服务。自动管理生命周期，有新消息会被自动唤醒。
 */

public class ListenerService extends WTListenerService {
    private static final String TAG = "ListenerService";

    @Override
    public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
        if (path.equals("/image")) {
            Log.i(TAG, "Receive image request.");
            try {
                InputStream in = getResources().getAssets().open("image2.webp");
                int len = in.available();
                byte[] buffer = new byte[len];
                in.read(buffer);
                in.close();

                Asset asset = Asset.createFromBytes(buffer);
                PutDataMapRequest putDataMapRequest = WTBothway.getPutDataMapRequest(path);
                putDataMapRequest.setUrgent();
                putDataMapRequest.getDataMap().putAsset("image", asset);
                Log.i(TAG, "Sending reply image data...");
                WTBothway.responseDataItem(this, bothwayId, putDataMapRequest, new WTSender.SendDataCallback() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG, "Send reply image OK.");
                    }

                    @Override
                    public void onFailed(int resultCode) {
                        Log.e(TAG, "Send reply image failed.");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDataChanged(String path, DataMap dataMap) {

    }

    @Override
    public void onDataDeleted(String path) {

    }

    private void sendImage() {

    }
}
