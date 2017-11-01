package cc.chenhe.lib.weartools.demo;

import android.app.Application;

import com.mobvoi.android.common.NoAvailableServiceException;

import cc.chenhe.lib.weartools.WTUtils;

/**
 * Created by 晨鹤 on 2017/10/18.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            WTUtils.init(this,WTUtils.MODE_AUTO);
        } catch (NoAvailableServiceException e) {
            e.printStackTrace();
        }
    }
}
