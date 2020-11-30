package com.sxh.arouter_complie;

import com.google.auto.service.AutoService;
import com.sxh.arouter_annotation.Router;
import com.sxh.arouter_annotation.RouterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
//支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Config.AROUTER_PACKAGE})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
// 注解处理器接收的参数
@SupportedOptions({Config.MODULE_NAME})
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    /**
     * 各个模块传递过来的模块名称
     */
    private String moduleName;

    /**
     * 存放group对应的所有path的HashMap,存放Map<"order", List<RouterBean>>
     */
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

    /**
     * 存放group对应的,存放Map<"order", "ARouter$$Path$$order.class">
     */
    private Map<String, String> mAllGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();

        //获取主模块传递的参数
        moduleName = processingEnv.getOptions().get(Config.MODULE_NAME);

        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> moduleName:" + moduleName);

        if (moduleName != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> APT环境搭建完成");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> APT 环境有问题，请检查参数");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> begin process");

        if (annotations.isEmpty()) {
            // 直接结束了
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Router.class);

        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementTool.getTypeElement(Config.ACTIVITY_PACKAGE);
        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        TypeMirror activityMirror = activityType.asType();

        // 通过Element工具类，获取Call
        TypeElement callType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_CALL);
        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        TypeMirror callMirror = callType.asType();

        for (Element element : elements) {

            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@Router注解的类有：" + className);

            Router router = element.getAnnotation(Router.class);

            //各项合法检查
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(router.group())
                    .addPath(router.path())
                    .addElement(element)
                    .build();

            // ARouter注解的类 必须继承 Activity
            TypeMirror elementMirror = element.asType();
            //判断是否是Activity子类、ICall的子类
            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else if (typeTool.isSubtype(elementMirror, callMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.CALL);
            } else {
                // 不匹配，则抛出异常
                throw new RuntimeException("@Router注解使用不合法，未按规范配置");
            }

            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@Router注解合法：" + className);
                //证明合法，可以继续往下操作
                //检查 mAllPathMap 是否已经创建该group对应的list
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                // 如果为空，新建List集合再添加进Map，否则直接存入
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                //不合法
                messager.printMessage(Diagnostic.Kind.ERROR, "@Router注解使用不合法，未按规范配置");
            }
        }

        //执行到这里说明，我们已经将所有注解都转换成RouteBean对象，并存入了mAllPathMap集合中，下面开始正式生成文件了
        ARouterGenerator generator = new ARouterGenerator.Builder()
                .setAllGroupMap(mAllGroupMap)
                .setAllPathMap(mAllPathMap)
                .setFiler(filer)
                .setMessager(messager)
                .setElementTool(elementTool)
                .setModuleName(moduleName)
                .build();

        // 第一步：生成PATH文件
        try {
            generator.generatePathFile();
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "生成Path模板时，发生异常 e:" + e.getMessage());
        }

        // 第二步：生成GROUP
        try {
            generator.generateGroupFile();
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "生成Group模板时，发生异常 e:" + e.getMessage());
        }
        return false;
    }

    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param bean 路由详细信息，最终实体封装类
     */
    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();

        // 校验
        // @Router注解中的path值，必须要以 / 开头
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@Router注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@Router注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出app作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // @Router注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@Router注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}