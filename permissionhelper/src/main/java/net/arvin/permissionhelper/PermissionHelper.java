package net.arvin.permissionhelper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import net.arvin.permissionhelper.core.DefaultPermissionTextProvider;
import net.arvin.permissionhelper.core.DefaultPermissionTipsDialogProvider;
import net.arvin.permissionhelper.core.IPermissionTextProvider;
import net.arvin.permissionhelper.core.IPermissionTipsDialogProvider;
import net.arvin.permissionhelper.core.PermissionFragment;
import net.arvin.permissionhelper.core.RequestInstallAppListener;
import net.arvin.permissionhelper.core.RequestPermissionListener;

import java.io.File;

/**
 * Created by arvinljw on 2018/9/17 16:30
 * Function：
 * Desc：6.0权限动态申请工具类
 */
public class PermissionHelper {
    private static final String TAG = PermissionHelper.class.getSimpleName();

    private final Builder builder;
    private PermissionFragment permissionFragment;
    private RequestPermissionListener requestPermissionListener;
    private RequestInstallAppListener requestInstallAppListener;

    private static IPermissionTextProvider permissionTextProvider;

    public static void setPermissionTextProvider(IPermissionTextProvider provider) {
        permissionTextProvider = provider;
    }

    private PermissionHelper(Builder builder) {
        this.builder = builder;
        if (builder.activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionFragment = initFragment(builder.activity.getSupportFragmentManager());
            }
            return;
        }
        if (builder.fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionFragment = initFragment(builder.fragment.getChildFragmentManager());
            }
            return;
        }
        Log.e(TAG, "PermissionUtil must set activity or fragment");
    }

    public static void destroy() {
        //以防以后需要销毁的数据这里处理
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private PermissionFragment initFragment(FragmentManager fragmentManager) {
        PermissionFragment fragment = (PermissionFragment) fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new PermissionFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commit();
        }
        fragment.setPermissionUtil(this);
        return fragment;
    }

    public void request(String msg, String permission, RequestPermissionListener listener) {
        request(msg, new String[]{permission}, listener);
    }

    public void request(String msg, String[] permissions, RequestPermissionListener listener) {
        if (permissionFragment == null) {
            Log.e(TAG, "PermissionUtil must set activity or fragment");
            listener.callback(true, false);
            return;
        }
        if (permissions == null || permissions.length == 0) {
            Log.d(TAG, "permissions requires at least one input permission");
            return;
        }
        this.requestPermissionListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionFragment.request(msg, permissions);
        }
    }

    public static String[] asArray(String... permissions) {
        return permissions;
    }

    public Builder getBuilder() {
        return builder;
    }

    public void requestBack(boolean granted, boolean isAlwaysDenied) {
        if (requestPermissionListener != null) {
            requestPermissionListener.callback(granted, isAlwaysDenied);
        }
    }

    public void requestInstallApp(RequestInstallAppListener listener) {
        if (permissionFragment == null) {
            listener.canInstallApp(false);
            Log.e(TAG, "PermissionUtil must set activity or fragment");
            return;
        }
        this.requestInstallAppListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionFragment.requestInstallApp();
        } else {
            listener.canInstallApp(true);
        }
    }

    public void callCanInstallApp(boolean canInstall) {
        if (requestInstallAppListener != null) {
            requestInstallAppListener.canInstallApp(canInstall);
        }
    }

    public void removeListener() {
        requestPermissionListener = null;
        requestInstallAppListener = null;
        permissionFragment = null;
    }

    public static Uri getUri(@NonNull Context context, @NonNull File file, @NonNull String authority) {
        return getUri(context, null, file, authority);
    }

    public static Uri getUri(@NonNull Context context, Intent intent, @NonNull File file, @NonNull String authority) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static class Builder {
        private FragmentActivity activity;
        private Fragment fragment;

        /*没有设置代表不需要标题*/
        private String titleText;
        /*默认是：确定*/
        private String ensureBtnText;
        /*默认是：取消*/
        private String cancelBtnText;
        /*申请权限说明弹款是否cancelable*/
        private boolean isRequestCancelable = true;
        /*打开设置界面弹款是否cancelable*/
        private boolean isSettingCancelable = true;
        /*打开允许安装此来源引用弹款是否cancelable*/
        private boolean isInstallCancelable = true;
        /*是否显示申请权限弹框，默认显示*/
        private boolean isShowRequest = true;
        /*是否显示设置弹框，默认显示*/
        private boolean isShowSetting = true;
        /*是否显示允许安装此来源弹框，默认显示*/
        private boolean isShowInstall = true;
        /*如果用户手动选择了不在提示申请权限的弹框，则让用户去打开设置界面，就是指这个文字提示，
         * 默认是：当前应用缺少必要权限。\n请点击"设置"-"权限"-打开所需权限。*/
        private String settingMsg;
        /*默认是：设置*/
        private String settingEnsureText;
        /*默认是：取消*/
        private String settingCancelText;
        /*默认是：允许安装来自此来源的应用*/
        private String installAppMsg;

        /*颜色没有设置就是默认使用系统AlertDialog的对应颜色*/
        @ColorInt
        private int titleColor;
        @ColorInt
        private int msgColor;
        @ColorInt
        private int ensureBtnColor;
        @ColorInt
        private int cancelBtnColor;

        private IPermissionTipsDialogProvider dialogProvider;

        public Builder() {
        }

        public Builder with(FragmentActivity activity) {
            this.activity = activity;
            return this;
        }

        public Builder with(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder setTitleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public Builder setEnsureBtnText(String ensureBtnText) {
            this.ensureBtnText = ensureBtnText;
            return this;
        }

        public Builder setCancelBtnText(String cancelBtnText) {
            this.cancelBtnText = cancelBtnText;
            return this;
        }

        public Builder setShowRequest(boolean showRequest) {
            isShowRequest = showRequest;
            return this;
        }

        public Builder setRequestCancelable(boolean requestCancelable) {
            isRequestCancelable = requestCancelable;
            return this;
        }

        public Builder setSettingCancelable(boolean settingCancelable) {
            isSettingCancelable = settingCancelable;
            return this;
        }

        public Builder setInstallCancelable(boolean installCancelable) {
            isInstallCancelable = installCancelable;
            return this;
        }

        public Builder setShowSetting(boolean showSetting) {
            isShowSetting = showSetting;
            return this;
        }

        public Builder setShowInstall(boolean showInstall) {
            isShowInstall = showInstall;
            return this;
        }

        public Builder setSettingMsg(String settingMsg) {
            this.settingMsg = settingMsg;
            return this;
        }

        public Builder setSettingEnsureText(String settingEnsureText) {
            this.settingEnsureText = settingEnsureText;
            return this;
        }

        public Builder setSettingCancelText(String settingCancelText) {
            this.settingCancelText = settingCancelText;
            return this;
        }

        public Builder setInstallAppMsg(String installAppMsg) {
            this.installAppMsg = installAppMsg;
            return this;
        }

        public Builder setTitleColor(@ColorInt int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setMsgColor(@ColorInt int msgColor) {
            this.msgColor = msgColor;
            return this;
        }

        public Builder setEnsureBtnColor(@ColorInt int ensureBtnColor) {
            this.ensureBtnColor = ensureBtnColor;
            return this;
        }

        public Builder setCancelBtnColor(@ColorInt int cancelBtnColor) {
            this.cancelBtnColor = cancelBtnColor;
            return this;
        }

        public Builder setDialogProvider(IPermissionTipsDialogProvider dialogProvider) {
            this.dialogProvider = dialogProvider;
            return this;
        }

        public String getTitleText() {
            return titleText;
        }

        public String getEnsureBtnText() {
            return ensureBtnText;
        }

        public String getCancelBtnText() {
            return cancelBtnText;
        }

        public boolean isRequestCancelable() {
            return isRequestCancelable;
        }

        public boolean isSettingCancelable() {
            return isSettingCancelable;
        }

        public boolean isInstallCancelable() {
            return isInstallCancelable;
        }

        public boolean isShowRequest() {
            return isShowRequest;
        }

        public boolean isShowSetting() {
            return isShowSetting;
        }

        public boolean isShowInstall() {
            return isShowInstall;
        }

        public String getSettingMsg() {
            return settingMsg;
        }

        public String getSettingEnsureText() {
            return settingEnsureText;
        }

        public String getSettingCancelText() {
            return settingCancelText;
        }

        public String getInstallAppMsg() {
            return installAppMsg;
        }

        public int getTitleColor() {
            return titleColor;
        }

        public int getMsgColor() {
            return msgColor;
        }

        public int getEnsureBtnColor() {
            return ensureBtnColor;
        }

        public int getCancelBtnColor() {
            return cancelBtnColor;
        }

        public IPermissionTipsDialogProvider getDialogProvider() {
            return dialogProvider;
        }

        public PermissionHelper build() {
            Context context = null;
            if (activity != null) {
                context = activity;
            }
            if (fragment != null) {
                context = fragment.getActivity();
            }
            if (context == null) {
                return new PermissionHelper(this);
            }

            if (permissionTextProvider == null) {
                permissionTextProvider = new DefaultPermissionTextProvider(context);
            }

            if (dialogProvider == null) {
                dialogProvider = new DefaultPermissionTipsDialogProvider();
            }

            if (textIsNone(ensureBtnText)) {
                ensureBtnText = permissionTextProvider.getEnsureBtnText();
            }
            if (textIsNone(cancelBtnText)) {
                cancelBtnText = permissionTextProvider.getCancelBtnText();
            }
            if (textIsNone(settingMsg)) {
                settingMsg = permissionTextProvider.getSettingMsg();
            }
            if (textIsNone(settingEnsureText)) {
                settingEnsureText = permissionTextProvider.getSettingEnsureText();
            }
            if (textIsNone(settingCancelText)) {
                settingCancelText = permissionTextProvider.getSettingCancelText();
            }
            if (textIsNone(installAppMsg)) {
                installAppMsg = permissionTextProvider.getInstallAppMsg();
            }
            if (titleColor == 0) {
                titleColor = permissionTextProvider.getTitleColor();
            }
            if (msgColor == 0) {
                msgColor = permissionTextProvider.getMsgColor();
            }
            if (ensureBtnColor == 0) {
                ensureBtnColor = permissionTextProvider.getEnsureBtnColor();
            }
            if (cancelBtnColor == 0) {
                cancelBtnColor = permissionTextProvider.getCancelBtnColor();
            }
            return new PermissionHelper(this);
        }

        private boolean textIsNone(String str) {
            return str == null;
        }
    }

}
