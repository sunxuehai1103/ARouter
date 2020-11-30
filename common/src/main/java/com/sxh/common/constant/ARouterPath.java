package com.sxh.common.constant;

/**
 * 路由Path管理
 */
public interface ARouterPath {

    String FLAG_DIVIDE = "/";

    interface App {

        String GROUP = "app";

        String MAIN_ACTIVITY_PATH = FLAG_DIVIDE + GROUP + FLAG_DIVIDE + "MainActivity";

    }

    interface Personal {

        String GROUP = "personal";

        String PERSONAL_ACTIVITY_PATH = FLAG_DIVIDE + GROUP + FLAG_DIVIDE + "PersonalActivity";

        String PERSONAL_SERVICE_PATH = FLAG_DIVIDE + GROUP + FLAG_DIVIDE + "PersonService";

    }

    interface Order {

        String GROUP = "order";

        String ORDER_ACTIVITY_PATH = FLAG_DIVIDE + GROUP + FLAG_DIVIDE + "OrderActivity";

        String ORDER_SERVICE_PATH = FLAG_DIVIDE + GROUP + FLAG_DIVIDE + "OrderService";

    }

}
