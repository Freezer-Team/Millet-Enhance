package nep.timeline.millet_enhance;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookInit implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam packageParam) {
        if ("android".equals(packageParam.packageName)) {
            ClassLoader classLoader = packageParam.classLoader;
            Class<?> greezeService = XposedHelpers.findClassIfExists("com.miui.server.greeze.GreezeManagerService", classLoader);
            if (greezeService == null) {
                XposedBridge.log(GlobalVars.TAG + " -> Your device is unsupported!");
                return;
            }

            XposedBridge.log(GlobalVars.TAG + " -> Start hooking!");
            
            try {
                XposedHelpers.findAndHookMethod(greezeService, "init", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        GreezeManagerService.setInstance(param.thisObject);
                    }
                });
            } catch (Throwable ignored) {
                XposedHelpers.findAndHookConstructor(greezeService, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        GreezeManagerService.setInstance(param.thisObject);
                    }
                });
            }

            XposedHelpers.findAndHookMethod(XposedHelpers.findClassIfExists("com.android.server.am.ActivityManagerService", classLoader), "setSystemProcess", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    ActivityManagerService.setInstance(param.thisObject);
                }
            });

            new BroadcastIntentHook(classLoader);

            XposedBridge.log(GlobalVars.TAG + " -> Hook success!");
        }
    }
}
