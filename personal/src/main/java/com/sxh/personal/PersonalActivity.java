package com.sxh.personal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sxh.arouter_annotation.Parameter;
import com.sxh.arouter_annotation.Router;
import com.sxh.arouter_api.ARouter;
import com.sxh.common.constant.ARouterPath;

import androidx.annotation.Nullable;

@Router(path = ARouterPath.Personal.PERSONAL_ACTIVITY_PATH)
public class PersonalActivity extends Activity {

    @Parameter(name = "name")
    String name;

    @Parameter(name = "sex")
    int sex;

    @Parameter(name = "isStudent")
    boolean isStudent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        ARouter.getInstance().inject(this);

        Log.d("PersonalActivity", "name:" + name + "  sex:" + sex + "  isStudent:" + isStudent);
    }
}
