package com.sxh.personal.service;

import com.sxh.arouter_annotation.Router;
import com.sxh.common.constant.ARouterPath;
import com.sxh.common.service.IUserService;
import com.sxh.common.model.UserInfo;

@Router(path = ARouterPath.Personal.PERSONAL_SERVICE_PATH)
public class UserService implements IUserService {

    @Override
    public UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("小雯");
        userInfo.setSex(2);
        userInfo.setStudent(false);
        return userInfo;
    }

    @Override
    public boolean isUserStudent(int id) {
        return id < 10000;
    }

}
