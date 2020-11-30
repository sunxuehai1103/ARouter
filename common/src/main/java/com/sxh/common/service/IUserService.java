package com.sxh.common.service;

import com.sxh.arouter_api.ICall;
import com.sxh.common.model.UserInfo;

/**
 * @author : sxh
 * e-mail : 820793721@qq.com
 * @date : 2020/11/28
 * desc   :
 */
public interface IUserService extends ICall {

    UserInfo getUserInfo();

    boolean isUserStudent(int id);

}
