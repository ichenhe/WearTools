package cc.chenhe.lib.weartools.listener;

import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.DataItem;
import com.mobvoi.android.wearable.DataMap;
import com.mobvoi.android.wearable.DataMapItem;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.WearableListenerService;

import java.util.Arrays;

import cc.chenhe.lib.weartools.WTBothway;


/**
 * Created by 晨鹤 on 2016/11/27.
 * 继承并封装WearableListenerService，增加对双向通讯的支持。
 */

public abstract class WTListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

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
        onMessageReceived(messageEvent.getSourceNodeId(), messageEvent.getPath(),
                data, bothwayId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                byte[] bothwayType = DataMapItem.fromDataItem(item).getDataMap()
                        .getByteArray(WTBothway.BOTHWAY_TYPE_KEY);
                if (bothwayType != null && Arrays.equals(bothwayType, WTBothway.BOTHWAY_REPLY_FLAG)) {
                    //为双向通讯响应data，交给WTResponseDataListener处理。
                    return;
                }
                onDataChanged(item.getUri().getPath(), DataMapItem.fromDataItem(item).getDataMap());
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                onDataDeleted(event.getDataItem().getUri().getPath());
            }
        }

    }

    /**
     * @param nodeId    发送此msg的节点id
     * @param path      path
     * @param data      发送的数据
     * @param bothwayId 双向通讯id，若非双向则为null
     */
    public abstract void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId);

    public abstract void onDataChanged(String path, DataMap dataMap);

    public abstract void onDataDeleted(String path);
}
