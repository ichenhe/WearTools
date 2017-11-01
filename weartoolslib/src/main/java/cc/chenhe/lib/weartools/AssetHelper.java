package cc.chenhe.lib.weartools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.Wearable;

import java.io.InputStream;

/**
 * Created by 晨鹤 on 2017/10/31.
 * 辅助在接收端获取DataMap的Asset。
 */

public class AssetHelper {

    public interface AssetCallback {
        /**
         * @param ins 提取出的数据。失败为null。
         */
        void onResult(InputStream ins);
    }

    /**
     * 异步提取Asset中的数据。失败则在Callback中返回null。
     *
     * @param context  c
     * @param asset    要提取的Asset
     * @param callback 回调
     */
    public static void get(@NonNull Context context, @NonNull final Asset asset,
                           @NonNull final AssetCallback callback) {
        WTApiClientManager.getInstance().getClient(context, new WTApiClientManager
                .GetClientCallback() {
            @Override
            public void onConnected(MobvoiApiClient client) {
                Wearable.DataApi.getFdForAsset(client, asset)
                        .setResultCallback(new ResultCallback<DataApi.GetFdForAssetResult>() {
                            @Override
                            public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                                callback.onResult(getFdForAssetResult.getInputStream());
                            }
                        });
            }

            @Override
            public void onConnectionFailed(@Nullable MobvoiApiClient client, @Nullable
                    ConnectionResult connectionResult) {
                callback.onResult(null);
            }
        });
    }
}
