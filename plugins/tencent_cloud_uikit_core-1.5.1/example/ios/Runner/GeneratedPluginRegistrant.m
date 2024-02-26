//
//  Generated file. Do not edit.
//

// clang-format off

#import "GeneratedPluginRegistrant.h"

#if __has_include(<tencent_cloud_chat_sdk/TencentImSDKPlugin.h>)
#import <tencent_cloud_chat_sdk/TencentImSDKPlugin.h>
#else
@import tencent_cloud_chat_sdk;
#endif

#if __has_include(<tencent_cloud_uikit_core/TencentCloudUikitCorePlugin.h>)
#import <tencent_cloud_uikit_core/TencentCloudUikitCorePlugin.h>
#else
@import tencent_cloud_uikit_core;
#endif

@implementation GeneratedPluginRegistrant

+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry {
  [TencentImSDKPlugin registerWithRegistrar:[registry registrarForPlugin:@"TencentImSDKPlugin"]];
  [TencentCloudUikitCorePlugin registerWithRegistrar:[registry registrarForPlugin:@"TencentCloudUikitCorePlugin"]];
}

@end
