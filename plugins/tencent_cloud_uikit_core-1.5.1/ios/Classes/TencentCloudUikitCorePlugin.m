#import "TencentCloudUikitCorePlugin.h"
#if __has_include(<tencent_cloud_uikit_core/tencent_cloud_uikit_core-Swift.h>)
#import <tencent_cloud_uikit_core/tencent_cloud_uikit_core-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tencent_cloud_uikit_core-Swift.h"
#endif

@implementation TencentCloudUikitCorePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTencentCloudUikitCorePlugin registerWithRegistrar:registrar];
}
@end
