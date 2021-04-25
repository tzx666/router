package buct.tzx.routerapi;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import buct.tzx.routerannotation.IPath;
import buct.tzx.routerannotation.RouterInfo;

public class RouterManager {
    private static RouterManager manager = new RouterManager();
    private static Map<String, RouterInfo>RouterMap = new HashMap<>();
    public void register(String path,RouterInfo info){
        if(!RouterMap.containsKey(path)){
            RouterMap.put(path,info);
        }
    }
    private static void register(IPath autoFile){
        Log.d("tzx", "register: ");
        autoFile.injectPath(RouterMap);
    }
    public static void autoInjectIntoMap(){
        // 通过asm字节码自动注入，like
        // register("com.toos.appprocessor");
        //etc
    }
    private static void register(String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                if (obj instanceof IPath) {
                    register((IPath) obj);
                } else {
                    Log.d("TAG123", "register failed, class name: " + className);
                }
            } catch (Exception e) {
                Log.d("TAG","register class error:" + className, e);
            }
        }
    }
    public static RouterManager getInstance(){
        return manager;
    }
    public Map<String, RouterInfo> getRouterMap() {
        return RouterMap;
    }
}
