package com.sxh.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * Parameter注解使用管理器
 */
public class ParameterManager {

    private static final String FILE_SUFFIX_NAME = "$$Parameter";

    //使用LruCache进行缓存
    private final LruCache<String, IParameter> cache;

    protected ParameterManager() {
        cache = new LruCache<>(100);
    }

    /**
     * 页面参数赋值的真正实现
     *
     * @param activity 传入页面activity
     */
    protected void loadParameter(Activity activity) {

        //获取到类名,例如:MainActivity
        String className = activity.getClass().getName();
        //先到缓存里找
        IParameter parameterLoad = cache.get(className);
        if (null == parameterLoad) {
            // 缓存里没有，就通过类加载的方式获取
            try {
                // apt生成类是MainActivity$$Parameter
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                // 因为生成的类都实现了ParameterGet接口，所以可以直接强转
                parameterLoad = (IParameter) aClass.newInstance();
                //放入缓存
                cache.put(className, parameterLoad); // 保存到缓存
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //执行接口方法，会执行我们生成的类
        parameterLoad.getParameter(activity);
    }
}
