## 适配8.0安装apk权限

8.0进一步加强了手机的安全性，默认在自己的应用内都不能安装apk，需要在系统的软件商店安装才可以。而我们可以提示用户去打开这个权限，让我们的应用也能去安装apk，最常见的就是应用的版本更新。

对于这个功能，安装apk，首先需要申明安装权限：

```
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
```

然后就是`getPackageManager().canRequestPackageInstalls()`判断是否拥有权限，没有的话就打开允许安装此来源的应用设置界面，让用户手动设置，在`onActivityResult`中再去判断是否拥有权限，有的话就去安装。

原理很简单，本库中也封装好了，像6.0申请动态权限一样不再那么麻烦，只需要一句话，等待回调即可。

```
public void installApp(View v) {
    if (permissionUtil == null) {
        initPermissionUtil();
    }
    permissionUtil.requestInstallApp(new PermissionUtil.RequestInstallAppListener() {
        @Override
        public void canInstallApp(boolean canInstall) {
            if (canInstall) {
                Toast.makeText(MainActivity.this, "安装app", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "不能安装app", Toast.LENGTH_SHORT).show();
            }
        }
    });
}
```

当然也是需要在onDestroy的时候移除掉监听，避免内存泄露。

```
@Override
protected void onDestroy() {
    super.onDestroy();
    if (permissionUtil != null) {
        permissionUtil.removeListener();
        permissionUtil = null;
    }
}
```

对于安装应用，因为要使用到文件共享的权限，还需要适配7.0，当然也是封装好了的，具体内容可以参考[文件权限](https://github.com/arvinljw/PermissionHelper/blob/master/doc/文件权限.md)一文，这里也封装了一个简单方法，去安装apk。

```
private void install(File apk) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = PermissionUtil.getUri(this, intent, apk);
    intent.setDataAndType(uri, "application/vnd.android.package-archive");
    startActivity(intent);
}
```

到此，8.0apk文件安装的适配也告一段落了。

其中参考了这篇文章[完美的适配Android8.0未知来源应用安装权限方案](https://blog.csdn.net/changmu175/article/details/78906829)，感谢作者。