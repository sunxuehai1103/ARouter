package com.sxh.arouter_complie;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sxh.arouter_annotation.Parameter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 使用@Parameter注解，APT代码生成器
 */
public class ParameterGenerator {

    private final Messager messager;

    private final Filer filer;

    private final Elements elementTool;

    private final Types typeTool;

    private final Map<TypeElement, List<Element>> parameterMap;

    private ParameterGenerator(Messager messager, Filer filer, Elements elementTool, Types typeTool, Map<TypeElement, List<Element>> parameterMap) {
        this.messager = messager;
        this.filer = filer;
        this.elementTool = elementTool;
        this.parameterMap = parameterMap;
        this.typeTool = typeTool;
    }

    public void generateFile() throws IOException {

//        生成代码模板
//        public class MainActivity$$Parameter implements IParameter {
//            @Override
//            public void getParameter(Object targetParameter) {
//                MainActivity t = (MainActivity) targetParameter;
//                t.name = t.getIntent().getStringExtra("name");
//            }
//        }

        if (ProcessorUtils.isEmpty(parameterMap)) {
            return;
        }

        // Activity类描述
        TypeElement activityType = elementTool.getTypeElement(Config.ACTIVITY_PACKAGE);
        // Serializable类描述
        TypeElement serializableType = elementTool.getTypeElement(Config.SERIALIZABLE);
        // IParameter接口描述
        TypeElement parameterType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_PARAMETER);
        // ICall接口描述
        TypeElement callType = elementTool.getTypeElement(Config.AROUTER_INTERFACE_CALL);

        for (Map.Entry<TypeElement, List<Element>> entry : parameterMap.entrySet()) {

            // key == MainActivity
            TypeElement typeElement = entry.getKey();

            // 如果类名的类型和Activity类型不匹配，直接报错
            if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
                throw new RuntimeException("@Parameter注解目前仅限用于Activity类里");
            }

            ClassName className = ClassName.get(typeElement);

            // 方法参数，即Object targetParameter
            ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Config.PARAMETER_NAME).build();

            // 生成方法名，即public void getParameter(Object targetParameter){}
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(Config.PARAMETER_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(parameterSpec);

            // 生成方法体
            // 添加方法体第一句 MainActivity t = (MainActivity) targetParameter;
            methodSpecBuilder.addStatement("$T t = ($T) " + Config.PARAMETER_NAME, className, className);

            // 循环添加赋值操作，即getIntent().putString()等
            for (Element element : entry.getValue()) {

                // 获取注解使用对象的类型
                TypeMirror typeMirror = element.asType();

                // 获取 TypeKind 枚举类型的序列号
                int type = typeMirror.getKind().ordinal();

                // 获取属性名，比如name  age  sex
                String fieldName = element.getSimpleName().toString();

                // 获取注解的值
                String annotationValue = element.getAnnotation(Parameter.class).name();

                // 判断注解的值为空的情况下的处理
                annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

                // 生成t.name
                String finalValue = "t." + fieldName;

                // 生成t.name = t.getIntent().
                String methodContent = finalValue + " = t.getIntent().";

                // 逐一判断类型，生成不同的内容
                if (type == TypeKind.INT.ordinal()) {
                    // int类型
                    // 生成t.s = t.getIntent().getIntExtra("age", t.age);
                    methodContent += "getIntExtra($S, " + finalValue + ")";  // 有默认值
                    methodSpecBuilder.addStatement(methodContent, annotationValue);
                } else if (type == TypeKind.BOOLEAN.ordinal()) {
                    // boolean类型
                    // 生成t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
                    methodContent += "getBooleanExtra($S, " + finalValue + ")";  // 有默认值
                    methodSpecBuilder.addStatement(methodContent, annotationValue);
                } else {
                    // String类型，TypeKind枚举类型不包含String，需要自己判断
                    // 生成t.s = t.getIntent.getStringExtra("s");
                    if (typeMirror.toString().equalsIgnoreCase(Config.STRING)) {
                        methodContent += "getStringExtra($S)"; // 无默认值
                        methodSpecBuilder.addStatement(methodContent, annotationValue);
                    } else if (typeTool.isSubtype(typeMirror, serializableType.asType())) {
                        // Serializable类型
                        // 生成t.s = (UserInfo) getIntent().getSerializableExtra("userInfo");
                        methodContent = finalValue + "= ($T) t.getIntent().getSerializableExtra($S)";  // 无默认值
                        methodSpecBuilder.addStatement(methodContent, TypeName.get(typeMirror), annotationValue);
                    } else if (typeTool.isSubtype(typeMirror, callType.asType())) {
                        // Call类型，比较特殊
                        // 生成t.s = (IUserInfo)ARouter.getInstance().build("order/getUserInfo")..navigation(t);
                        methodContent = finalValue + " = ($T) $T.getInstance().build($S).navigation(t)";
                        methodSpecBuilder.addStatement(methodContent,
                                TypeName.get(typeMirror),
                                ClassName.get(Config.AROUTER_API_PACKAGE, Config.AROUTER),
                                annotationValue);
                    }
                }
            }

            //最终类名 MainActivity$$Parameter
            String finalClassName = typeElement.getSimpleName() + Config.PARAMETER_FILE_NAME;

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" + className.packageName() + "." + finalClassName);

            JavaFile.builder(className.packageName(),
                    TypeSpec.classBuilder(finalClassName)
                            .addModifiers(Modifier.PUBLIC)
                            .addSuperinterface(ClassName.get(parameterType))  //实现IParameter接口
                            .addMethod(methodSpecBuilder.build())  //方法构建完成
                            .build())  //类构建完成
                    .build()    //JavaFile构建完成
                    .writeTo(filer);
        }
    }


    /**
     * 通过构建者模式创建ParameterGenerator对象
     */
    public static class Builder {

        private Messager messager;

        private Filer filer;

        private Elements elementTool;

        private Types typeTool;

        private Map<TypeElement, List<Element>> parameterMap;

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

        public Builder setTypeTool(Types typeTool) {
            this.typeTool = typeTool;
            return this;
        }

        public Builder setParameterMap(Map<TypeElement, List<Element>> parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public ParameterGenerator build() {
            return new ParameterGenerator(messager, filer, elementTool, typeTool, parameterMap);
        }
    }

}
