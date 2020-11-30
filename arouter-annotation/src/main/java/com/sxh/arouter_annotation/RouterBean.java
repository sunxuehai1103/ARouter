package com.sxh.arouter_annotation;

import javax.lang.model.element.Element;

/**
 * 路由路径Path的实体封装类
 * <p>
 * 例如：封装app分组中的MainActivity对象，以及其他一些属性
 */
public class RouterBean {

    //定义一个枚举，方便后期扩展，比如Fragment
    public enum TypeEnum {
        ACTIVITY,
        CALL
    }

    private TypeEnum typeEnum;

    /**
     * 对应类节点 JavaPoet中使用
     */
    private Element element;

    /**
     * 被注解的 Class对象 例如： MainActivity.class  OrderActivity.class
     */
    private Class<?> myClass;

    /**
     * 路由地址  例如：/app/MainActivity
     */
    private String path;

    /**
     * 路由组  例如：app  order  personal
     */
    private String group;

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private RouterBean(TypeEnum typeEnum, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    // 主要是为了方便APT生成代码
    public static RouterBean create(TypeEnum type, Class<?> clazz, String path, String group) {
        return new RouterBean(type, clazz, path, group);
    }

    private RouterBean(Builder builder) {
        this.typeEnum = builder.type;
        this.element = builder.element;
        this.myClass = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }

    /**
     * 构建者模式,方便对象生成
     */
    public static class Builder {

        // 枚举类型：Activity
        private TypeEnum type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由地址
        private String path;
        // 路由组
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        public RouterBean build() {
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path can't be null");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
