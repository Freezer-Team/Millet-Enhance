package nep.timeline.millet_enhance;

import de.robv.android.xposed.XposedHelpers;

public class GreezeManagerService {
    private static volatile Object instance;

    public static void setInstance(Object instance) {
        GreezeManagerService.instance = instance;
    }

    public static void thawUidAsync(int targetUid, int callerUid, String reason) {
        if (instance == null)
            return;

        XposedHelpers.callMethod(instance, "thawUidAsync", targetUid, callerUid, reason);
    }

    public static boolean thawUid(int targetUid, int callerUid, String reason) {
        if (instance == null)
            return false;

        return (boolean) XposedHelpers.callMethod(instance, "thawUid", targetUid, callerUid, reason);
    }

    public static boolean isUidFrozen(int targetUid) {
        if (instance == null)
            return false;

        return (boolean) XposedHelpers.callMethod(instance, "isUidFrozen", targetUid);
    }
}
