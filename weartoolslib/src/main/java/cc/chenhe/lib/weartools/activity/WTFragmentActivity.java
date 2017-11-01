package cc.chenhe.lib.weartools.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.mobvoi.android.wearable.DataMap;

import java.lang.ref.WeakReference;

import cc.chenhe.lib.weartools.WTRegister;
import cc.chenhe.lib.weartools.listener.WTDataListener;
import cc.chenhe.lib.weartools.listener.WTMessageListener;

/**
 * Created by 晨鹤 on 2017/10/31.
 */

public class WTFragmentActivity extends FragmentActivity {
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
        private WeakReference<WTFragmentActivity> mActivityWeakReference;

        public MyMessageListener(@NonNull WTFragmentActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
            WTFragmentActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onMessageReceived(nodeId, path, data, bothwayId);
        }
    }

    private static class MyDataListener extends WTDataListener {
        private WeakReference<WTFragmentActivity> mActivityWeakReference;

        public MyDataListener(@NonNull WTFragmentActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onDataChanged(String path, DataMap dataMap) {
            WTFragmentActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onDataChanged(path, dataMap);
        }

        @Override
        public void onDataDeleted(String path) {
            WTFragmentActivity activity = mActivityWeakReference.get();
            if (activity == null) return;
            activity.onDataDeleted(path);
        }
    }
}
