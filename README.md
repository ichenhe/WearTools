# 概述

WearTools 二次封装了[Ticear提供的sdk](https://bintray.com/ticwear/maven/mobvoi-api)，同时内部包含了`Google Play services 10.2.0`，以便实现 Android Wear、Android Wear China、Ticwear 的全兼容。下载量超10万的[腕间图库](http://wg.chenhe.cc/)已经使用老版本的 WearTools 库一年。

弱弱地求个Star★(*￣3￣)╭ 

使用 WearTools ，可以大幅简化手表与手机的通讯代码，你只需关注业务逻辑而不必将大量精力放在传输的维护上。本库提供了不同系统不同设备下统一的API，手表与手机可以互为发送方与接收方并且不需要编写不同的代码。



# 特性

- 兼容多系统。（Andorid Wear、AW China、Ticwear）
- 提供全局静态函数便于发送。
- 回调与调用在同一线程。
- 支持 Request/Response 模型。
- 支持超时返回。




# 依赖与冲突处理

1. 添加仓库地址。
   由于本库依赖了Ticwear的sdk，但其并没有传至Jcenter，所以需要在 Project 的 build.gradle 的 allprojects-repositories 节点下添加仓库地址：

   ```
   maven {
   	url 'https://dl.bintray.com/ticwear/maven'
   }
   ```

2. 添加依赖。
   在 Module 的 build.gradle 中添加 WearTools 的依赖：

   ```
   compile ('cc.chenhe:wear-tools:1.0.0'){
   	exclude group: 'com.android.support'
   }
   ```

   如上，为了避免发生`android.support.VERSION`冲突，需要将部分库排除。

3. 删除多余依赖。
   你不应该再添加`com.ticwear:mobvoi-api`或`com.google.android.gms:play-services-wearable`的依赖项，如果有请删除，因为本项目内部已经依赖了这2个库。
   **注意：** 如果需要使用其他 Google play service，请统一使用10.2.0版本，否则可能出现问题。


# 具体使用方法见[wiki](https://github.com/liangchenhe55/WearTools/wiki).

# For the detail, see [wiki](https://github.com/liangchenhe55/WearTools/wiki).
