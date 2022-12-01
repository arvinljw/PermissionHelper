package net.arvin.permissionhelper.core;

import android.content.Context;

import net.arvin.permissionhelper.R;

public class DefaultPermissionTextProvider implements IPermissionTextProvider {

    private final Context context;

    public DefaultPermissionTextProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getEnsureBtnText() {
        return getString(context, R.string.permission_ensure);
    }

    @Override
    public String getCancelBtnText() {
        return getString(context, R.string.permission_cancel);
    }

    @Override
    public String getSettingMsg() {
        return getString(context, R.string.permission_setting_msg);
    }

    @Override
    public String getSettingEnsureText() {
        return getString(context, R.string.permission_setting);
    }

    @Override
    public String getSettingCancelText() {
        return getString(context, R.string.permission_cancel);
    }

    @Override
    public String getInstallAppMsg() {
        return getString(context, R.string.permission_install_tips);
    }

    @Override
    public int getTitleColor() {
        return getColor(context, R.color.ph_title_color);
    }

    @Override
    public int getMsgColor() {
        return getColor(context, R.color.ph_msg_color);
    }

    @Override
    public int getEnsureBtnColor() {
        return getColor(context, R.color.ph_enable_color);
    }

    @Override
    public int getCancelBtnColor() {
        return getColor(context, R.color.ph_cancel_color);
    }

    private String getString(Context context, int textId) {
        return context.getResources().getString(textId);
    }

    private int getColor(Context context, int colorId) {
        return context.getResources().getColor(colorId);
    }
}
