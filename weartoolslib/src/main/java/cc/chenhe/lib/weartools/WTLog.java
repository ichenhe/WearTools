package cc.chenhe.lib.weartools;

import android.util.Log;

/**
 * Created by 晨鹤 on 2016/11/26.
 * 输出log工具类。
 */

public class WTLog {

    protected static void v(String tag, String msg) {
        if (WTUtils.isDebug()) Log.v(tag, msg);
    }

    protected static void d(String tag, String msg) {
        if (WTUtils.isDebug()) Log.d(tag, msg);
    }

    protected static void i(String tag, String msg) {
        if (WTUtils.isDebug()) Log.i(tag, msg);
    }

    protected static void w(String tag, String msg) {
        if (WTUtils.isDebug()) Log.w(tag, msg);
    }

    protected static void e(String tag, String msg) {
        if (WTUtils.isDebug()) Log.e(tag, msg);
    }

}
