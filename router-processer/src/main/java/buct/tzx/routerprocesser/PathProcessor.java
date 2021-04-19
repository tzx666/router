package buct.tzx.routerprocesser;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import buct.tzx.routerannotation.Router;
import buct.tzx.routerannotation.RouterInfo;
import buct.tzx.routerannotation.RouterType;

import static javax.lang.model.element.Modifier.PUBLIC;


@AutoService(Processor.class)
public class PathProcessor extends AbstractProcessor {
    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private List<RouterInfo> routerInfoList;
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "android.app.Fragment";
    public static final String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    public static final String SERVICE = "android.app.Service";
    public static final String PARCELABLE = "android.os.Parcelable";
    public static final String KEY_MODULE_NAME = "ROUTER_MODULE_NAME";
    String moduleName = "";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mTypeUtils = processingEnvironment.getTypeUtils();
        mElementUtils = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        routerInfoList = new ArrayList<>();
        System.out.println("router start init>>>");
        Map<String, String> options = processingEnv.getOptions();
        if (!options.isEmpty()) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (moduleName.length() > 0) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            System.out.println("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            throw new RuntimeException("No module name");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Router.class.getName());
        return annotations;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("开始处理注解");
        System.out.println("找到了注解" + roundEnv.getElementsAnnotatedWith(Router.class).size() + "个");
        System.out.println("开始处理注解");
        long starttime = System.currentTimeMillis();
        for (Element s : roundEnv.getElementsAnnotatedWith(Router.class)) {
            if (s.getKind() != ElementKind.CLASS) {
                throw new IllegalArgumentException("错误的注解");
            }
            System.out.println("注解处理的类的名字是" + s.toString());
            Router realannotation = s.getAnnotation(Router.class);
            System.out.println("协议路径是" + " " + realannotation.Path());
            System.out.println("准备生成对应文件");
            TypeMirror type_Activity = mElementUtils.getTypeElement(ACTIVITY).asType();
            TypeMirror fragmentTm = mElementUtils.getTypeElement(FRAGMENT).asType();
            TypeMirror targetMirror = s.asType();
            System.out.println("classname" + ClassName.get(targetMirror).toString());
            /*
             * 构造一个map<string,RouterInfo>的字典
             * */
            RouterInfo info = null;
            if (mTypeUtils.isSubtype(targetMirror, type_Activity) || mTypeUtils.isSubtype(targetMirror, fragmentTm)) {
                // 是activity
                if (mTypeUtils.isSubtype(targetMirror, type_Activity)) {
                    info = new RouterInfo(ClassName.get(targetMirror).toString(), RouterType.ACTIVITY, realannotation.Path());
                    routerInfoList.add(info);
                } else if (mTypeUtils.isSubtype(targetMirror, fragmentTm)) {
                    // 是fragment
                    info = new RouterInfo(ClassName.get(targetMirror).toString(), RouterType.FRAGMENT, realannotation.Path());
                    routerInfoList.add(info);
                }
            } else {
                throw new IllegalStateException("只支持activity和fragment的跳转，其他的通信请使用事件总线！");
            }
        }
        // 这里注解收集完成，开始对每个包生成文件

        // 生成map
        /*
              ```Map<String, RouteMeta>```
        */
        if (routerInfoList.size() > 0) {
            ParameterizedTypeName RouterMap = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterInfo.class)
            );
            ParameterSpec rootParamSpec = ParameterSpec.builder(RouterMap, "routes").build();
                            /*
              Build method : 'loadInto'
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder("injectPath")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);
            String rootFileName = moduleName + "processor";
            ClassName router = ClassName.get(RouterInfo.class);

            for (RouterInfo info : routerInfoList) {
                System.out.println("添加字段" + info.getPath());
                loadIntoMethodOfRootBuilder.addStatement("routes.put($S, new $T($S,$T." + info.getType() + ",$S));", info.getPath(), router, info.getTargetRoute(), ClassName.get(RouterType.class), info.getPath());
            }
            try {
                JavaFile.builder("buct.tzx.routergenerated",
                        TypeSpec.classBuilder(rootFileName)
                                .addSuperinterface(ClassName.get(mElementUtils.getTypeElement("buct.tzx.routerannotation.IPath")))
                                .addModifiers(PUBLIC)
                                .addMethod(loadIntoMethodOfRootBuilder.build())
                                .build()
                ).build().writeTo(mFiler);
                routerInfoList.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
