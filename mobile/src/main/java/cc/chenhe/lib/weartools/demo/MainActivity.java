package cc.chenhe.lib.weartools.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.PutDataMapRequest;

import java.io.IOException;
import java.io.InputStream;

import cc.chenhe.lib.weartools.WTBothway;
import cc.chenhe.lib.weartools.WTSender;
import cc.chenhe.lib.weartools.WTUtils;
import cc.chenhe.lib.weartools.activity.WTAppCompatActivity;

public class MainActivity extends WTAppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainAty";

    private TextInputLayout tilMsg, tilData;
    private Context context;

    private LongSparseArray<String> t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WTUtils.setDebug(true);
        context = this;
        initView();

        addMessageListener();
    }

    @Override
    protected void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
        super.onMessageReceived(nodeId, path, data, bothwayId);
        if (path.equals("/image")){
            Log.i(TAG,"Receive image request.");
        }
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        tilMsg = findViewById(R.id.til_msg);
        tilData = findViewById(R.id.til_data);

        findViewById(R.id.btn_send_msg).setOnClickListener(this);
        findViewById(R.id.btn_send_data).setOnClickListener(this);
        findViewById(R.id.btn_del_data).setOnClickListener(this);
        findViewById(R.id.btn_send_msg_both_way).setOnClickListener(this);
        findViewById(R.id.btn_send_image).setOnClickListener(this);
    }

    private Uri mDataUri;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_msg:
                sendMsg();
                break;
            case R.id.btn_send_data:
                sendData();
                break;
            case R.id.btn_del_data:
                delData();
                break;
            case R.id.btn_send_msg_both_way:
                sendMsgBothway();
                break;
            case R.id.btn_send_image:
                sendImage();
                break;
        }
    }

    private void sendMsg() {
        if (TextUtils.isEmpty(tilMsg.getEditText().getText())) return;
        Log.i(TAG, "Sending msg...");
        WTSender.sendMessage(this, "/msg/test", tilMsg.getEditText().getText().toString(), new WTSender.SendMsgCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Send msg OK", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Send msg OK");
            }

            @Override
            public void onFailed(int resultCode) {
                Toast.makeText(context, "Send msg failed: " + resultCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Send msg failed");
            }
        });
    }

    private void sendData() {
        if (TextUtils.isEmpty(tilData.getEditText().getText())) return;
        Log.i(TAG, "Sending data...");
        PutDataMapRequest putDataMapRequest = WTSender.getPutDataMapRequest("/data/test");
        putDataMapRequest.getDataMap().putString("test",
                tilData.getEditText().getText().toString());
        putDataMapRequest.setUrgent();
        mDataUri = putDataMapRequest.getUri();
        WTSender.sendData(context, putDataMapRequest, new WTSender.SendDataCallback() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(context, "Send data OK", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Send data OK");
            }

            @Override
            public void onFailed(int resultCode) {
                Toast.makeText(context, "Send data failed: " + resultCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Send data failed");
            }
        });
    }

    private void delData() {
        if (mDataUri == null) return;
        WTSender.deleteData(context, mDataUri, new WTSender.DeleteDataCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Del data OK", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Del data OK");
            }

            @Override
            public void onFailed(int resultCode) {
                Toast.makeText(context, "Del data failed: " + resultCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Del data failed");
            }
        });
    }

    private void sendMsgBothway() {
        if (TextUtils.isEmpty(tilMsg.getEditText().getText())) return;
        WTBothway.request(context, "/bothway/msg/test", tilMsg.getEditText().getText().toString(), new WTBothway.BothwayCallback() {
            @Override
            public void onRespond(byte[] data) {
                Toast.makeText(context, "Rec reply: " + new String(data), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Receive reply: " + new String(data));
            }

            @Override
            public void onFailed(int resultCode) {
                Toast.makeText(context, "Bothway msg failed: " + resultCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Bothway msg failed: " + resultCode);
            }
        });
    }

    private void sendImage() {
        try {
            InputStream in = getResources().getAssets().open("image1.webp");
            int len = in.available();
            byte[] buffer = new byte[len];
            in.read(buffer);
            in.close();

            Asset asset = Asset.createFromBytes(buffer);
            PutDataMapRequest putDataMapRequest = WTSender.getPutDataMapRequest("/image");
            putDataMapRequest.setUrgent();
            putDataMapRequest.getDataMap().putAsset("image", asset);
            Log.i(TAG, "Sending image data...");
            WTSender.sendData(context, putDataMapRequest, new WTSender.SendDataCallback() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.i(TAG, "Send image OK");
                }

                @Override
                public void onFailed(int resultCode) {
                    Log.e(TAG, "Send image failed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
