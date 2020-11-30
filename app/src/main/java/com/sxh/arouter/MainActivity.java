package com.sxh.arouter;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.sxh.arouter_annotation.Parameter;
import com.sxh.arouter_annotation.Router;
import com.sxh.arouter_api.ARouter;
import com.sxh.common.constant.ARouterPath;
import com.sxh.common.model.UserInfo;
import com.sxh.common.service.IOrderService;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@Router(path = ARouterPath.App.MAIN_ACTIVITY_PATH, group = ARouterPath.App.GROUP)
public class MainActivity extends AppCompatActivity {

    @Parameter(name = ARouterPath.Order.ORDER_SERVICE_PATH)
    IOrderService orderService;

    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ARouter.getInstance().inject(this);

        ImageView ivCover = findViewById(R.id.iv_cover);
        ivCover.setImageResource(orderService.getOrderCover());

        userInfo = new UserInfo();
        userInfo.setName("奥特曼");
        userInfo.setSex(1);
        userInfo.setStudent(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void gotoOrder(View view) {
        ARouter.getInstance().build(ARouterPath.Order.ORDER_ACTIVITY_PATH)
                .withInt("sex", 1)
                .withString("name", "小海")
                .withBoolean("isStudent", false)
                .withSerializable("userInfo", userInfo)
                .navigation(this);
    }

}