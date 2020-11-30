package com.sxh.arouter_complie;

import com.google.auto.service.AutoService;
import com.sxh.arouter_annotation.Parameter;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
//支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Config.AROUTER_PACKAGE})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private final Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> begin process");

        if (ProcessorUtils.isEmpty(annotations)) {
            // 直接结束了
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

        if (ProcessorUtils.isEmpty(elements)) {
            return false;
        }

        //这里element对应的就是加上@Parameter的字段，例如：name、sex
        for (Element element : elements) {
            //注解节点的上一层节点，其实就是类节点，例如：enclosingElement == MainActivity，作为我们map的key
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            //判断map里是否已经具有该key对应的list
            if (tempParameterMap.containsKey(enclosingElement)) {
                tempParameterMap.get(enclosingElement).add(element);
            } else {
                List<Element> fields = new ArrayList<>();
                fields.add(element);
                tempParameterMap.put(enclosingElement, fields);
            }
        }
        //循环执行完毕，说明所有的注解信息都已经存入tempParameterMap中，接下来我们开始正式生成文件了

        ParameterGenerator generator = new ParameterGenerator.Builder()
                .setElementTool(elementTool)
                .setFiler(filer)
                .setMessager(messager)
                .setTypeTool(typeTool)
                .setParameterMap(tempParameterMap)
                .build();

        try {
            generator.generateFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}