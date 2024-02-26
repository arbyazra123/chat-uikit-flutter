import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:tencent_cloud_uikit_core/src/tuicallback.dart';
import 'package:tencent_cloud_uikit_core/platform/tuicore_platform_interface.dart';
import 'package:tencent_cloud_uikit_core/src/tuicore_define.dart';

/// An implementation of [TUICorePlatform] that uses method channels.
class MethodChannelTUICore extends TUICorePlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('tuicore');

  @override
  Future<bool> getService(String serviceName) async {
    return await methodChannel
        .invokeMethod('getService', {'serviceName': serviceName});
  }

  @override
  Future<void> callService(
      String serviceName, String method, Map<String, Object> param) async {
    await methodChannel.invokeMethod('callService',
        {'serviceName': serviceName, 'method': method, 'param': param});
  }

  @override
  Future<void> login(
      int sdkAppId, String userId, String userSig, TUICallback callback) async {
    try {
      await methodChannel.invokeMethod('login',
          {'sdkAppId': sdkAppId, 'userId': userId, 'userSig': userSig});
    } on PlatformException catch (error) {
      return callback.onError!(int.fromEnvironment(error.code), error.message!);
    } on Exception catch (error) {
      return callback.onError!(-1, error.toString());
    }
    callback.onSuccess!();
  }

  @override
  Future<void> logout(TUICallback callback) async {
    try {
      await methodChannel.invokeMethod('logout');
    } on PlatformException catch (error) {
      return callback.onError!(int.fromEnvironment(error.code), error.message!);
    } on Exception catch (error) {
      return callback.onError!(-1, error.toString());
    }
    callback.onSuccess!();
  }

  @override
  Future<bool> hasPermissions(
      {required List<TUIPermissions> permissions}) async {
    List<int> permissionsList = [];
    for (var element in permissions) {
      permissionsList.add(element.index);
    }
    return await methodChannel
        .invokeMethod('hasPermissions', {'permission': permissionsList});
  }

  @override
  Future<TUIPermissionResult> requestPermissions(
      {required List<TUIPermissions> permissions,
      String title = "",
      String description = "",
      String settingsTip = ""}) async {
    try {
      List<int> permissionsList = [];
      for (var element in permissions) {
        permissionsList.add(element.index);
      }
      int result = await methodChannel.invokeMethod('requestPermissions', {
        'permission': permissionsList,
        'title': title,
        'description': description,
        'settingsTip': settingsTip
      });
      if (result == TUIPermissionResult.granted.index) {
        return TUIPermissionResult.granted;
      } else if (result == TUIPermissionResult.denied.index) {
        return TUIPermissionResult.denied;
      } else {
        return TUIPermissionResult.requesting;
      }
    } on PlatformException catch (_) {
      return TUIPermissionResult.denied;
    } on Exception catch (_) {
      return TUIPermissionResult.denied;
    }
  }

  @override
  Future<void> showToast(
      String content, TUIDuration duration, TUIGravity gravity) async {
    await methodChannel.invokeMethod('showToast', {
      'content': content,
      'duration': duration.index,
      'gravity': gravity.index
    });
  }
}
