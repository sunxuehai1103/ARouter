package com.sxh.arouter_complie;

/**
 * 常量定义
 */
public interface Config {

    /*******************************   包名、全类名   ***************************************/
    // ARouter类名
    String AROUTER = "ARouter";
    // @Router的全类名
    String AROUTER_PACKAGE = "com.sxh.arouter_annotation.Router";
    // ARouter api 包名
    String AROUTER_API_PACKAGE = "com.sxh.arouter_api";
    // APT生成的代码存放的包名
    String APT_PACKAGE = "com.sxh.arouter.apt";
    // String全类名
    String STRING = "java.lang.String";
    // Activity全类名
    String ACTIVITY_PACKAGE = "android.app.Activity";
    // Serializable全类名
    String SERIALIZABLE = "java.io.Serializable";


    /*******************************   参数   ***************************************/
    // 接收参数的TAG标记
    String MODULE_NAME = "moduleName";

    // Group文件里面的方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // Group文件里面的参数名
    String GROUP_VAR = "groupMap";

    // Group最终要生成的文件名前缀
    String GROUP_FILE_NAME = "ARouter$$Group$$";

    // Path文件里面的方法名
    String PATH_METHOD_NAME = "getPathMap";

    // Path文件里面的参数名
    String PATH_VAR = "pathMap";

    // Path最终要生成的文件名前缀
    String PATH_FILE_NAME = "ARouter$$Path$$";

    // Parameter文件里面的方法名
    String PARAMETER_METHOD_NAME = "getParameter";

    // Parameter文件里面的参数名
    String PARAMETER_NAME = "targetParameter";

    // Parameter最终要生成的文件名后缀
    String PARAMETER_FILE_NAME = "$$Parameter";


    /*******************************   接口标准   ***************************************/
    // Group文件的接口标准
    String AROUTER_INTERFACE_GROUP = AROUTER_API_PACKAGE + ".IGroup";

    // Path文件的接口标准
    String AROUTER_INTERFACE_PATH = AROUTER_API_PACKAGE + ".IPath";

    // Parameter文件的接口标准
    String AROUTER_INTERFACE_PARAMETER = AROUTER_API_PACKAGE + ".IParameter";

    // Parameter文件里的服务接口标准
    String AROUTER_INTERFACE_CALL = AROUTER_API_PACKAGE + ".ICall";
}
