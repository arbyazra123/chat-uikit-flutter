package com.tencent.cloud.uikit.core.utils;

import android.util.Log;

import io.flutter.plugin.common.MethodCall;

public class MethodUtils {

    /**
     * 通用方法，获得参数值，参数可以为null
     */
    public static <T> T getMethodParam(MethodCall methodCall, String param) {
        T parameter = methodCall.argument(param);
        return parameter;
    }
}
