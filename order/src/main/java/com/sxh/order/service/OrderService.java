package com.sxh.order.service;

import com.sxh.arouter_annotation.Router;
import com.sxh.common.constant.ARouterPath;
import com.sxh.common.service.IOrderService;
import com.sxh.order.R;

@Router(path = ARouterPath.Order.ORDER_SERVICE_PATH)
public class OrderService implements IOrderService {

    @Override
    public int getOrderCover() {
        return R.drawable.icon_kindergarten;
    }

}
