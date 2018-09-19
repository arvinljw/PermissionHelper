package net.arvin.permissionhelper.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.arvin.permissionhelper.PermissionUtil;

public class MainActivity extends AppCompatActivity {
    PermissionUtil permissionUtil;
    TextView tvDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
    }

    public void requestPermission(View view) {
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtil.Builder()
                    .with(this)//必传：可使用FragmentActivity或v4.Fragment实例
                    .setTitleText("提示")//弹框标题
                    .setEnsureBtnText("确定")//权限说明弹框授权按钮文字
                    .setCancelBtnText("取消")//权限说明弹框取消授权按钮文字
                    .setSettingEnsureText("设置")//打开设置说明弹框打开按钮文字
                    .setSettingCancelText("取消")//打开设置说明弹框关闭按钮文字
                    .setSettingMsg("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。")//打开设置说明弹框内容文字
                    .setShowSetting(true)//是否打开设置说明弹框
                    .setTitleColor(Color.BLACK)//弹框标题文本颜色
                    .setMsgColor(Color.GRAY)//弹框内容文本颜色
                    .setEnsureBtnColor(Color.BLACK)//弹框确定文本颜色
                    .setCancelBtnColor(Color.BLACK)//弹框取消文本颜色
                    .build();
        }
        permissionUtil.request("需要读取手机信息权限",
                PermissionUtil.asArray(Manifest.permission.READ_PHONE_STATE),
                new PermissionUtil.RequestPermissionListener() {
                    @Override
                    public void callback(boolean granted, boolean isAlwaysDenied) {
                        if (granted) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permissionUtil != null) {
            permissionUtil.removeListener();
            permissionUtil = null;
        }
    }
}
