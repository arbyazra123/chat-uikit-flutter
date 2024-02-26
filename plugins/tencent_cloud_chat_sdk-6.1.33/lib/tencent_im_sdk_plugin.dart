import 'package:flutter/widgets.dart';
import 'package:tencent_cloud_chat_sdk/manager/v2_tim_manager.dart';
import 'package:flutter/services.dart';

typedef TencentCloudChatPluginTapFn = bool Function(Map<String, String> data);

/// TencentImSDKPlugin entry
///
class TencentImSDKPlugin {
  static const MethodChannel _channel =
      const MethodChannel('tencent_im_sdk_plugin');

  static V2TIMManager? manager;

  static V2TIMManager managerInstance() {
    if (manager == null) {
      manager = V2TIMManager(_channel);
    }

    return manager!;
  }

  static V2TIMManager v2TIMManager = TencentImSDKPlugin.managerInstance();
}

abstract class TencentCloudChatPlugin {
  Future<Map<String, dynamic>> init(String? data);
  Future<Map<String, dynamic>> unInit(String? data);
  TencentCloudChatPlugin getInstance();
  Future<Map<String, dynamic>> callMethod({
    required String methodName,
    String? data,
  });
  Future<Widget?> getWidget({
    required String methodName,
    Map<String, String>? data,
    Map<String, TencentCloudChatPluginTapFn>? fns,
  });
}
