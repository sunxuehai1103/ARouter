package com.sxh.arouter_api;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 用于跳转时参数的传递
 */
public class BundleManager {

    private Bundle bundle = new Bundle();

    private ICall call;

    public ICall getCall() {
        return call;
    }

    public void setCall(ICall call) {
        this.call = call;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withSerializable(@NonNull String key, @Nullable Serializable value) {
        bundle.putSerializable(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context) {
        return ARouter.getInstance().navigation(context, this);
    }
}
