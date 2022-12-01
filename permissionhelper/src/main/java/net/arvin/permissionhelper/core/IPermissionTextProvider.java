package net.arvin.permissionhelper.core;

import androidx.annotation.ColorRes;

/**
 * xxx 不直接调用sdk里的Resource类
 */
public interface IPermissionTextProvider {
    String getEnsureBtnText();

    String getCancelBtnText();

    String getSettingMsg();

    String getSettingEnsureText();

    String getSettingCancelText();

    String getInstallAppMsg();

    int getTitleColor();

    int getMsgColor();

    int getEnsureBtnColor();

    int getCancelBtnColor();
}
