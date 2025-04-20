package nep.timeline.millet_enhance;

import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class BroadcastIntentHook {
    public BroadcastIntentHook(ClassLoader classLoader) {
       try {
           Class<?> clazz = XposedHelpers.findClassIfExists("com.android.server.am.ActivityManagerService", classLoader);
           if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
               Class<?> controller = XposedHelpers.findClassIfExists("com.android.server.am.BroadcastController", classLoader);
               if (controller != null)
                   clazz = controller;
           }

           if (clazz == null) {
               XposedBridge.log(GlobalVars.TAG + " -> Failed to listen broadcast intent!");
               return;
           }

           Method targetMethod = null;
           for (Method method : clazz.getDeclaredMethods())
               if (method.getName().equals("broadcastIntentLocked") && (targetMethod == null || targetMethod.getParameterTypes().length < method.getParameterTypes().length))
                   targetMethod = method;

           ArrayList<Object> arrayList = new ArrayList<>(Arrays.asList(targetMethod.getParameterTypes()));
           arrayList.add(new XC_MethodHook() {
               @Override
               protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                   try {
                       int intentArgsIndex = 2;
                       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                           intentArgsIndex = 3;

                       int userIdIndex = 15;
                       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                           userIdIndex = 17;
                       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R)
                           userIdIndex = 18;
                       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S)
                           userIdIndex = 19;
                       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2 && Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                           userIdIndex = 20;
                       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
                           userIdIndex = 21;

                       Intent intent = (Intent) param.args[intentArgsIndex];
                       int userId = (int) param.args[userIdIndex];
                       if (intent != null) {
                           String action = intent.getAction();

                           if (action == null || !action.endsWith(".android.c2dm.intent.RECEIVE"))
                               return;

                           String packageName = (intent.getComponent() == null ? intent.getPackage() : intent.getComponent().getPackageName());

                           if (packageName == null)
                               return;

                           int uid = ActivityManagerService.getUidFromPackage(packageName, userId);
                           if (uid != -1 && GreezeManagerService.isUidFrozen(uid))
                               GreezeManagerService.thawUid(uid, 1000, "FCM");
                       }
                   } catch (Throwable throwable) {
                       XposedBridge.log(GlobalVars.TAG + " -> Throw exception:");
                       XposedBridge.log(throwable);
                   }
               }
           });

           XposedHelpers.findAndHookMethod(clazz, targetMethod.getName(), arrayList.toArray());

           XposedBridge.log(GlobalVars.TAG + " -> Listen broadcast intent");
       } catch (Throwable throwable) {
           XposedBridge.log(GlobalVars.TAG + " -> Failed to listen broadcast because: " + throwable.getMessage());
       }
    }
}
