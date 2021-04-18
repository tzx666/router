package buct.tzx.routerapi;

import java.util.HashMap;
import java.util.Map;

import buct.tzx.routerannotation.RouterInfo;

public class RouterManager {
    private static RouterManager manager = new RouterManager();
    private Map<String, RouterInfo>RouterMap = new HashMap<>();
    public void register(String path,RouterInfo info){
        if(!RouterMap.containsKey(path)){
            RouterMap.put(path,info);
        }
    }
    public static RouterManager getInstance(){
        return manager;
    }
    public Map<String, RouterInfo> getRouterMap() {
        return RouterMap;
    }

}
