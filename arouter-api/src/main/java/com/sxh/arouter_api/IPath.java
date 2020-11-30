package com.sxh.arouter_api;

import com.sxh.arouter_annotation.RouterBean;

import java.util.Map;

/**
 * Path对应的数据结构
 */
public interface IPath {

    /**
     * key -> 用户注解里path字段对应的字符串
     * value -> path对应的RouterBean对象，这里RouterBean其实就是对xxx.class对象的封装
     * <p>
     * 例如：
     * key 为 "/order/Order_MainActivity"
     * value 为 RouterBean==Order_MainActivity.class
     */
    Map<String, RouterBean> getPathMap();

}
