import 'package:tencent_cloud_uikit_core/platform/tuicore_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/src/tuicore_define.dart';

class TUIToast {
  static Future<void> show(
      {required String content,
      TUIDuration duration = TUIDuration.short,
      TUIGravity gravity = TUIGravity.bottom}) async {
    return await TUICorePlatform.instance.showToast(content, duration, gravity);
  }
}
