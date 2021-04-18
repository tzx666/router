package buct.tzx.routerannotation;

import java.util.Map;

public interface IPath {
    void injectPath(Map<String,RouterInfo> map);
}
