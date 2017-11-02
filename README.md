# 概述

WearTools 二次封装了[Ticear提供的sdk](https://bintray.com/ticwear/maven/mobvoi-api)，同时内部包含了`Google Play services 10.2.0`，以便实现 Android Wear、Android Wear China、Ticwear 的全兼容。下载量超10万的[腕间图库](http://wg.chenhe.cc/)已经使用老版本的 WearTools 库一年。
弱弱地求个Star★(*￣3￣)╭ 

使用 WearTools ，可以大幅简化手表与手机的通讯代码，你只需关注业务逻辑而不必将大量精力放在传输的维护上。本库提供了不同系统不同设备下统一的API，手表与手机可以互为发送方与接收方并且不需要编写不同的代码。

**添加依赖** ：
`compile 'cc.chenhe:wear-tools:1.0.0'`



# 特性

- 兼容多系统。（Andorid Wear、AW China、Ticwear）
- 提供全局静态函数便于发送。
- 回调与调用在同一线程。
- 支持 Request/Response 模型。
- 支持超时返回。



# 初始化

建议在Application的onCreate方法中进行初始化。如下。

```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

      	//初始化WearTools,自动适配系统。
        try {
            WTUtils.init(this,WTUtils.MODE_AUTO);
        } catch (NoAvailableServiceException e) {
            e.printStackTrace();
        }
    }
}
```

记得在manifest中注册Application。如下。

```xml
...
<application
        android:name="cc.chenhe.lib.weartools.demo.App"
		...
</application>
...
```

**注意：** 不建议在手机终端使用自动适配系统。因为此模式下，sdk会判断当前手机设备是否安装了`Ticwear`App，如果安装了则认为是Ticwear模式，而不是依据实际连接情况进行判断。假设这样一种情况：用户同时安装了`Ticwear`与`Android Wear`，但当前连接的是 Android Wear 手表。那么sdk就会判断错误，导致相关功能不可用。
**最佳实践** 是根据目标用户，手动指定系统或者引导用户自行选择。

**注意：** ApiClient 将由 WearTools 统一管理，你不应当尝试手动创建、连接或释放。



# 发送数据

在使用此api之前，建议先阅读[谷歌文档](https://developer.android.com/training/wearables/data-layer/index.html)了解 Message 和 DataItem 的区别以及适用场景。



## 发送Message

你可以在任何地方任何线程调用`WTSender.sendMessage() `函数来发送 Message.
你可以选择传入一个`SendMsgCallback`在取得发送的结果。
下面是一个简单的发送Demo:

```java
String path = "/msg/test";
String content = "This is a Message";
WTSender.sendMessage(context, path, content, new WTSender.SendMsgCallback() {
		@Override
		public void onSuccess() {
			Log.i(TAG, "Send msg OK");
		}

		@Override
		public void onFailed(int resultCode) {
			Log.e(TAG, "Send msg failed");
		}
});
```

**注意：** 回调中的成功指的是成功将请求发送给 Google Play Service 或 Mobvoi API ，这不代表对端设备一定可以收到数据。



## 发送DataItem(DataMap)

由于 DataMap 比 DataItem 更加易用，WearTools 将只支持发送结构化的 DataMap，其完全可以取代 DataItem.

同样的，你可以在任何地方任何线程调用`WTSender.sendData() `函数来发送 DataMap. 不过在此之前应先调用`WTSender.getPutDataMapRequest()`来获得 PutDataMapRequest ，并向其添加数据。
下面是一个简单的发送Demo:

```java
String path = "/data/test";
String conetnt = "This is a dataMap."
PutDataMapRequest putDataMapRequest = WTSender.getPutDataMapRequest(path);
putDataMapRequest.getDataMap().putString("test",conetnt);
//要求系统立即同步此DataMap，否则系统会在适当的时候同步以便节约资源消耗。
putDataMapRequest.setUrgent();
//发送
WTSender.sendData(context, putDataMapRequest, new WTSender.SendDataCallback() {
	@Override
	public void onSuccess(Uri uri) {
		Log.i(TAG, "Send data OK");
	}

	@Override
	public void onFailed(int resultCode) {
		Log.e(TAG, "Send data failed");
	}
});
```

**注意：** 与发送 Message 一样，回调中的成功指的是成功将请求发送给 Google Play Service 或 Mobvoi API ，这不代表对端设备一定可以收到数据。



## 删除DataItem(DataMap)

因为 DataItem(DataMap) 设计上是用来在手机与手表间同步数据，因此，也提供了删除方法。

你可以在任何地方任何线程调用`WTSender.deleteData() `函数来删除 DataMap. 不过这要求你在发送 DataMap 时存下了它的 Uri ，可以使用`putDataMapRequest.getUri()`获得。
下面是一个简单的删除Demo:

```java
Uri mDataUri;
WTSender.deleteData(context, mDataUri, new WTSender.DeleteDataCallback() {
	@Override
  	public void onSuccess() {
		Log.i(TAG, "Del data OK");
	}

	@Override
	public void onFailed(int resultCode) {
		Log.e(TAG, "Del data failed");
	}
});
```



## 发送文件

DataMap 限制大小为100KB，如果要传输一个图片等大数据，则需要通过 Asset ，Asset 又需要与 DataMap 绑定。
下面是一个发送图片的Demo:

```java
try {
  	//从assets中读取图片
	InputStream in = getResources().getAssets().open("image1.webp");
	int len = in.available();
	byte[] buffer = new byte[len];
	in.read(buffer);
	in.close();

	Asset asset = Asset.createFromBytes(buffer); //创建Asset
	PutDataMapRequest putDataMapRequest = WTSender.getPutDataMapRequest("/image");
	putDataMapRequest.setUrgent(); //要求系统立即传输
	putDataMapRequest.getDataMap().putAsset("image", asset); //将Asset绑定到DataMap
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
```

**注意：** 虽然理论上Asset是不限制大小的，但是受限于蓝牙带宽，发送大文件将耗费很长的时间，在这过程中还可能影响其他数据的传输，因此请慎重传输超大文件。

# 通过监听器接收数据

WearTools 也提供了各式各样的监听器，你可以在任何地方注册它们。回调函数将运行在与监听器实例化的同一线程，因此，你可以方便地在回调中更新UI.

**警告！！ 不论何种监听器，如果要在 Activity 中实例化，请务必特别处理以免造成内存泄漏。详情请参加下面的内存泄漏章节**



## 监听Message

实例化并注册`WTMessageListener`即可实现对Message的监听。
下面是一个简单的Demo:

```java
WTMessageListener messageListener = new WTMessageListener() {
	@Override
	public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
            Log.i(TAG, "Receive msg: " + new String(data));
	}
};
WTRegister.addMessageListener(context, messageListener); //注册

//WTRegister.removeMessageListener(context, messageListener); //移除
```



## 监听DataMap

类似地，实例化并注册`WTDataListener`即可实现对DataMap的监听。
下面是一个简单的Demo:

```java
WTDataListener dataListener = new WTDataListener() {
	@Override
	public void onDataChanged(String path, DataMap dataMap) {
      	//此回调对应 WTSender.sendData()
		Log.i(TAG, "Receive data: " + dataMap.getString("test"));
	}

	@Override
	public void onDataDeleted(String path) {
      	//此回调对应 WTSender.deleteData()
		Log.i(TAG, "Data deleted: " + path);
	}
};
WTRegister.addDataListener(context, dataListener); //注册

//WTRegister.removeDataListener(context, dataListener); //移除
```

## 从 DataMap 中取出 Asset 数据

上面提到，可以利用 Asset 传输较大的文件。 WearTools 提供了`AssetHelper.get()`来方便地取出数据。
下面是一个Demo展示如何取出上文发送的图片并显示：

```java
@Override
public void onDataChanged(String path, DataMap dataMap) {
	super.onDataChanged(path, dataMap);
	
  	Log.i(TAG, "Receive image");
	Asset asset = dataMap.getAsset("image");
	AssetHelper.get(context, asset, new AssetHelper.AssetCallback() {
		@Override
		public void onResult(InputStream ins) {
			imageView.setImageBitmap(BitmapFactory.decodeStream(ins));
		}
	});
}
```



# 通过 Service 接收数据

有时我们仅仅需要默默地接收数据并处理，而不需要UI交互。此时便适用于使用 Service 来监听。



## 创建Service

首先请创建一个类，并继承自`WTListenerService`，此 Servie 的生命周期将由系统自动管理，你不需要手动启动或者停止服务。这些定义好的抽象方法与上述监听器类似，将在收到数据的时候被自动调用。
下面是一个简单的 Service Demo:

```java
public class ListenerService extends WTListenerService {
    @Override
    public void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
        
    }

    @Override
    public void onDataChanged(String path, DataMap dataMap) {

    }

    @Override
    public void onDataDeleted(String path) {

    }
}
```



## 注册Service

之后记得在manifest里注册Service.
值得注意的是，必须指定`<intent-filter>`，里面必须指定`action`与`data`，否则服务将不会被启动，当中的函数也不会被执行。
有关`<intent-filter>`的详细资料，请参加[谷歌文档](https://developer.android.com/training/wearables/data-layer/events.html) 。

**注意：所有的** `com.google.android.gms.wearable` **前缀需要替换为** `com.mobvoi.android.wearable`

下面是一个注册了上述几个发送监听的Demo:

```xml
<service android:name="cc.chenhe.lib.weartools.demo.ListenerService">
	<intent-filter>
		<action android:name="com.mobvoi.android.wearable.MESSAGE_RECEIVED" />
		<action android:name="com.mobvoi.android.wearable.DATA_CHANGED" />
		<data
			android:host="*"
			android:path="/image"
			android:scheme="wear" />
		<data
			android:host="*"
			android:path="/data/test"
			android:scheme="wear" />
		<data
			android:host="*"
			android:path="/msg/test"
			 android:scheme="wear" />
	</intent-filter>
</service>
```

**注意：** 曾经的`BIND_LISTENER` action 已经不再支持，注册`BIND_LISTENER`不会收到任何回调。



# 双向通讯 (Request / Response)

很多时候，我们需要类似Http的请求/响应模型来传输数据，或者来确认数据是否已经收到。同时需要有超时判断，当对端一定时间内没有响应时给出反馈。WearTools 很贴心地实现了这种需求。
双向通讯的有关函数与类均在`WTBothway`中。

你需要了解，WTBothway 中的 Request 本质上是发送一个 Message ，而 Response 可以通过实际需求，选择 Message 或 DataMap 进行响应。

## 发送请求

通过`WTBothway.request()`可以发出请求，并且根据期望的响应类型（Message或DataMap）的不同，你应当传入相应的回调。一个回调只能监听到与之相匹配的类型的响应。

下面是一个简单的期望得到Message响应的请求Demo:

```java
String path = "/bothway/msg/test";
String content = "request test";
WTBothway.request(context, path, content, new WTBothway.BothwayCallback() {
	@Override
	public void onRespond(byte[] data) {
		Log.i(TAG, "Receive reply: " + new String(data));
	}

	@Override
	public void onFailed(int resultCode) {
		Log.e(TAG, "Bothway msg failed: " + resultCode);
		//通过判断resultCode可以得知是发送失败还是响应超时。
	}
});
```



## 响应请求

普通的`WTMessageListener`或者 Servie 都可以监听到 Bothway 的请求。你可以通过判断参数`bothwayId`是否为null确定当前Message是一个普通的数据还是一个 Bothway 的请求。
如果是一个 Bothway 的请求，你需要尽快予以 Message 或 DataMap 形式的响应。若响应时间太长，请求方会认为超时，之后收到的响应数据会被直接丢弃。

下面是一个以 Message 形式响应请求的Demo:

```java
@Override
protected void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) {
	super.onMessageReceived(nodeId, path, data, bothwayId);
	if (bothwayId != null) { //判断当前是否是一个Bothway的请求
		Log.i(TAG, "Receive bothway request msg: " + new String(data));
		// 发送响应。
		WTBothway.response(context, nodeId, path, bothwayId, "reply for " + new String(data), null);
	}
}
```

**注意：** `bothwayId`必须原封不动地传给response函数，如果经过修改，那么发送方不会收到回调。虽然`path`可以不同，但按照规范也考虑到后续版本兼容性，请务必也原封不动地传给response().

以 DataMap形式响应请求是大致相同的，不过需要调用`WTBothway.responseDataItem()`函数来实现。
**注意：** 响应的DataMap务必设置`putDataMapRequest.setUrgent();`，否则极容易导致超时。



#　内存泄漏

由于未知的原因，所有的监听器(Listener)短时间内无法被释放，即使已经被remove甚至置null也无济于事。根据内存分析，目前初步判断是 Ticwear SDK 内部原因。此问题已经与 Ticwear 公司技术人员沟通，但截至目前尚无结论。如果你在 Activity 中new了 Listener ，那么由于java内部类对象会持有外部类引用，Activity 便无法被及时回收，遂发生内存泄漏。

目前提供2个解决方案。



## 继承 WearTools 提供的 Activity

推荐使用此方案。

WearTools 提供了三种 Activity ，分别是`WTActivity` `WTAppCompatActivity` `WTFragmentActivity`，他们分别继承了`Activity` `AppCompatActivity` `FragmentActivity`。

这三个类内部实现了可以避免内存泄漏的Listener，你可以通过 Activity 自身提供的`addMessageListener()`与`addDataListener()`函数启用内置监听器，并且重写`onMessageReceived()` `onDataChanged()` `onDataDeleted()`这三个方法来获得回调。默认情况下，会在`onPause()`中注销监听器并在`onResume()`中重新注册（如果已经启用的话）。

下面是一个Demo展示如何继承WTActivity并启用监听：

```java
public class MainActivity extends WTActivity {
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addMessageListener(); //启用msg监听
        addDataListener(); //启用data监听
    }

    @Override
    protected void onMessageReceived(String nodeId, String path, byte[] data, byte[] bothwayId) 	{
		super.onMessageReceived(nodeId, path, data, bothwayId);
		Log.i(TAG, "Receive msg: " + new String(data));
    }

    @Override
    protected void onDataChanged(String path, DataMap dataMap) {
        super.onDataChanged(path, dataMap);
        Log.i(TAG, "Receive data: " + s);
    }

    @Override
    protected void onDataDeleted(String path) {
        super.onDataDeleted(path);
        Log.i(TAG, "Del data: " + path);
    }
}
```



## 自定义静态类继承Listener

不推荐此方案。

其实上述 WearTools 提供的三种 Activity 内部就是使用了此方案，因此，直接继承他们是更加方便快捷的做法。如果满足不了需求，你也可以自己来定义静态类继承Listener。

此方案与 Handler 引发内存泄漏解决方案一样。大概就是将 Listener 定义为静态类，如此一来便不再持有外部类的引用。为了在回调中操作外部类的变量，可以添加一个外部类的若引用。

下面是一个此方案的Demo:

```java
public class MainActivity extends Activity {
	private MyMessageListener messageListener;
	private MyDataListener dataListener;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		messageListener = new MyMessageListener(this);
		dataListener = new MyDataListener(this);
	}
  
	@Override
	protected void onResume() {
        super.onResume();
		WTRegister.addMessageListener(this, messageListener);
		WTRegister.addDataListener(this, dataListener);
    }
  
	@Override
	protected void onPause() {
        if (messageListener != null)
            WTRegister.removeMessageListener(this, messageListener);
        if (dataListener != null)
            WTRegister.removeDataListener(this, dataListener);
        super.onPause();
	}

	//定义 WTMessageListener 的静态类
	private static class MyMessageListener extends WTMessageListener {
		private WeakReference<WTActivity> mActivityWeakReference;

		public MyMessageListener(@NonNull WTActivity activity) {
			mActivityWeakReference = new WeakReference<WTActivity>(activity);
		}

		@Override
		public void onMessageReceived(String nodeId, String path, byte[] data, byte[] 		 bothwayId) {
			WTActivity activity = mActivityWeakReference.get();
			if (activity == null) return;
         	// Do something here.
		}
	}

	//定义 WTDataListener 的静态类
	private static class MyDataListener extends WTDataListener {
		private WeakReference<WTActivity> mActivityWeakReference;

		public MyDataListener(@NonNull WTActivity activity) {
			mActivityWeakReference = new WeakReference<>(activity);
		}

		@Override
		public void onDataChanged(String path, DataMap dataMap) {
			WTActivity activity = mActivityWeakReference.get();
			if (activity == null) return;
			// Do something here.
		}

		@Override
		public void onDataDeleted(String path) {
			WTActivity activity = mActivityWeakReference.get();
			if (activity == null) return;
			// Do something here.
		}
	}
}
```



# 最后

有问题欢迎Issue，欢迎 Pull request.
再求个Star★(*￣3￣)╭ 
