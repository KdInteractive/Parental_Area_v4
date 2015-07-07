package com.cide.interactive.parentalArea.Analytics;

/**
 * Created by lionel on 17/07/14.
 */

//used to send app analytics in json format to server
public class AppAnalyticsInfo {
    //specific parameters name to match db on server
    int install_count, uninstall_count, restored_from_preload, not_restored_from_preload,
            installed_from_parental, uninstalled_from_parental, new_app_installed_from_parental,
            updated_from_parental;
    String package_name, serial;

    public int getInstallCount() {
        return install_count;
    }

    public void setInstallCount(int mInstallCount) {
        this.install_count = mInstallCount;
    }

    public int getUninstallCount() {
        return uninstall_count;
    }

    public void setUninstallCount(int mUninstallCount) {
        this.uninstall_count = mUninstallCount;
    }

    public int getRestoredCount() {
        return restored_from_preload;
    }

    public void setRestoredCount(int mRestoredCount) {
        this.restored_from_preload = mRestoredCount;
    }

    public int getNotRestoredCount() {
        return not_restored_from_preload;
    }

    public void setNotRestoredCount(int mNotRestoredCount) {
        this.not_restored_from_preload = mNotRestoredCount;
    }

    public String getPackageName() {
        return package_name;
    }

    public void setPackageName(String mPackageName) {
        this.package_name = mPackageName;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String mSerial) {
        this.serial = mSerial;
    }

    public int getNewAppInstalledFromParental() {
        return new_app_installed_from_parental;
    }

    public void setNewAppInstalledFromParental(int newAppInstalledFromParental) {
        this.new_app_installed_from_parental = newAppInstalledFromParental;
    }

    public int getUninstalledFromParental() {
        return uninstalled_from_parental;
    }

    public void setUninstalledFromParental(int uninstalledFromParental) {
        this.uninstalled_from_parental = uninstalledFromParental;
    }

    public int getInstalledFromParental() {
        return installed_from_parental;
    }

    public void setInstalledFromParental(int installedFromParental) {
        this.installed_from_parental = installedFromParental;
    }

    public int getUpdatedFromParental() {
        return updated_from_parental;
    }

    public void setUpdatedFromParental(int updatedFromParental) {
        this.updated_from_parental = updatedFromParental;
    }
}
