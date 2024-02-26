package com.qq.qcloud.tencent_im_sdk_plugin;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qq.qcloud.tencent_im_sdk_plugin.manager.ConversationManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.FriendshipManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.GroupManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.MessageManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.OfflinePushManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.SignalingManager;
import com.qq.qcloud.tencent_im_sdk_plugin.manager.TimManager;
import com.qq.qcloud.tencent_im_sdk_plugin.util.CommonUtil;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * tencent_im_sdk_plugin
 */
public class
tencent_im_sdk_plugin implements FlutterPlugin, MethodChannel.MethodCallHandler {
    /**
     * log signature
     */
    public static String TAG = "tencent_im_sdk_plugin";

    /**
     * global context
     */
    public static Context context;

    /**
     * Communication pipeline with Flutter
     */
    private static List<MethodChannel> channels = new LinkedList<>();
    private static MessageManager messageManager;
    private static GroupManager groupManager;
    private static SignalingManager signalingManager;
    private static ConversationManager conversationManager;
    private static FriendshipManager friendshipManager;
    private static OfflinePushManager offlinePushManager;
    public static TimManager timManager;

    private static List<String> Plugins = new LinkedList<String>();


    private static Application mApplication;
    public tencent_im_sdk_plugin() {
    }

    private tencent_im_sdk_plugin(Context context, MethodChannel channel) {
        tencent_im_sdk_plugin.context = context;
        tencent_im_sdk_plugin.channels.add(channel);
        tencent_im_sdk_plugin.messageManager = new MessageManager(channel);
        tencent_im_sdk_plugin.groupManager = new GroupManager(channel);
        tencent_im_sdk_plugin.signalingManager = new SignalingManager(channel);
        tencent_im_sdk_plugin.conversationManager = new ConversationManager(channel);
        tencent_im_sdk_plugin.friendshipManager = new FriendshipManager(channel);
        tencent_im_sdk_plugin.offlinePushManager = new OfflinePushManager(channel);
        tencent_im_sdk_plugin.timManager = new TimManager(channel, context);
        CommonUtil.context = context;
//        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.mask;

    }

    @Override
    public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        Log.i(TAG, "onAttachedToEngine");
        MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tencent_im_sdk_plugin");
        mApplication = (Application) flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(new tencent_im_sdk_plugin(mApplication.getApplicationContext(), channel));
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.

    @Override
    public void onMethodCall(MethodCall call, Result result) {

        String TIMManagerName = CommonUtil.getParam(call, result, "TIMManagerName");
        // String trace = CommonUtil.getParam(call, result, "stacktrace");
        // if(!CommonUtil.isValidateTrace(trace)){
        //     result.error("-10000","using the api in the wrong context","");
        //     return;
        // }
        // List<String> list =  CommonUtil.checkApiContains(trace);

        // boolean needCheckPlugins = false;
         String apiname = call.method;
        // boolean pParamAvalible = false;
//        if(apiname.equals("initSDK")){
//            List<String> plugins = call.argument("plugins");
//
//            if(!plugins.isEmpty()){
//                needCheckPlugins = true;
//
//                for (int i = 0; i < plugins.size(); i++) {
//                    String pindex = plugins.get(i);
//
//                    Plugins.add(pindex);
//                }
//            }
//        }
//        if(list.size() == Plugins.size()){
//            int counter = 0;
//            for (int i = 0; i < list.size(); i++) {
//                if(Plugins.contains(list.get(i))){
//                    counter++;
//                }
//            }
//            if(counter == list.size()){
//                pParamAvalible = true;
//            }
//        }
//        if(!pParamAvalible){
//            result.error("-10001","The plugins parameter of the initSDK API is incorrect or missing","");
//            return;
//        }
//        if(needCheckPlugins){
//            for (int i = 0; i < Plugins.size(); i++) {
//                List<String> lists =  CommonUtil.getCheckList(Plugins.get(i));
//                if(lists.contains(apiname)){
//
//                }
//            }
//        }
        
        Field field = null;
        Method method = null;
        try {
            field = tencent_im_sdk_plugin.class.getDeclaredField(TIMManagerName);
            method = field.get(new Object()).getClass().getDeclaredMethod(apiname, MethodCall.class, Result.class);
            method.invoke(field.get(new Object()), call, result);
            try {
                call.<HashMap<String,Object>>arguments().remove("stacktrace");
                call.<HashMap<String,Object>>arguments().put("method",apiname);
                CommonUtil.writeLog(call.<HashMap<String,Object>>arguments().toString(),false);
            }catch (Exception e){
                System.out.println("print log error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.writeLog("flutter invoke native method fail "+e.toString(),false);
        }
    }


    @Override
    public  void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.i(TAG, "onDetachedFromEngine");
       for (MethodChannel channel : channels) {
           channel.setMethodCallHandler(null);
       }
       channels = new LinkedList<>();
       MessageManager.cleanChannels();
       TimManager.cleanChannels();
       GroupManager.cleanChannels();
       OfflinePushManager.cleanChannels();
       FriendshipManager.cleanChannels();
       SignalingManager.cleanChannels();
       ConversationManager.cleanChannels();
        // 为了适配flutter多引擎开发模式，这里不在onDetachedFromEngine生命周期把chanel移除
    }

}
