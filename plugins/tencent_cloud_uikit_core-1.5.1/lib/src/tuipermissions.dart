import 'package:tencent_cloud_uikit_core/platform/tuicore_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/src/tuicore_define.dart';

class TUIPermission {
  static Future<bool> has({required List<TUIPermissions> permissions}) async {
    return await TUICorePlatform.instance
        .hasPermissions(permissions: permissions);
  }

  static Future<TUIPermissionResult> request(
      {required List<TUIPermissions> permissions,
      String title = '',
      String description = '',
      String settingsTip = ''}) async {
    return await TUICorePlatform.instance.requestPermissions(
        permissions: permissions,
        title: title,
        description: description,
        settingsTip: settingsTip);
  }
}
