[ ![Download](https://api.bintray.com/packages/liangchenhe55/maven/wear-tools/images/download.svg) ](https://bintray.com/liangchenhe55/maven/wear-tools/_latestVersion)

**[English](#english)**

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

1. 添加依赖。

   在 Module 的 build.gradle 中添加 WearTools 的依赖：

   ```gradle
   implementation ('cc.chenhe:wear-tools:1.0.1'){
   	exclude group: 'com.android.support'
   }
   ```

   如上，为了避免发生 `android.support.VERSION` 冲突，建议将部分库排除。你也可以使用其他的方案解决版本冲突。
   
   如果正在使用 Android studio 3.0 以下版本，请把 `implementation` 替换为 `compile`.

2. 删除多余依赖。

   你不应该再添加`com.ticwear:mobvoi-api`或`com.google.android.gms:play-services-wearable`的依赖项，如果有请删除，因为本项目内部已经依赖了这2个库。
   
   **注意：** 如果需要使用其他 Google play service，请统一使用10.2.0版本，否则可能出现问题。


# 具体使用方法见[Wiki](https://github.com/liangchenhe55/WearTools/wiki#%E4%B8%AD%E6%96%87).

***

# English

# Summary

WearTools secondary encapsulated [Ticear sdk](https://bintray.com/ticwear/maven/mobvoi-api), and compile `Google Play services 10.2.0`, to be compatible with Android Wear/Android Wear China/Ticwear.[Wear Gallery](http://wg.chenhe.cc/) which more than 100,000 downloads,  had been using this library for one year+.

Please give me a Star★(*￣3￣)╭ 

WearTools can dramatically simplify the communication code between wear and mobile. You just need to focus on the business logic and not be focused on the maintenance of the communication. This library provides a unified API for different systems and devices, that means  the watch and phone can be both sender and receiver, and don't have to write different code.

# Feature

- Compatible multisystem. (Andorid Wear/AW China/Ticwear)
- Global static functions are provided to facilitate sending.
- Callbacks are in the same thread as the call.
- Support Request/Response model.
- Support timeout.

# Dependence and Conflict

1. Add dependence.

   Add dependence in Module's build.gradle:

   ```gradle
   implementation ('cc.chenhe:wear-tools:1.0.1'){
   	exclude group: 'com.android.support'
   }
   ```

   As above, we suggest exclude some libraries to avoid `android.support.VERSION` conflict. You can also use other solutions to resolve it.
   
   If you are using Android studio below 3.0，please replace `implementation` with `compile`.

2. Delete redundant dependencies.

   You shouldn't depend `com.ticwear:mobvoi-api` or `com.google.android.gms:play-services-wearable` again, because the two libraries were already included on the project.
   
   **Notice：** If you need use other Google play service, make sure they are version 10.2.0.

# For the detail, see [Wiki](https://github.com/liangchenhe55/WearTools/wiki#en).
