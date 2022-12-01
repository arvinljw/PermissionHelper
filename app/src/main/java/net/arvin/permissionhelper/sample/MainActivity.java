package net.arvin.permissionhelper.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.arvin.permissionhelper.PermissionHelper;
import net.arvin.permissionhelper.core.DefaultPermissionTipsDialogProvider;
import net.arvin.permissionhelper.core.RequestInstallAppListener;
import net.arvin.permissionhelper.core.RequestPermissionListener;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    PermissionHelper permissionUtil;
    TextView tvDeviceInfo;
    String authority = "net.arvin.permissionhelper.sample.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);

    }

    private void initPermissionUtil() {
        permissionUtil = new PermissionHelper.Builder()
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
                .setDialogProvider(new DefaultPermissionTipsDialogProvider())
                .build();
    }

    public void requestPermission(View view) {
        if (permissionUtil == null) {
            initPermissionUtil();
        }
        permissionUtil.request("需要读取手机信息以及文件读写权限",
                PermissionHelper.asArray(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                new RequestPermissionListener() {
                    @Override
                    public void callback(boolean granted, boolean isAlwaysDenied) {
                        if (granted) {
                            getFilePath();
                            TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (phone != null) {
                                @SuppressLint({"MissingPermission", "HardwareIds"})
                                String deviceId = phone.getDeviceId();
                                tvDeviceInfo.setText(deviceId);
                                Toast.makeText(MainActivity.this, "权限申请成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "deviceId is null", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (isAlwaysDenied) {
                                Toast.makeText(MainActivity.this, "权限申请失败，用户已拒绝且不提示，请自行到设置中修改", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "权限申请失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void getFilePath() {
        File externalFilesDir = getExternalFilesDir(null);
        File externalCacheDir = getExternalCacheDir();

        String filePath = getFilesDir().getAbsolutePath();
        String cachePath = getCacheDir().getAbsolutePath();
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String externalFilePath = externalFilesDir == null ? "" : externalFilesDir.getAbsolutePath();
        String externalCachePath = externalCacheDir == null ? "" : externalCacheDir.getAbsolutePath();

        Log.d("filePath = ", filePath);
        Log.d("cachePath = ", cachePath);
        Log.d("externalPath = ", externalPath);
        Log.d("externalFilePath = ", externalFilePath);
        Log.d("externalCachePath = ", externalCachePath);

        openCamera(externalCachePath);
    }

    private void openCamera(String dir) {
        File file = new File(dir, System.currentTimeMillis() + ".jpg");
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, PermissionHelper.getUri(this, intent, file, authority));
        startActivity(intent);
    }


    public void installApp(View v) {
        if (permissionUtil == null) {
            initPermissionUtil();
        }
        permissionUtil.requestInstallApp(new RequestInstallAppListener() {
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

    private void install(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = PermissionHelper.getUri(this, apk, authority);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public void openSetting(View v) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permissionUtil != null) {
            permissionUtil.removeListener();
            permissionUtil = null;
        }
    }
}
