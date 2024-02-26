package com.qq.qcloud.tencent_im_sdk_plugin.util;

import com.tencent.imsdk.v2.V2TIMMessage;

public abstract class GetMesasgeByIDCallback {
    public GetMesasgeByIDCallback(){};

    public void onSuccess(V2TIMMessage msg){};

    public void onError(int code,String desc){};
}
