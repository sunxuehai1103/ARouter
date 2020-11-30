package com.sxh.arouter_api;

public interface IParameter {

    /**
     * 目标对象.属性名 = getIntent().属性类型... 完成赋值操作
     * @param targetParameter 目标对象：例如：MainActivity中的那些属性
     */
    void getParameter(Object targetParameter);

}
