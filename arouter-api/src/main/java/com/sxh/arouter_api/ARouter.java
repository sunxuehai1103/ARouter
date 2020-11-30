package com.sxh.arouter_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.sxh.arouter_annotation.RouterBean;

import androidx.annotation.RequiresApi;

/**
 * ARoute注解使用管理器
 */
public class ARouter {

    public String TAG = "RouterManager";

    private static ARouter instance;
    private final ParameterManager parameterManager;

    public static ARouter getInstance() {
        if (instance == null) {
            synchronized (ARouter.class) {
                if (instance == null) {
                    instance = new ARouter();
                }
            }
        }
        return instance;
    }

    private ARouter() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);

        parameterManager = new ParameterManager();
    }

    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

    // 路由的组名
    private String group;
    // 路由的路径
    private String path;

    //Group缓存
    private LruCache<String, IGroup> groupLruCache;
    //Path缓存
    private LruCache<String, IPath> pathLruCache;

    /**
     * 构建路由
     *
     * @param path
     * @return
     */
    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("path格式不合法：如 /app/MainActivity");
        }

        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("path格式不合法：如 /app/MainActivity");
        }

        // 截取组名,获得到group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path格式不合法：如 /app/MainActivity");
        }

        this.path = path;
        this.group = finalGroup;
        return new BundleManager();
    }

    /**
     * 触发路由
     *
     * @param context
     * @param bundleManager
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context, BundleManager bundleManager) {

        String groupClassName = context.getPackageName() + ".apt." + FILE_GROUP_NAME + group;

        Log.d(TAG, "navigation: groupClassName=" + groupClassName);

        try {
            //第一步: 从Group缓存中寻找Group类文件
            IGroup loadGroup = groupLruCache.get(group);
            if (null == loadGroup) {
                // 如果缓存里没有，则通过类加载的方式获取
                Class<?> aClass = Class.forName(groupClassName);
                // 初始化类文件
                loadGroup = (IGroup) aClass.newInstance();

                // 保存到缓存
                groupLruCache.put(group, loadGroup);
            }

            if (loadGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("路由出错"); // Group这个类 加载失败
            }

            //第二步：从Path缓存中寻找Path类文件
            IPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                // 如果缓存里没有，则通过类加载的方式
                Class<? extends IPath> clazz = loadGroup.getGroupMap().get(group);
                loadPath = clazz.newInstance();
                // 放入缓存
                pathLruCache.put(path, loadPath);
            }

            //第三步：进行跳转
            if (loadPath != null) {
                if (loadPath.getPathMap().isEmpty()) {
                    throw new RuntimeException("路由出错");
                }

                RouterBean routerBean = loadPath.getPathMap().get(path);

                if (routerBean != null) {
                    if (routerBean.getTypeEnum() == RouterBean.TypeEnum.ACTIVITY) {
                        Intent intent = new Intent(context, routerBean.getMyClass());
                        intent.putExtras(bundleManager.getBundle());
                        context.startActivity(intent);
                    } else if (routerBean.getTypeEnum() == RouterBean.TypeEnum.CALL) {
                        Class<?> myClass = routerBean.getMyClass();
                        ICall call = (ICall) myClass.newInstance();
                        bundleManager.setCall(call);
                        return call;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注入参数
     *
     * @param activity 页面activity
     */
    public void inject(Activity activity) {
        parameterManager.loadParameter(activity);
    }
}
