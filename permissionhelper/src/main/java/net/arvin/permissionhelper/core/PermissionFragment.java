package net.arvin.permissionhelper.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import net.arvin.permissionhelper.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arvinljw on 2018/9/17 16:32
 * Function：
 * Desc：6.0权限动态申请实现类
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PermissionFragment extends Fragment {
    private static final int REQUEST_CODE_PERMISSION = 0x1001;
    private static final int REQUEST_CODE_SETTING = 0x1002;
    private static final int REQUEST_CODE_INSTALL_APP = 0x1003;

    private Context context;
    private PermissionHelper permissionUtil;

    private String requestMsg;
    private String[] requestPermissions;
    private boolean requestInstall;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        context = getActivity();
        if (savedInstanceState != null) {
            requestMsg = savedInstanceState.getString("requestMsg");
            requestPermissions = savedInstanceState.getStringArray("requestPermissions");
            requestInstall = savedInstanceState.getBoolean("requestInstall");
        }

        if (requestMsg != null) {
            request(requestMsg, requestPermissions);
        }
        if (requestInstall) {
            requestInstallApp();
        }
    }

    public void setPermissionUtil(PermissionHelper permissionUtil) {
        this.permissionUtil = permissionUtil;
    }

    public void request(String msg, String[] permissions) {
        this.requestMsg = msg;
        this.requestPermissions = permissions;
        if (context == null) {
            return;
        }
        if (checkIsGranted(permissions)) {
            requestBack(true);
        } else {
            requestPermissions(msg);
        }
    }

    public boolean checkIsGranted(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String perm : permissions) {
            boolean hasPerm = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                    || PermissionChecker.checkSelfPermission(context, perm) == PermissionChecker.PERMISSION_GRANTED;
            if (!hasPerm) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions(String msg) {
        boolean shouldShowRationale = false;
        for (String perm : requestPermissions) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(perm);
        }

        if (shouldShowRationale) {
            Activity activity = getActivity();
            if (null == activity || permissionUtil == null) {
                Log.d("PermissionFragment", "permissionUtil is null");
                return;
            }
            showRequestPermissionDialog(msg, activity);
        } else {
            requestPermissions(requestPermissions, REQUEST_CODE_PERMISSION);
        }
    }

    private void showRequestPermissionDialog(String msg, Activity activity) {
        PermissionHelper.Builder resBuilder = permissionUtil.getBuilder();
        if (!resBuilder.isShowRequest()) {
            requestPermissions(requestPermissions, REQUEST_CODE_PERMISSION);
            return;
        }
        resBuilder.getDialogProvider().showRequestPermissionDialog(activity, resBuilder, msg, new IDialogCallback() {

            @Override
            public void ensure() {
                requestPermissions(requestPermissions, REQUEST_CODE_PERMISSION);
            }

            @Override
            public void cancel() {
                requestBack(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE_PERMISSION) {
            return;
        }

        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        if (!denied.isEmpty()) {
            onPermissionsDenied(denied);
        }

        if (!granted.isEmpty() && denied.isEmpty()) {
            requestBack(true);
        }
    }

    private void onPermissionsDenied(List<String> deniedPerms) {
        boolean shouldShowRationale = true;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(perm);
            if (!shouldShowRationale) {
                break;
            }
        }
        if (!shouldShowRationale) {
            showOpenSettingDialog();
        } else {
            requestBack(false);
        }
    }

    private void showOpenSettingDialog() {
        final Activity activity = getActivity();
        if (activity == null || permissionUtil == null) {
            return;
        }
        PermissionHelper.Builder resBuilder = permissionUtil.getBuilder();
        if (!resBuilder.isShowSetting()) {
            requestBack(false, true);
            return;
        }
        resBuilder.getDialogProvider().showOpenSettingDialog(activity, resBuilder, new IDialogCallback() {
            @Override
            public void ensure() {
                openSetting(activity);
            }

            @Override
            public void cancel() {
                requestBack(false);
            }
        });
    }

    private void openSetting(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_SETTING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTING) {
            request(requestMsg, requestPermissions);
        }
        if (requestCode == REQUEST_CODE_INSTALL_APP) {
            requestInstallApp(true);
        }
    }

    public void requestInstallApp() {
        requestInstallApp(false);
    }

    private void requestInstallApp(boolean fromResult) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context == null) {
                requestInstall = true;
                return;
            }
            boolean haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {
                showOpenInstallAppPermissionDialog(fromResult);
                return;
            }
        }
        callCanInstallApp(true);
    }

    private void showOpenInstallAppPermissionDialog(boolean fromResult) {
        final Activity activity = getActivity();
        if (null == activity || permissionUtil == null) {
            return;
        }
        PermissionHelper.Builder resBuilder = permissionUtil.getBuilder();
        if (!resBuilder.isShowInstall()) {
            if (fromResult) {
                callCanInstallApp(false);
            } else {
                openInstallAppSetting();
            }
            return;
        }
        resBuilder.getDialogProvider().showInstallDialog(activity, resBuilder, new IDialogCallback() {

            @Override
            public void ensure() {
                openInstallAppSetting();
            }

            @Override
            public void cancel() {
                callCanInstallApp(false);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void openInstallAppSetting() {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, REQUEST_CODE_INSTALL_APP);
    }

    private void requestBack(boolean granted) {
        requestBack(granted, false);
    }

    private void requestBack(boolean granted, boolean isAlwaysDenied) {
        if (permissionUtil != null) {
            permissionUtil.requestBack(granted, isAlwaysDenied);
        }
        requestMsg = null;
        requestPermissions = null;
    }

    private void callCanInstallApp(boolean canInstall) {
        if (permissionUtil != null) {
            permissionUtil.callCanInstallApp(canInstall);
        }
        requestInstall = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (requestMsg != null) {
            outState.putString("requestMsg", requestMsg);
            outState.putStringArray("requestPermissions", requestPermissions);
        }
        if (requestInstall) {
            outState.putBoolean("requestInstall", true);
        }
    }

    @Override
    public void onDestroyView() {
        context = null;
        permissionUtil = null;
        super.onDestroyView();
    }
}
