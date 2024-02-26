import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/platform/tuicore_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/src/tuicallback.dart';

class TUILogin extends PlatformInterface {
  TUILogin() : super(token: _token);

  static final Object _token = Object();

  static TUILogin _instance = TUILogin();

  static TUILogin get instance => _instance;

  static set instance(TUILogin instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// login IM
  ///
  /// @param sdkAppId      sdkAppId
  /// @param userId        userId
  /// @param userSig       userSig
  Future<void> login(
      int sdkAppId, String userId, String userSig, TUICallback callback) async {
    await TUICorePlatform.instance.login(sdkAppId, userId, userSig, callback);
  }

  /// logout IM
  ///
  Future<void> logout(TUICallback callback) async {
    await TUICorePlatform.instance.logout(callback);
  }
}
