package cc.chenhe.lib.weartools.bean;

import com.mobvoi.android.wearable.DataMap;

/**
 * Created by 晨鹤 on 2016/12/17.
 */

public class ResponseDataBean {
    public DataMap dataMap;
    public String path;//请求时传入的真实path

    public ResponseDataBean( DataMap dataMap, String path) {
        this.dataMap = dataMap;
        this.path = path;
    }
}
