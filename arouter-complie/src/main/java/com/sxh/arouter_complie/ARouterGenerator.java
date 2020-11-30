package com.sxh.arouter_complie;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.sxh.arouter_annotation.RouterBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * 使用@Router注解，APT代码生成器
 */
public class ARouterGenerator {

    /**
     * 用于存放APT生成的文件
     */
    private final String aptPackageName = Config.APT_PACKAGE;

    private final Messager messager;

    private final Filer filer;

    private final String moduleName;

    private final Elements elementTool;

    /**
     * 存放group下对应的所有path列表，比如<"order", List<RouterBean>>
     */
    private final Map<String, List<RouterBean>> mAllPathMap;

    /**
     * 存放group下所有生成的path文件，比如<"order", "ARouter$$Path$$order.class">
     */
    private final Map<String, String> mAllGroupMap;

    private ARouterGenerator(Messager messager, Filer filer, Elements elementTool, Map<String, String> mAllGroupMap, Map<String, List<RouterBean>> mAllPathMap, String moduleName) {
        this.messager = messager;
        this.filer = filer;
        this.mAllPathMap = mAllPathMap;
        this.mAllGroupMap = mAllGroupMap;
        this.moduleName = moduleName;
        this.elementTool = elementTool;
    }

    /**
     * 生成Path相关的类
     *
     * @throws IOException
     */
    public void generatePathFile() throws IOException {
//        path最终生成的代码效果如下:
//        public class ARouter$$Path$$personal implements IPath {
//            @Override
//            public Map<String, RouterBean> getPathMap() {
//                Map<String, RouterBean> pathMap = new HashMap<>();
//                pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.ACTIVITY,PersonalActivity.class,"/personal/PersonalActivity", "personal"));
//                pathMap.put("/personal/PersonalActivity2",RouterBean.create(TypeEnum.ACTIVITY,PersonalActivity2.class,"/personal/PersonalActivity2", "personal"));
//                return pathMap;
//            }
//        }

        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        // IPath接口描述，所有Path文件均继承自IPath接口
        TypeElement pathType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_PATH);

