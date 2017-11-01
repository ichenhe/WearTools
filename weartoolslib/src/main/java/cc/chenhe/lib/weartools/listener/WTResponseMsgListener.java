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
 * 监听响应双向通讯的响应msg.
 */

public abstract class WTResponseMsgListener implements MessageApi.MessageListener {

    private static class MyHandler extends Handler {
        private WeakReference<WTResponseMsgListener> mMsgListenerWeakReference;

        public MyHandler(WTResponseMsgListener msgListener) {
            mMsgListenerWeakReference = new WeakReference<>(msgListener);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WTResponseMsgListener msgListener = mMsgListenerWeakReference.get();
            if (msgListener == null) return;

            MessageEvent messageEvent = (MessageEvent) msg.obj;
            byte[] data = messageEvent.getData();
            byte[] bothwayId = null;

            byte[] prefix = Arrays.copyOf(data, WTBothway.BOTHWAY_REPLY_FLAG.length);
            if (!Arrays.equals(prefix, WTBothway.BOTHWAY_REPLY_FLAG)) {
                //不是双向通讯响应msg，交给WTMessageListener处理。
                return;
            }
            bothwayId = Arrays.copyOfRange(data, WTBothway.BOTHWAY_REPLY_FLAG.length,
                    WTBothway.BOTHWAY_REPLY_FLAG.length + 8);
            //分离出真实data
            data = Arrays.copyOfRange(data, WTBothway.BOTHWAY_REPLY_FLAG.length + 8, data.length);
            //调用回调
            msgListener.onResponseMsgReceived(messageEvent.getPath(), data, bothwayId);
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
     * @param path      发送时传入的真实path
     * @param data      发送的数据
     * @param bothwayId 带有bothway特征与id的path
     */
    @UiThread
    public abstract void onResponseMsgReceived(String path, byte[] data, byte[] bothwayId);
}
