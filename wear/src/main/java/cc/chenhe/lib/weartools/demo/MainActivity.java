package cc.chenhe.lib.weartools.demo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataMap;

import java.io.InputStream;

import cc.chenhe.lib.weartools.AssetHelper;
import cc.chenhe.lib.weartools.WTBothway;
import cc.chenhe.lib.weartools.WTUtils;
import cc.chenhe.lib.weartools.activity.WTActivity;

public class MainActivity extends WTActivity {
    private static final String TAG = "MainAty";

    private Context context;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        findViewById(R.id.btn_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestForImage();
            }
        });

        WTUtils.setDebug(true);
        addMessageListener();
        addDataListener();
    }

    private void requestForImage() {
        Toast.makeText(context, "Requesting...", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Sending request for image...");
        WTBothway.request(context, "/image", "11", new WTBothway.BothwayCallback4DataMap() {
            @Override
            public void onRespond(DataMap data) {
                Log.i(TAG, "Receive reply image.");
                Asset asset = data.getAsset("image");
                AssetHelper.get(context, asset, new AssetHelper.AssetCallback() {
                    @Override
                    public void onResult(InputStream ins) {
                        imageView.setImageBitmap(BitmapFactory.decodeStream(ins));
                    }
                });
            }

            @Override
            public void onFailed(int resultCode) {
                Toast.makeText(context, "No image reply.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Request for image failed: " + resultCode);
            }
        });
    }

    @Override
    protected void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
        super.onMessageReceived(nodeId, path, data, bothwayId);

        Toast.makeText(context, new String(data), Toast.LENGTH_SHORT).show();
        if (bothwayId == null) {
            Log.i(TAG, "Receive msg: " + new String(data));
        } else {
            Log.i(TAG, "Receive bothway request msg: " + new String(data));
            //send response message.
            WTBothway.response(context, nodeId, path, bothwayId, "reply for " + new String(data), null);
        }
    }

    @Override
    protected void onDataChanged(String path, DataMap dataMap) {
        super.onDataChanged(path, dataMap);

        if (path.equals("/image")) {
            Log.i(TAG, "Receive image");
            Asset asset = dataMap.getAsset("image");
            AssetHelper.get(context, asset, new AssetHelper.AssetCallback() {
                @Override
                public void onResult(InputStream ins) {
                    imageView.setImageBitmap(BitmapFactory.decodeStream(ins));
                }
            });
            return;
        }

        String s = dataMap.getString("test");
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Receive data: " + s);
    }

    @Override
    protected void onDataDeleted(String path) {
        super.onDataDeleted(path);
        Toast.makeText(context, path + " deleted.", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Del data: " + path);
    }
}
