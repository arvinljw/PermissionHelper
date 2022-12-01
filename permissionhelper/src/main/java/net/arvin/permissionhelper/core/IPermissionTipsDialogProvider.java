package net.arvin.permissionhelper.core;

import android.app.Activity;

import net.arvin.permissionhelper.PermissionHelper;

public interface IPermissionTipsDialogProvider {

    void showRequestPermissionDialog(Activity activity, PermissionHelper.Builder builder, String requestPermissionTipsMsg, IDialogCallback callback);

    void showOpenSettingDialog(Activity activity, PermissionHelper.Builder builder, IDialogCallback callback);

    void showInstallDialog(Activity activity, PermissionHelper.Builder builder, IDialogCallback callback);
}
