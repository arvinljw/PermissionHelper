## PermissionHelper

最近又看到有人是分享使用空Fragment，来避免重写onActivityResult进行Activity之间信息的交互，这让我想起了之前看到的RxPermissions这个库也是使用这个原理去申请权限，当时觉得居然还有这种操作，想象力真是太好了。

但是即使它这个再好，我还是想说我这个可能更好一点。

[项目地址](https://github.com/arvinljw/PermissionHelper)

### 优点

* 没有使用多余的第三方库
* Google权限申请的最佳实践
* 使用简单，低耦合，可自定义提示框样式
* 集成6.0动态申请权限，适配7.0文件共享以及8.0安装来自未知来源的应用
* 申请Manifest中没有的权限时提示

### 用法

#### 引用

**1、在根目录的build.gradle中加入如下配置**

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**2、在要是用的module中增加如下引用**

```
dependencies {
    ...
    implementation 'com.android.support:appcompat-v7:27.1.1'
    implementation 'com.github.arvinljw:PermissionHelper:v1.0.1'
}
```

#### 使用

**1、初始化PermissionUtil**

```
permissionUtil = new PermissionUtil.Builder()
        .with(this)//必传：可使用FragmentActivity或v4.Fragment实例
        .setTitleText("提示")//弹框标题
        .setEnsureBtnText("确定")//权限说明弹框授权按钮文字
        .setCancelBtnText("取消")//权限说明弹框取消授权按钮文字
        .setSettingEnsureText("设置")//打开设置说明弹框打开按钮文字
        .setSettingCancelText("取消")//打开设置说明弹框关闭按钮文字
        .setSettingMsg("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。")//打开设置说明弹框内容文字
        .setInstallAppMsg("允许安装来自此来源的应用")//打开允许安装此来源的应用设置
        .setShowRequest(true)//是否显示申请权限弹框
        .setShowSetting(true)//是否显示设置弹框
        .setShowInstall(true)//是否显示允许安装此来源弹框
        .setRequestCancelable(true)//申请权限说明弹款是否cancelable
        .setSettingCancelable(true)//打开设置界面弹款是否cancelable
        .setInstallCancelable(true)//打开允许安装此来源引用弹款是否cancelable
        .setTitleColor(Color.BLACK)//弹框标题文本颜色
        .setMsgColor(Color.GRAY)//弹框内容文本颜色
        .setEnsureBtnColor(Color.BLACK)//弹框确定文本颜色
        .setCancelBtnColor(Color.BLACK)//弹框取消文本颜色
        .build();
```

可配置的属性很多，大致含义也注释写清楚了，必须调用的属性只有一个，其他都有默认值。

**简洁版可以这样**：

```
permissionUtil = new PermissionUtil.Builder().with(this).build();
```

需要说明一下动态申请权限可能有两个弹框：

* 第一个弹框：第一次申请权限被拒绝后，弹出的弹框，解释为什么需要这个权限。
* 第二个弹框：打开设置说明的弹框，只有当isShowSetting为true的时候，被拒绝一次之后，再次申请时再次拒绝且还点了不再提示，则通过打开设置去让用户手动修改权限。

其中弹框使用的是AlertDialog，通过反射去修改文本颜色，不设置则不会调用反射方法，会有默认颜色：

* 标题使用：textAppearanceLarge样式
* 内容实用：textAppearanceMedium样式
* 按钮颜色：默认使用colorAccent的颜色

**2、申请权限并回调**

```
permissionUtil.request("需要读取联系人权限",
        Manifest.permission.READ_PHONE_STATE,
        new PermissionUtil.RequestPermissionListener() {
            @Override
            public void callback(boolean granted, boolean isAlwaysDenied) {
                if (granted) {
                    //do your jobs..
                } else {
                    //show some tips..
                }
            }
        });
```

* 第一个参数：是申请权限说明，会在上文说的第一个弹框中作为内容显示
* 第二个参数：是一个所要申请的权限字符串，也可以使用字符串数组，例如申请多个权限可使用：

	`
PermissionUtil.asArray(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS)
	`
	
* 第三个参数：申请权限的回调，`granted`表示是否通过，如果有多个权限的话表示是否全部通过；`isAlwaysDenied ` false表示会重复提示，true表示拒绝且不再提示

通过这两步，就能完全权限的申请了，当然这里申请的权限需要在配置文件中定义。

**3、清除permissionUtil持有的回调**

这一步清除回调，避免匿名内部类引起的内存泄露。

```
if (permissionUtil != null) {
    permissionUtil.removeListener();
    permissionUtil = null;
}
```

### 7.0和8.0的适配

[7.0文件权限适配](https://github.com/arvinljw/PermissionHelper/blob/master/doc/文件权限.md)

[8.0安装未知来源应用适配](https://github.com/arvinljw/PermissionHelper/blob/master/doc/安装App权限.md)

### 混淆

```
-keep class net.arvin.permissionhelper.**{*;}
```

### 注意

这里想说，**有的手机只要用户永久拒绝了权限，那么打开设置去手动打开权限也是无效的**，索然会回调已获取权限，但是实际的使用中是获取不到那些信息的，例如联系人或者手机设备信息，我测试到的例如小米。当然如果能获取到的自然就不用管了。

**这个问题目前测试的其他库也存在，所以这个问题尚未找到最好的解决方案**

我参考了支付宝发现，在申请权限之前就先弹一个框告诉用户我需要这个权限，你要给我，如果你不给，就不去申请权限。但是如果点了给权限，但是在真正的权限弹框时又不给，依然无法解决，这个是系统的原因，改不了。

目前有两个办法能让处于拒绝且不再提示的应用重新获取权限：

* 打开应用设置详情界面（参照lib中打开方式），然后用户手动关闭相应权限，如果已关闭则打开再关闭，这时候这个系统会发送一个广播重启该应用，所以需要做好数据保存和恢复工作。
* 第二种方式就是手动清除应用所有数据，之前所有权限以及缓存都会消失，这种显然不是很友好，要是能找到权限的缓存数据存在哪个地方去手动清除也是一种办法，知道的朋友请不吝赐教。

这是我发现的这个问题，要是有更好的解决方案，从而不会有这个问题的也请不吝赐教，那就非常感谢了。

如果觉得好，可以star支持一下。

[项目地址](https://github.com/arvinljw/PermissionHelper)

### 参考

**参考文章：**

* [Request app permissions](https://developer.android.google.cn/training/permissions/requesting)
* [App permissions best practices](https://developer.android.google.cn/training/permissions/usage-notes)

**参考项目：**

* [RxPermissions](https://github.com/tbruyelle/RxPermissions)
* [easypermissions](https://github.com/googlesamples/easypermissions)

### License

```
Copyright 2018 arvinljw

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```


	

