package net.arvin.permissionhelper.core;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import net.arvin.permissionhelper.PermissionHelper;

import java.lang.reflect.Field;

public class DefaultPermissionTipsDialogProvider implements IPermissionTipsDialogProvider {

    private AlertDialog requestPermissionDialog;
    private AlertDialog openSettingDialog;
    private AlertDialog openInstallAppDialog;

    @Override
    public void showRequestPermissionDialog(Activity activity, PermissionHelper.Builder builder, String requestPermissionTipsMsg, final IDialogCallback callback) {
        if (requestPermissionDialog == null) {
            requestPermissionDialog = new CustomAlertDialogBuilder(activity).initRequestPermission(builder, requestPermissionTipsMsg).create();
            requestPermissionDialog.setCancelable(builder.isRequestCancelable());
            requestPermissionDialog.setCanceledOnTouchOutside(builder.isRequestCancelable());
        }
        requestPermissionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.cancel();
            }
        });
        requestPermissionDialog.setMessage(requestPermissionTipsMsg);
        requestPermissionDialog.show();

        setAlertDialogColorAndCallback(requestPermissionDialog, builder, callback);
    }

    @Override
    public void showOpenSettingDialog(Activity activity, PermissionHelper.Builder builder, final IDialogCallback callback) {
        if (openSettingDialog == null) {
            openSettingDialog = new CustomAlertDialogBuilder(activity).initSetting(builder).create();
            openSettingDialog.setCancelable(builder.isSettingCancelable());
            openSettingDialog.setCanceledOnTouchOutside(builder.isSettingCancelable());
        }
        openSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.cancel();
            }
        });
        openSettingDialog.show();
        setAlertDialogColorAndCallback(openSettingDialog, builder, callback);
    }

    @Override
    public void showInstallDialog(Activity activity, PermissionHelper.Builder builder, final IDialogCallback callback) {
        if (openInstallAppDialog == null) {
            openInstallAppDialog = new CustomAlertDialogBuilder(activity).initInstallApp(builder).create();
            openInstallAppDialog.setCancelable(builder.isSettingCancelable());
            openInstallAppDialog.setCanceledOnTouchOutside(builder.isSettingCancelable());
        }
        openInstallAppDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.cancel();
            }
        });
        openInstallAppDialog.show();
        setAlertDialogColorAndCallback(openInstallAppDialog, builder, callback);
    }

    private void setAlertDialogColorAndCallback(final AlertDialog alertDialogColor, PermissionHelper.Builder resBuilder, final IDialogCallback callback) {
        Button positiveBtn = alertDialogColor.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.ensure();
                alertDialogColor.dismiss();
            }
        });
        if (isValidateRes(resBuilder.getEnsureBtnColor())) {
            positiveBtn.setTextColor(resBuilder.getEnsureBtnColor());
        }
        Button btnNegative = alertDialogColor.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.cancel();
                alertDialogColor.dismiss();
            }
        });
        if (isValidateRes(resBuilder.getCancelBtnColor())) {
            btnNegative.setTextColor(resBuilder.getCancelBtnColor());
        }
        if (isValidateRes(resBuilder.getTitleColor())) {
            setReflectTextColor(alertDialogColor, "mTitleView", resBuilder.getTitleColor());
        }
        if (isValidateRes(resBuilder.getMsgColor())) {
            setReflectTextColor(alertDialogColor, "mMessageView", resBuilder.getMsgColor());
        }
    }

    private boolean isValidateRes(int resColor) {
        return resColor != 0;
    }

    private void setReflectTextColor(AlertDialog alertDialog, String fieldName, @ColorInt int color) {
        try {
            Class<AlertDialog> dialogClass = AlertDialog.class;
            Field mAlertField = dialogClass.getDeclaredField("mAlert");
            mAlertField.setAccessible(true);
            Object mAlert = mAlertField.get(alertDialog);

            Field field = mAlert.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            TextView textView = (TextView) field.get(mAlert);
            textView.setTextColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class CustomAlertDialogBuilder extends AlertDialog.Builder {
        CustomAlertDialogBuilder(@NonNull Context context) {
            super(context);
        }

        CustomAlertDialogBuilder initRequestPermission(PermissionHelper.Builder resBuilder, String msg) {
            setMessage(msg);
            if (!TextUtils.isEmpty(resBuilder.getTitleText())) {
                setTitle(resBuilder.getTitleText());
            }
            if (!TextUtils.isEmpty(resBuilder.getEnsureBtnText())) {
                setPositiveButton(resBuilder.getEnsureBtnText(), null);
            }
            if (!TextUtils.isEmpty(resBuilder.getCancelBtnText())) {
                setNegativeButton(resBuilder.getCancelBtnText(), null);
            }
            return this;
        }

        CustomAlertDialogBuilder initSetting(PermissionHelper.Builder resBuilder) {
            setMessage(resBuilder.getSettingMsg());
            if (!TextUtils.isEmpty(resBuilder.getTitleText())) {
                setTitle(resBuilder.getTitleText());
            }
            if (!TextUtils.isEmpty(resBuilder.getSettingEnsureText())) {
                setPositiveButton(resBuilder.getSettingEnsureText(), null);
            }
            if (!TextUtils.isEmpty(resBuilder.getSettingCancelText())) {
                setNegativeButton(resBuilder.getSettingCancelText(), null);
            }
            return this;
        }

        CustomAlertDialogBuilder initInstallApp(PermissionHelper.Builder resBuilder) {
            setMessage(resBuilder.getInstallAppMsg());
            if (!TextUtils.isEmpty(resBuilder.getTitleText())) {
                setTitle(resBuilder.getTitleText());
            }
            if (!TextUtils.isEmpty(resBuilder.getEnsureBtnText())) {
                setPositiveButton(resBuilder.getEnsureBtnText(), null);
            }
            if (!TextUtils.isEmpty(resBuilder.getCancelBtnText())) {
                setNegativeButton(resBuilder.getCancelBtnText(), null);
            }
            return this;
        }
    }
}