        //定义返回值，即Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),                               // Map
                ClassName.get(String.class),                            // Map<String,
                ClassName.get(RouterBean.class)                         // Map<String, RouterBean>
        );

        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            //1. 生成方法
            //1.1 生成方法名
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(Config.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);
            //1.2 生成方法体，创建HashMap集合
            methodSpecBuilder.addStatement("$T<$T, $T> $N = new $T<>();",
                    ClassName.get(Map.class),                               // Map
                    ClassName.get(String.class),                            // Map<String,
                    ClassName.get(RouterBean.class),                        // Map<String,RouterBean>
                    Config.PATH_VAR,                                        // Map<String,RouterBean> pathMap
                    ClassName.get(HashMap.class));                          // Map<String,RouterBean> pathMap = new HashMap<>();

            //1.3 生成方法体，循环往HashMap添加元素
            for (RouterBean routerBean : entry.getValue()) {
                Element element = routerBean.getElement();
                methodSpecBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S, $S))",
                        Config.PATH_VAR,                                      // pathMap.put
                        routerBean.getPath(),                                 // pathMap.put("/personal/PersonalActivity",
                        ClassName.get(RouterBean.class),                      // pathMap.put("/personal/PersonalActivity",RouterBean.create
                        ClassName.get(RouterBean.TypeEnum.class),             // pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.
                        routerBean.getTypeEnum(),                             // pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.ACTIVITY,
                        ClassName.get((TypeElement) routerBean.getElement()), // pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.ACTIVITY,Personal_MainActivity.class,
                        routerBean.getPath(),                                 // pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.ACTIVITY,Personal_MainActivity.class,"/personal/Personal_MainActivity"
                        routerBean.getGroup()                                 // pathMap.put("/personal/PersonalActivity",RouterBean.create(TypeEnum.ACTIVITY,Personal_MainActivity.class,"/personal/Personal_MainActivity","personal"));
                );
            }

            //1.3 生成方法体，添加返回语句
            methodSpecBuilder.addStatement("return $N", Config.PATH_VAR);

            //2. 生成类
            //注意：一般我们是按照1.方法，2.类  3.包的流程生成Java文件， 但是目前要生成的文件里面有implements，所以方法和类要合为一体生成才行

            //最终生成的类文件名  ARouter$$Path$$personal
            String finalClassName = Config.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>>>>>> APT生成路由Path类文件：" + aptPackageName + "." + finalClassName);

            JavaFile.builder(aptPackageName, // 包名  APT 存放的路径
                    TypeSpec.classBuilder(finalClassName) // 类名，即ARouter$$Path$$personal，personal为group名字
                            .addSuperinterface(ClassName.get(pathType)) // 实现IPath接口
                            .addModifiers(Modifier.PUBLIC) // 添加public修饰符
                            .addMethod(methodSpecBuilder.build()) // 添加方法
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

            //到这里，path文件已经全部生成完毕，我们把全类名保存到mAllGroupMap中，用于后面生成Group文件
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    /**
     * 生成路由组Group文件，如：ARouter$$Group$$app
     */
    public void generateGroupFile() throws IOException {
//        group最终生成的代码效果如下:
//        public class ARouter$$Group$$personal implements IGroup {
//            @Override
//            public Map<String, Class<? extends ARouterPath>> getGroupMap() {
//                Map<String, Class<? extends IPath>> groupMap = new HashMap<>();
//                groupMap.put("personal", ARouter$$Path$$personal.class);
//                return groupMap;
//            }
//        }

        if (ProcessorUtils.isEmpty(mAllGroupMap)) {
            return;
        }

        // IGroup接口描述，所有Group文件均继承自IGroup接口
        TypeElement groupType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_GROUP);
        TypeElement pathType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_PATH);


        //定义返回值，即Map<String, Class<? extends IPath>>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),        // Map
                ClassName.get(String.class),    // Map<String,
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends IPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // Map<String, Class<? extends IPath>>
        );

        //1. 生成方法
        //1.1 生成方法名
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(Config.GROUP_METHOD_NAME);
        methodSpecBuilder.addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(methodReturn);

        //1.2 生成方法体，创建HashMap集合
        methodSpecBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),                                      // Map<
                ClassName.get(String.class),                                   // Map<String
                ParameterizedTypeName.get(ClassName.get(Class.class),          // Map<String,Class
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),  // Map<String,Class<? extends IPath>>
                Config.GROUP_VAR,                                              // Map<String,Class<? extends IPath>> groupMap
                ClassName.get(HashMap.class));                                 // Map<String,Class<? extends ARouterPath>> groupMap = new HashMap<>();

        //1.3 生成方法体，循环添加元素
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodSpecBuilder.addStatement("$N.put($S, $T.class)",
                    Config.GROUP_VAR,                                         // groupMap.put
                    entry.getKey(),                                           // groupMap.put("personal",
                    ClassName.get(aptPackageName, entry.getValue()));         // groupMap.put("personal", ARouter$$Path$$personal.class);
        }

        //1.4 生成方法体，添加返回语句
        methodSpecBuilder.addStatement("return $N", Config.GROUP_VAR);

        //2. 生成类和包

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = Config.GROUP_FILE_NAME + moduleName;
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" + aptPackageName + "." + finalClassName);

        JavaFile.builder(aptPackageName,
                TypeSpec.classBuilder(finalClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(groupType))
                        .addMethod(methodSpecBuilder.build())
                        .build())
                .build()
                .writeTo(filer);
    }


    /**
     * 通过构建者模式创建ARouterGenerator对象
     */
    public static class Builder {

        private Messager messager;

        private Filer filer;

        private Elements elementTool;

        private Map<String, List<RouterBean>> allPathMap;

        private Map<String, String> allGroupMap;

        private String moduleName;

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setFiler(Filer filer) {
            this.filer = filer;
            return this;
        }

        public Builder setElementTool(Elements elementTool) {
            this.elementTool = elementTool;
            return this;
        }

        public Builder setAllPathMap(Map<String, List<RouterBean>> allPathMap) {
            this.allPathMap = allPathMap;
            return this;
        }

        public Builder setAllGroupMap(Map<String, String> allGroupMap) {
            this.allGroupMap = allGroupMap;
            return this;
        }

        public Builder setModuleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public ARouterGenerator build() {
            return new ARouterGenerator(messager, filer, elementTool, allGroupMap, allPathMap, moduleName);
        }
    }

}
