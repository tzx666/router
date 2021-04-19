package buct.tzx.routerapi;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import buct.tzx.routerannotation.IPath;
import buct.tzx.routerannotation.RouterInfo;
import buct.tzx.routerannotation.RouterType;
import buct.tzx.routerapi.utils.ClassUtil;

public class BuctRouter {
    private static volatile BuctRouter router;
    private RouterManager _manager;
    private Context context;
    private static Handler mHandler;
    // 判断是否进行了初始化
    private boolean mIsInitialized = false;
    private BuctRouter() {
    }

    public static BuctRouter getInstance() {
        if (router == null) {
            synchronized (BuctRouter.class) {
                if (router == null) {
                    router = new BuctRouter();
                }
            }
        }
        return router;
    }
    public void init(Context context){
        _manager = RouterManager.getInstance();
        try {
            this.context =context;
            mHandler = new Handler(Looper.getMainLooper());
            Set<String>classlist = ClassUtil.getFileNameByPackageName(context,"buct.tzx.routergenerated");
            Log.d("TAG123", "init: "+classlist.size());
            for(String clz:classlist){
                Log.d("TAG123", "init: "+clz);
                ((IPath)Class.forName(clz).getConstructor().newInstance()).injectPath(_manager.getRouterMap());
            }
            mIsInitialized =true;
        } catch (PackageManager.NameNotFoundException | IOException | InterruptedException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    public Object naviagtion(Activity context, String path){
        if(mIsInitialized){
            RouterInfo info = _manager.getRouterMap().getOrDefault(path,null);
            if(info==null){
                Toast.makeText(context,"没有找到注册类",Toast.LENGTH_SHORT).show();
            }else{
                if(info.getType()== RouterType.ACTIVITY){
                    try {
                        Intent intent =new Intent(context, Class.forName(info.getTargetRoute()));
                        // Navigation in main looper.
                        runInMainThread(() -> ActivityCompat.startActivity(context,intent,null));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }else if(info.getType()== RouterType.FRAGMENT){
                    FragmentManager fragmentManager = context.getFragmentManager();
                    try {
                        Object targetfragment = Class.forName(info.getTargetRoute()).getConstructor().newInstance();
                        return targetfragment;
                    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(context,"暂不支持此类型跳转",Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            throw new IllegalStateException("还没有初始化！");
        }
        return null;
    }
    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
    private void Filter(String path){
        String[] pathPraser = path.split("\\?");

    }
}
