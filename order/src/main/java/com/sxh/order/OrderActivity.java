package com.sxh.order;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sxh.arouter_annotation.Parameter;
import com.sxh.arouter_annotation.Router;
import com.sxh.arouter_api.ARouter;
import com.sxh.common.constant.ARouterPath;
import com.sxh.common.model.UserInfo;
import com.sxh.common.service.IUserService;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@Router(path = ARouterPath.Order.ORDER_ACTIVITY_PATH, group = ARouterPath.Order.GROUP)
public class OrderActivity extends Activity {

    @Parameter(name = ARouterPath.Personal.PERSONAL_SERVICE_PATH)
    IUserService userService;

    @Parameter(name = "userInfo")
    UserInfo userInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ARouter.getInstance().inject(this);

        Log.d("OrderActivity", "userInfo:" + userService.getUserInfo().toString());

        Log.d("OrderActivity", "isUserStudent:" + userService.isUserStudent(10000));

        Log.d("OrderActivity", "userInfo2:" + userInfo.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void gotoPersonal(View view) {
        ARouter.getInstance().build(ARouterPath.Personal.PERSONAL_ACTIVITY_PATH)
                .withInt("sex", 1)
                .withString("name", "小海")
                .withBoolean("isStudent", false)
                .navigation(this);
    }
}
