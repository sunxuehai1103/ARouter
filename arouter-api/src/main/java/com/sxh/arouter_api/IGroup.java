package com.sxh.arouter_api;

import java.util.Map;

/**
 * Group对应的数据结构
 */
public interface IGroup {

    /**
     * key -> 用户注解里group字段对应的字符串
     * value -> group下所有的ARouterPath对象
     * <p>
     * 例如：
     * key 为 "order"
     * value 为 order组下所有的ARouterPath的实现类（APT生成出来的 ARouter$$Path$$order）
     */
    Map<String, Class<? extends IPath>> getGroupMap();

}
