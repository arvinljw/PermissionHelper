package net.arvin.permissionhelper;

import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by arvinljw on 2018/9/17 16:30
 * Function：
 * Desc：6.0权限动态申请工具类
 */
public class PermissionUtil {
    private static final String TAG = PermissionUtil.class.getSimpleName();

    private Builder builder;
    private PermissionFragment permissionFragment;
    private RequestPermissionListener requestPermissionListener;

    PermissionUtil(Builder builder) {
        this.builder = builder;
        if (builder.activity != null) {
            permissionFragment = initFragment(builder.activity.getSupportFragmentManager());
            return;
        }
        if (builder.fragment != null) {
            permissionFragment = initFragment(builder.fragment.getChildFragmentManager());
            return;
        }
        Log.e(TAG, "PermissionUtil must set activity or fragment");
    }

    private PermissionFragment initFragment(FragmentManager fragmentManager) {
        PermissionFragment fragment = (PermissionFragment) fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new PermissionFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commitNow();
        }
        fragment.setPermissionUtil(this);
        return fragment;
    }

    public void request(String msg, String permissions, RequestPermissionListener listener) {
        request(msg, new String[]{permissions}, listener);
    }

    public void request(String msg, String[] permissions, RequestPermissionListener listener) {
        if (permissionFragment == null) {
            Log.e(TAG, "PermissionUtil must set activity or fragment");
            return;
        }
        if (permissions == null || permissions.length == 0) {
            Log.d(TAG, "permissions requires at least one input permission");
            return;
        }
        this.requestPermissionListener = listener;
        permissionFragment.request(msg, permissions);
    }

    public static String[] asArray(String... permissions) {
        return permissions;
    }

    public Builder getBuilder() {
        return builder;
    }

    void requestBack(boolean granted, boolean isAlwaysDenied) {
        if (requestPermissionListener != null) {
            requestPermissionListener.callback(granted, isAlwaysDenied);
        }
    }

    public void removeListener() {
        requestPermissionListener = null;
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
        /*是否现实设置弹框，默认显示*/
        private boolean isShowSetting = true;
        /*如果用户手动选择了不在提示申请权限的弹框，则让用户去打开设置界面，就是指这个文字提示，
         * 默认是：当前应用缺少必要权限。\n请点击"设置"-"权限"-打开所需权限。*/
        private String settingMsg;
        /*默认是：设置*/
        private String settingEnsureText;
        /*默认是：取消*/
        private String settingCancelText;

        /*颜色没有设置就是默认使用系统AlertDialog的对应颜色*/
        @ColorInt
        private int titleColor;
        @ColorInt
        private int msgColor;
        @ColorInt
        private int ensureBtnColor;
        @ColorInt
        private int cancelBtnColor;

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

        public Builder setShowSetting(boolean showSetting) {
            isShowSetting = showSetting;
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

        public String getTitleText() {
            return titleText;
        }

        public String getEnsureBtnText() {
            return ensureBtnText;
        }

        public String getCancelBtnText() {
            return cancelBtnText;
        }

        public boolean isShowSetting() {
            return isShowSetting;
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

        public PermissionUtil build() {
            if (textIsNone(ensureBtnText)) {
                ensureBtnText = "确定";
            }
            if (textIsNone(cancelBtnText)) {
                cancelBtnText = "取消";
            }
            if (textIsNone(settingMsg)) {
                settingMsg = "当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。";
            }
            if (textIsNone(settingEnsureText)) {
                settingEnsureText = "设置";
            }
            if (textIsNone(settingCancelText)) {
                settingCancelText = "取消";
            }
            return new PermissionUtil(this);
        }

        private boolean textIsNone(String str) {
            return str == null;
        }
    }

    public interface RequestPermissionListener {
        /**
         * @param granted        权限是否通过，如果有多个权限的话表示是否全部通过
         * @param isAlwaysDenied false表示会重复提示，true表示拒绝且不再提示
         */
        void callback(boolean granted, boolean isAlwaysDenied);
    }
}
