package buct.tzx.routerannotation;

import java.util.Objects;

/*
 * 一个用于存储路由相关信息的类，用于在编译期间存储注解信息并辅助生成java的类
 * create by tianzexin 2021/4/18 email 916196773@qq.com
 * */
public class RouterInfo {
    // 注解对应类
    private String targetRoute;
    // 跳转类型 activity、fragment、其他
    private RouterType type;
    // 注解路径
    private String path;

    public RouterInfo(String targetRoute, RouterType type, String path) {
        this.targetRoute = targetRoute;
        this.type = type;
        this.path = path;
    }

    public String getTargetRoute() {
        return targetRoute;
    }

    public RouterType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTargetRoute(String targetRoute) {
        this.targetRoute = targetRoute;
    }

    public void setType(RouterType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouterInfo that = (RouterInfo) o;
        return Objects.equals(targetRoute, that.targetRoute) &&
                type == that.type &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetRoute, type, path);
    }
}
