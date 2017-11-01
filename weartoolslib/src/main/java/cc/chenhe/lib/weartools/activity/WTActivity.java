package cc.chenhe.lib.weartools.activity;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.mobvoi.android.wearable.DataMap;

import java.lang.ref.WeakReference;

import cc.chenhe.lib.weartools.WTRegister;
import cc.chenhe.lib.weartools.listener.WTDataListener;
import cc.chenhe.lib.weartools.listener.WTMessageListener;

/**
 * Created by 晨鹤 on 2016/12/12.
 */

public class WTActivity extends Activity {
    private MyMessageListener messageListener;
    private MyDataListener dataListener;

    protected void addMessageListener() {
        messageListener = new MyMessageListener(this);
        WTRegister.addMessageListener(this, messageListener);
    }

    protected void addDataListener() {
        dataListener = new MyDataListener(this);
        WTRegister.addDataListener(this, dataListener);
    }

    @Override
    protected void onResume() {
        if (messageListener != null)
            WTRegister.addMessageListener(this, messageListener);
        if (dataListener != null)
            WTRegister.addDataListener(this, dataListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (messageListener != null)
            WTRegister.removeMessageListener(this, messageListener);
        if (dataListener != null)
            WTRegister.removeDataListener(this, dataListener);
        super.onPause();
    }

    protected void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
    }

    protected void onDataChanged(String path, DataMap dataMap) {
    }

    protected void onDataDeleted(String path) {

    }

    private static class MyMessageListener extends WTMessageListener {
        private WeakReference<WTActivity> mActivityWeakReference;

        public MyMessageListener(@NonNull WTActivity activity) {
            mActivityWeakReference = new WeakReference<WTActivity>(activity);
        }

        @Override
        public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
            WTActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onMessageReceived(nodeId, path, data, bothwayId);
        }
    }

    private static class MyDataListener extends WTDataListener {
        private WeakReference<WTActivity> mActivityWeakReference;

        public MyDataListener(@NonNull WTActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onDataChanged(String path, DataMap dataMap) {
            WTActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onDataChanged(path, dataMap);
        }

        @Override
        public void onDataDeleted(String path) {
            WTActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onDataDeleted(path);
        }
    }
}
