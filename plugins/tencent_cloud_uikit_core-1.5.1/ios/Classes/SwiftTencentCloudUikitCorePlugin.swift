import Flutter
import UIKit
import TUICore

public class SwiftTencentCloudUikitCorePlugin: NSObject, FlutterPlugin {

    static let channelName = "tuicore"
    private let channel: FlutterMethodChannel
    let registrar: FlutterPluginRegistrar
    var messager: FlutterBinaryMessenger {
       return registrar.messenger()
    }

    init(registrar: FlutterPluginRegistrar) {
       self.channel = FlutterMethodChannel(name: SwiftTencentCloudUikitCorePlugin.channelName, binaryMessenger: registrar.messenger())
       self.registrar = registrar

       super.init()
    }


    public static func register(with registrar: FlutterPluginRegistrar) {
       let instance = SwiftTencentCloudUikitCorePlugin.init(registrar: registrar)
       registrar.addMethodCallDelegate(instance, channel: instance.channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

    switch call.method {
    case "getService":
        getService(call, result: result)
        break

    case "callService":
        callService(call, result: result)
        break

    case "login":
        login(call, result: result)
        break

    case "logout":
        logout(call, result: result)
        break

    case "showToast":
        showToast(call, result: result)


    default:
    break;
    }
    }

    public func getService(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let serviceName = MethodUtils.getMethodParams(call: call, key: "serviceName", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "getService", paramKey: "serviceName", result: result)
            return
        }

        if serviceName == FlutterCallKitServiceName {
            let output: TUIServiceProtocol? = TUICore.getService(IOSCallKitServiceName)
            if output != nil {
                result(NSNumber(booleanLiteral: true))
            } else {
                result(NSNumber(booleanLiteral: false))
            }
        } else {
            result(NSNumber(booleanLiteral: false))
        }
    }

    public func callService(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let serviceName = MethodUtils.getMethodParams(call: call, key: "serviceName", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "callService", paramKey: "serviceName", result: result)
            return
        }
        let name = (serviceName == FlutterCallKitServiceName) ? IOSCallKitServiceName : serviceName

        guard let method = MethodUtils.getMethodParams(call: call, key: "method", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "callService", paramKey: "method", result: result)
            return
        }
        var methodName = ""
        switch method {
            case "methodEnableFloatWindow":
                methodName = TUICore_TUICallingService_EnableFloatWindowMethod
            case "call":
                methodName = TUICore_TUICallingService_ShowCallingViewMethod
            default:
                break
        }

        var callParam: Dictionary<String, Any> = Dictionary()
        if let paramsDic = MethodUtils.getMethodParams(call: call, key: "param", resultType: Dictionary<String, Any>.self) {
            for param in paramsDic {
                var key: String = ""
                var value: Any?

                switch param.key {
                ///floatWindow
                case "enableFloatWindow":
                    key = TUICore_TUICallingService_EnableFloatWindowMethod_EnableFloatWindow
                    guard let enable = param.value as? Bool else { return }
                    value = enable
                /// call
                case "userIDs":
                    key = TUICore_TUICallingService_ShowCallingViewMethod_UserIDsKey
                    guard let userIDs = param.value as? Array<String> else { return }
                    value = userIDs

                case "groupId":
                    key = TUICore_TUICallingService_ShowCallingViewMethod_GroupIDKey
                    guard let groupId = param.value as? String else { return }
                    value = groupId == "" ? nil : groupId

                case "type":
                    key = TUICore_TUICallingService_ShowCallingViewMethod_CallTypeKey
                    guard let type = param.value as? String else { return }
                    if type == "audio" {
                        value = CallMediaType.audio.rawValue
                    } else if type == "video" {
                        value = CallMediaType.video.rawValue
                    } else {
                        value = CallMediaType.unknown.rawValue
                    }

                default:
                    break
                }

                callParam[key] = value
            }
        }

        TUICore.callService(name, method: methodName, param: callParam)
    }

    public func login(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let sdkAppId = MethodUtils.getMethodParams(call: call, key: "sdkAppId", resultType: Int32.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "login", paramKey: "sdkAppId", result: result)
            return
        }

        guard let userId = MethodUtils.getMethodParams(call: call, key: "userId", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "login", paramKey: "userId", result: result)
            return
        }

        guard let userSig = MethodUtils.getMethodParams(call: call, key: "userSig", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "login", paramKey:  "userSig", result: result)
            return
        }

        TUILogin.login(sdkAppId, userID: userId, userSig: userSig) {
            result(NSNumber(value: 0))
        } fail: { code, message in
            let error = FlutterError(code: "\(code)", message: message, details: nil)
            result(error)
        }
    }

    public func logout(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        TUILogin.logout {
            result(NSNumber(value: 0))
        } fail: { code, message in
            let error = FlutterError(code: "\(code)", message: message, details: nil)
            result(error)
        }
    }
    
    public func showToast(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let content = MethodUtils.getMethodParams(call: call, key: "content", resultType: String.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "showToast", paramKey: "content", result: result)
            return
        }

        guard let durationEnum = MethodUtils.getMethodParams(call: call, key: "duration", resultType: Int.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "showToast", paramKey: "duration", result: result)
            return
        }

        guard let gravityEnum = MethodUtils.getMethodParams(call: call, key: "gravity", resultType: Int.self) else {
            FlutterResultUtils.handleMethod(code: .paramNotFound, methodName: "showToast", paramKey:  "gravity", result: result)
            return
        }

        let duration = durationEnum == 0 ? 2 : 4
        var idposition = TUICSToastPositionBottom
        switch(gravityEnum) {
        case 0:
            idposition = TUICSToastPositionTop
        case 1:
            idposition = TUICSToastPositionBottom
        case 2:
            idposition = TUICSToastPositionCenter
        default:
            idposition = TUICSToastPositionBottom
        }
        TUITool.makeToast(content, duration: TimeInterval(duration), idposition: idposition)
    }
}
