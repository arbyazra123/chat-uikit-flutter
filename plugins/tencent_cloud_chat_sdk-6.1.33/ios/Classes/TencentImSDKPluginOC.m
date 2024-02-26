// HelloPlugin.m
// 
// Created by xingchenhe on 2020/11/19
// Copyright (c) 2020年 Tencent. All rights reserved.
//
#import "TencentImSDKPluginOC.h"
#if __has_include(<tencent_cloud_chat_sdk/tencent_cloud_chat_sdk-Swift.h>)
#import <tencent_cloud_chat_sdk/tencent_cloud_chat_sdk-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tencent_cloud_chat_sdk-Swift.h"
#endif
/*
  HelloPlugin
 */
@implementation TencentImSDKPluginOC
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [TencentImSDKPlugin registerWithRegistrar:registrar];
}
@end
