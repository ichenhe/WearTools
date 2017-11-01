package cc.chenhe.lib.weartools.listener;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;

import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import cc.chenhe.lib.weartools.WTBothway;

/**
 * Created by 晨鹤 on 2016/11/26.
 * 监听Message消息。
 * 封装了官方的MsgListener，增加对双向通讯的判断。
 */

public abstract class WTMessageListener implements MessageApi.MessageListener {

    private static class MyHandler extends Handler {
        private WeakReference<WTMessageListener> mMessageListenerWeakReference;

        public MyHandler(WTMessageListener messageListener) {
            mMessageListenerWeakReference = new WeakReference<>(messageListener);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WTMessageListener messageListener = mMessageListenerWeakReference.get();
            if (messageListener == null) return;

            MessageEvent messageEvent = (MessageEvent) msg.obj;
            byte[] data = messageEvent.getData();
            byte[] bothwayId = null;


            byte[] prefix = Arrays.copyOf(data, WTBothway.BOTHWAY_REQUEST_FLAG.length);
            if (Arrays.equals(prefix, WTBothway.BOTHWAY_REQUEST_FLAG)) {
                // 为双向通讯请求msg
                bothwayId = Arrays.copyOfRange(data, WTBothway.BOTHWAY_REQUEST_FLAG.length, WTBothway.BOTHWAY_REQUEST_FLAG.length + 8);
                //分离出真实data
                data = Arrays.copyOfRange(data, WTBothway.BOTHWAY_REQUEST_FLAG.length + 8, data.length);
            } else if (Arrays.equals(prefix, WTBothway.BOTHWAY_REPLY_FLAG)) {
                //为双向通讯响应msg，交给WTResponseMsgListener处理。
                return;
            }
            messageListener.onMessageReceived(messageEvent.getSourceNodeId(),
                    messageEvent.getPath(), data, bothwayId);
        }
    }

    private MyHandler handler = new MyHandler(this);

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Message msg = handler.obtainMessage(0);
        msg.obj = messageEvent;
        handler.sendMessage(msg);
    }

    /**
     * @param nodeId    发送此msg的节点id
     * @param path      path
     * @param data      发送的数据
     * @param bothwayId 双向通讯id，若非双向则为null
     */
    @UiThread
    public abstract void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId);
}
