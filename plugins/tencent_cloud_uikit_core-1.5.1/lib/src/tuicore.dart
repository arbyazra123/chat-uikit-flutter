import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/platform/tuicore_platform_interface.dart';

class TUICore extends PlatformInterface {
  TUICore() : super(token: _token);

  static final Object _token = Object();

  static TUICore _instance = TUICore();

  static TUICore get instance => _instance;

  static set instance(TUICore instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// check Service is exist
  ///
  /// @param serviceName      serviceName
  Future<bool> getService(String serviceName) async {
    return await TUICorePlatform.instance.getService(serviceName);
  }

  /// call Service method
  ///
  /// @param serviceName      service name
  /// @param method           method name
  /// @param param            method param
  ///
  /// for example:
  ///
  /// use callKit single video call（groupId is "" meaning sing call）：
  /// callService(TUICALLKIT_SERVICE_NAME, METHOD_NAME_CALL, {
  ///                         PARAM_NAME_TYPE: TYPE_VIDEO,
  ///                         PARAM_NAME_USERIDS: ["111"],
  ///                         PARAM_NAME_GROUPID: ""
  ///                       });
  ///
  /// use callKit group video call（groupId is not "" meaning sing call）：
  /// callService(TUICALLKIT_SERVICE_NAME, METHOD_NAME_CALL, {
  ///                         PARAM_NAME_TYPE: TYPE_VIDEO,
  ///                         PARAM_NAME_USERIDS: ["111","222","333"],
  ///                         PARAM_NAME_GROUPID: "1234"
  ///                       });
  ///
  /// use callKit and set enable floating window:
  /// callService(TUICALLKIT_SERVICE_NAME, METHOD_NAME_ENABLE_FLOAT_WINDOW, {
  ///                         PARAM_NAME_ENABLE_FLOAT_WINDOW: true
  ///                       });
  ///
  /// the method name and method param please refer to tuicore_define.dart
  Future<void> callService(
      String serviceName, String method, Map<String, Object> param) async {
    await TUICorePlatform.instance.callService(serviceName, method, param);
  }
}
