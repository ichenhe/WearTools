package cc.chenhe.lib.weartools;

import android.content.Context;

import com.mobvoi.android.common.MobvoiApiManager;
import com.mobvoi.android.common.NoAvailableServiceException;

/**
 * Created by 晨鹤 on 2016/11/23.
 * 工具类。
 */

public class WTUtils {
    private static final String TAG = "WTUtils";

    //兼容模式
    public static final int MODE_GMS = 1;
    public static final int MODE_MMS = 2;
    public static final int MODE_AUTO = 0;

    private static long timeOut = 3000;

    private static long bothwayTimeOut = 10000;

    private static boolean isDebug = false;

    /**
     * 初始化，建议在Application onCreate中调用。
     *
     * @param context c
     * @param mode    通讯API兼容模式。MODE_开头常量
     * @throws NoAvailableServiceException 初始化失败
     */
    public static void init(Context context, int mode) throws NoAvailableServiceException {
        if (!MobvoiApiManager.getInstance().isInitialized())
            switch (mode) {
                case MODE_AUTO:
                    MobvoiApiManager.getInstance().adaptService(context.getApplicationContext());
                    break;
                case MODE_GMS:
                    MobvoiApiManager.getInstance().loadService(context.getApplicationContext(),
                            MobvoiApiManager.ApiGroup.GMS);
                    break;
                case MODE_MMS:
                    MobvoiApiManager.getInstance().loadService(context.getApplicationContext(),
                            MobvoiApiManager.ApiGroup.MMS);
                    break;
            }

        MobvoiApiManager.ApiGroup group = MobvoiApiManager.getInstance().getGroup();
        if (group.equals(MobvoiApiManager.ApiGroup.GMS))
            WTLog.d(TAG, "GMS mode: Google Service");
        else if (group.equals(MobvoiApiManager.ApiGroup.MMS))
            WTLog.d(TAG, "MMS mode: Ticwear");
        else if (group.equals(MobvoiApiManager.ApiGroup.NONE))
            WTLog.d(TAG, "NONE mode: not init yet.");
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static long getBothwayTimeOut() {
        return bothwayTimeOut;
    }

    public static void setBothwayTimeOut(long bothwayTimeOut) {
        WTUtils.bothwayTimeOut = bothwayTimeOut;
    }

    public static void setTimeOut(int timeout) {
        WTUtils.timeOut = timeout;
    }

    public static long getTimeOut() {
        return timeOut;
    }
}
