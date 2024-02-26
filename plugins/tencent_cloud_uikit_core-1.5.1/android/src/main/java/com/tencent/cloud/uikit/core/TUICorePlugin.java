package com.tencent.cloud.uikit.core;

import android.Manifest;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.qcloud.tuicore.TUIConstants;
import com.tencent.cloud.uikit.core.utils.MethodUtils;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.interfaces.ITUIService;
import com.tencent.qcloud.tuicore.interfaces.TUICallback;
import com.tencent.qcloud.tuicore.permission.PermissionCallback;
import com.tencent.qcloud.tuicore.permission.PermissionRequester;
import com.tencent.qcloud.tuicore.util.ToastUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class TUICorePlugin implements FlutterPlugin, MethodCallHandler {

    private static final String TAG                      = "TUICorePlugin";
    private static final int    ERROR_CODE_NULL_PARAM    = -1001;
    private static final int    ERROR_CODE_INVALID_PARAM = -1002;

    private MethodChannel mMethodChannel;
    private Context       mContext;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        mMethodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tuicore");
        mMethodChannel.setMethodCallHandler(this);
        mContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Log.i(TAG, "onMethodCall -> method:" + call.method + ", arguments:" + call.arguments);
        try {
            Method method = TUICorePlugin.class.getDeclaredMethod(call.method, MethodCall.class,
                    MethodChannel.Result.class);
            method.invoke(this, call, result);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "onMethodCall |method=" + call.method + "|arguments=" + call.arguments + "|error=" + e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "onMethodCall |method=" + call.method + "|arguments=" + call.arguments + "|error=" + e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "onMethodCall |method=" + call.method + "|arguments=" + call.arguments + "|error=" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        mMethodChannel.setMethodCallHandler(null);
    }

    public void getService(MethodCall call, MethodChannel.Result result) {
        String serviceName = MethodUtils.getMethodParam(call, "serviceName");
        if (TextUtils.isEmpty(serviceName)) {
            result.error("" + ERROR_CODE_NULL_PARAM, "getService serviceName is null", "");
            return;
        }

        ITUIService service = TUICore.getService(serviceName);
        if (service != null) {
            result.success(true);
        } else {
            result.success(false);
        }
    }

    public void callService(MethodCall call, MethodChannel.Result result) {
        String serviceName = MethodUtils.getMethodParam(call, "serviceName");
        if (TextUtils.isEmpty(serviceName)) {
            result.error("" + ERROR_CODE_NULL_PARAM, "callService serviceName is null", "");
            return;
        }

        String method = MethodUtils.getMethodParam(call, "method");
        if (TextUtils.isEmpty(method)) {
            result.error("" + ERROR_CODE_NULL_PARAM, "callService method is null", "");
            return;
        }
        Map map = MethodUtils.getMethodParam(call, "param");
        checkAndConvertParameter(map);
        TUICore.callService(serviceName, method, map);
    }

    private void checkAndConvertParameter(Map map) {
        if (map != null && map.containsKey(TUIConstants.TUICalling.PARAM_NAME_USERIDS)) {
            List<String> userIdList = (ArrayList) map.get(TUIConstants.TUICalling.PARAM_NAME_USERIDS);
            if (userIdList != null && userIdList.size() > 0) {
                String[] userIdArray = new String[userIdList.size()];
                for (int i = 0; i < userIdList.size(); i++) {
                    userIdArray[i] = userIdList.get(i);
                }
                map.remove(TUIConstants.TUICalling.PARAM_NAME_USERIDS);
                map.put(TUIConstants.TUICalling.PARAM_NAME_USERIDS, userIdArray);
            }
        }
    }

    public void login(MethodCall call, MethodChannel.Result result) {
        int sdkAppId = MethodUtils.getMethodParam(call, "sdkAppId");
        if (sdkAppId <= 0) {
            result.error("" + ERROR_CODE_INVALID_PARAM, "login sdkappid is invalid", "");
        }

        String userId = MethodUtils.getMethodParam(call, "userId");
        if (TextUtils.isEmpty(userId)) {
            result.error("" + ERROR_CODE_NULL_PARAM, "login userId is null", "");
            return;
        }

        String userSig = MethodUtils.getMethodParam(call, "userSig");
        if (TextUtils.isEmpty(userSig)) {
            result.error("" + ERROR_CODE_NULL_PARAM, "login userSig is null", "");
            return;
        }

        TUILogin.login(mContext, sdkAppId, userId, userSig, new TUICallback() {
            @Override
            public void onSuccess() {
                result.success(0);
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "reject Error{ code:" + code + ",message:" + message + "}");
                result.error("" + code, message, "");
            }
        });
    }

    public void logout(MethodCall call, MethodChannel.Result result) {
        TUILogin.logout(new TUICallback() {
            @Override
            public void onSuccess() {
                result.success(0);
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "reject Error{ code:" + code + ",message:" + message + "}");
                result.error("" + code, message, "");
            }
        });
    }

    public void showToast(MethodCall call, MethodChannel.Result result) {
        String content = MethodUtils.getMethodParam(call, "content");
        if (TextUtils.isEmpty(content)) {
            result.error("" + ERROR_CODE_INVALID_PARAM, "content is empty", "");
            return;
        }
        int durationIndex = MethodUtils.getMethodParam(call, "duration");
        boolean duration = getDurationByIndex(durationIndex);
        int gravityIndex = MethodUtils.getMethodParam(call, "gravity");
        int gravity = getGravityByIndex(gravityIndex);

        ToastUtil.show(content, duration, gravity);
    }

    public void hasPermissions(MethodCall call, MethodChannel.Result result) {
        List<Integer> permissionsList = MethodUtils.getMethodParam(call, "permission");
        String[] strings = new String[permissionsList.size()];
        for (int i = 0; i < permissionsList.size(); i++) {
            strings[i] = getPermissionsByIndex(permissionsList.get(i));
        }
        boolean isGranted = com.tencent.qcloud.tuicore.util.PermissionRequester.isGranted(strings);
        result.success(isGranted);
    }

    public void requestPermissions(MethodCall call, MethodChannel.Result result) {
        List<Integer> permissionsList = MethodUtils.getMethodParam(call, "permission");
        String[] permissions = new String[permissionsList.size()];
        for (int i = 0; i < permissionsList.size(); i++) {
            permissions[i] = getPermissionsByIndex(permissionsList.get(i));
        }
        String title = MethodUtils.getMethodParam(call, "title");
        String description = MethodUtils.getMethodParam(call, "description");
        String settingsTip = MethodUtils.getMethodParam(call, "settingsTip");
        PermissionCallback callback = new PermissionCallback() {
            @Override
            public void onGranted() {
                result.success(PermissionRequester.Result.Granted.ordinal());
            }

            @Override
            public void onDenied() {
                result.success(PermissionRequester.Result.Denied.ordinal());
            }

            @Override
            public void onRequesting() {
                result.success(PermissionRequester.Result.Requesting.ordinal());
            }
        };

        PermissionRequester.newInstance(permissions)
                .title(title)
                .description(description)
                .settingsTip(settingsTip)
                .callback(callback)
                .request();
    }

    private String getPermissionsByIndex(int index) {
        switch (index) {
            case 0:
                return Manifest.permission.CAMERA;
            case 1:
                return Manifest.permission.RECORD_AUDIO;
            case 2:
                return Manifest.permission.BLUETOOTH_CONNECT;
            default:
                return "";
        }
    }

    private int getGravityByIndex(int gravityIndex) {
        switch (gravityIndex) {
            case 0:
                return Gravity.TOP;
            case 1:
                return Gravity.BOTTOM;
            case 2:
                return Gravity.CENTER;

            default:
                return Gravity.BOTTOM;
        }
    }

    private boolean getDurationByIndex(int durationIndex) {
        if (durationIndex == Toast.LENGTH_LONG) {
            return true;
        } else {
            return false;
        }
    }
}

