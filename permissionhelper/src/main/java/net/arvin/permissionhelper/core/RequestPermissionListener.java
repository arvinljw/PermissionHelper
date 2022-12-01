package net.arvin.permissionhelper.core;

public interface RequestPermissionListener {
    /**
     * @param granted        权限是否通过，如果有多个权限的话表示是否全部通过
     * @param isAlwaysDenied false表示会重复提示，true表示拒绝且不再提示
     */
    void callback(boolean granted, boolean isAlwaysDenied);
}
