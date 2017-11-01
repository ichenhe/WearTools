package cc.chenhe.lib.weartools.listener;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;

import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.DataItem;
import com.mobvoi.android.wearable.DataMap;
import com.mobvoi.android.wearable.DataMapItem;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import cc.chenhe.lib.weartools.WTBothway;

/**
 * Created by 晨鹤 on 2016/12/17.
 * 监听响应双向通讯的响应data.
 */

public abstract class WTResponseDataListener implements DataApi.DataListener {

    private static class MyHandler extends Handler {
        private WeakReference<WTResponseDataListener> mDataListenerWeakReference;

        public MyHandler(WTResponseDataListener dataListener) {
            mDataListenerWeakReference = new WeakReference<>(dataListener);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WTResponseDataListener dataListener = mDataListenerWeakReference.get();
            if (dataListener == null) return;

            if (msg.what != DataEvent.TYPE_CHANGED) return;
            DataItem item = (DataItem) msg.obj;
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            byte[] bothwayType = dataMap.getByteArray(WTBothway.BOTHWAY_TYPE_KEY);

            if (bothwayType == null || !Arrays.equals(bothwayType, WTBothway.BOTHWAY_REPLY_FLAG)) {
                //不为双向通讯响应data，交给WTDataListener处理。
                return;
            }
            dataListener.onResponseDataChanged(dataMap.getByteArray(WTBothway.BOTHWAY_ID_KEY),
                    item.getUri().getPath(), dataMap);
        }
    }

    private MyHandler handler = new MyHandler(this);

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Message msg = handler.obtainMessage();
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                msg.what = DataEvent.TYPE_CHANGED;
                msg.obj = item;
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * @param bothwayId 带有bothway特征与id的path
     * @param path      发送时传入的真实path
     * @param dataMap   数据
     */
    @UiThread
    public abstract void onResponseDataChanged(byte[] bothwayId, String path, DataMap dataMap);
}
