package nep.timeline.millet_enhance;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ActivityManagerService {
    private static volatile Object instance;

    public static void setInstance(Object instance) {
        ActivityManagerService.instance = instance;
    }

    public static Object getInstance() {
        return instance;
    }

    public static Context getContext() {
        if (instance == null)
            return null;

        return (Context) XposedHelpers.getObjectField(instance, "mContext");
    }

    public static int getUidFromPackage(String packageName, int userId) {
        Context context = getContext();
        if (context == null)
            return -1;

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = (ApplicationInfo) XposedHelpers.callMethod(pm, "getApplicationInfoAsUser", packageName, 0, userId);
            return appInfo.uid;
        } catch (Throwable throwable) {
            XposedBridge.log(GlobalVars.TAG + " -> Throw exception:");
            XposedBridge.log(throwable);
            return -1;
        }
    }
}
