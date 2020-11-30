package com.sxh.common.model;

import java.io.Serializable;

/**
 * @author : sxh
 * e-mail : 820793721@qq.com
 * @date : 2020/11/28
 * desc   :
 */
public class UserInfo implements Serializable {

    private String name;

    private int sex;

    private boolean isStudent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public void setStudent(boolean student) {
        isStudent = student;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", sex=" + sex +
                ", isStudent=" + isStudent +
                '}';
    }
}
