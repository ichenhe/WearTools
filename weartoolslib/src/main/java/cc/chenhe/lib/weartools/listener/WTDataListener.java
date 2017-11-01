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
 * Created by 晨鹤 on 2016/11/25.
 * 用于监听data变化。
 * 封装了官方的DataListener,增加双向通讯判断。
 */

public abstract class WTDataListener implements DataApi.DataListener {

    private static class MyHandler extends Handler {
        private WeakReference<WTDataListener> mDataListenerWeakReference;

        public MyHandler(WTDataListener dataListener) {
            mDataListenerWeakReference = new WeakReference<>(dataListener);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WTDataListener dataListener = mDataListenerWeakReference.get();
            if (dataListener == null) return;

            if (msg.what == DataEvent.TYPE_CHANGED) {
                DataItem item = (DataItem) msg.obj;

                byte[] bothwayType = DataMapItem.fromDataItem(item).getDataMap()
                        .getByteArray(WTBothway.BOTHWAY_TYPE_KEY);
                if (bothwayType != null && Arrays.equals(bothwayType, WTBothway.BOTHWAY_REPLY_FLAG)) {
                    //为双向通讯响应data，交给WTResponseDataListener处理。
                    return;
                }
                dataListener.onDataChanged(item.getUri().getPath(), DataMapItem.fromDataItem(item).getDataMap());
            } else if (msg.what == DataEvent.TYPE_DELETED) {
                dataListener.onDataDeleted((String) msg.obj);
            }
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Message msg = handler.obtainMessage();
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                msg.what = DataEvent.TYPE_CHANGED;
                msg.obj = event.getDataItem();
                handler.sendMessage(msg);
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                msg.what = DataEvent.TYPE_DELETED;
                msg.obj = event.getDataItem().getUri().getPath();
                handler.sendMessage(msg);
            }
        }
    }

    private MyHandler handler = new MyHandler(this);

    @UiThread
    public abstract void onDataChanged(String path, DataMap dataMap);

    @UiThread
    public abstract void onDataDeleted(String path);

}
