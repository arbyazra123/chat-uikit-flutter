import 'package:flutter/material.dart';
import 'dart:async';

import 'package:tencent_cloud_uikit_core/tencent_cloud_uikit_core.dart';
import 'generate_test_user_sig.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool enableFloatWindow = false;
  final _tencentImBaseTUICore = TUICore();
  final _tencentImBaseTUILogin = TUILogin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SizedBox(
          child: ListView(
            padding: const EdgeInsets.all(40), //沿主轴方向居中
            children: <Widget>[
              Container(
                height: 50.0,
                child: ElevatedButton(
                  child: const Text("getService"),
                  onPressed: () {
                    _tencentImBaseTUICore
                        .getService(TUICALLKIT_SERVICE_NAME)
                        .then((value) =>
                            {TUIToast.show(content: "getService: ${value}")});
                  },
                ),
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("callService-singlecall"),
                  onPressed: () {
                    setState(() {
                      _tencentImBaseTUICore.callService(
                          TUICALLKIT_SERVICE_NAME, METHOD_NAME_CALL, {
                        PARAM_NAME_TYPE: TYPE_AUDIO,
                        PARAM_NAME_USERIDS: ["910635538"],
                        PARAM_NAME_GROUPID: ""
                      });
                    });
                  },
                ),
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("callService-groupcall"),
                  onPressed: () {
                    setState(() {
                      _tencentImBaseTUICore.callService(
                          TUICALLKIT_SERVICE_NAME, METHOD_NAME_CALL, {
                        PARAM_NAME_TYPE: TYPE_VIDEO,
                        PARAM_NAME_USERIDS: ["910635538", "8387", "8558"],
                        PARAM_NAME_GROUPID: "@TGS#1IYQ4JNML"
                      });
                    });
                  },
                ),
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("callService-floatWindow"),
                  onPressed: () {
                    setState(() {
                      enableFloatWindow = !enableFloatWindow;
                      if (enableFloatWindow) {
                        TUIToast.show(content: "show float window");
                      } else {
                        TUIToast.show(content: "hide float window");
                      }
                      _tencentImBaseTUICore.callService(
                          TUICALLKIT_SERVICE_NAME,
                          METHOD_NAME_ENABLE_FLOAT_WINDOW,
                          {PARAM_NAME_ENABLE_FLOAT_WINDOW: enableFloatWindow});
                    });
                  },
                ),
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("login"),
                  onPressed: () {
                    setState(() {
                      _tencentImBaseTUILogin.login(
                          GenerateTestUserSig.sdkAppId,
                          "1234",
                          GenerateTestUserSig.genTestSig("1234"),
                          TUICallback(onSuccess: () {
                            TUIToast.show(content: "login success");
                          }, onError: (int code, String message) {
                            TUIToast.show(content: "login fail, code:${code} message:${message}");
                          }));
                    });
                  },
                ),
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("logout"),
                  onPressed: () {
                    setState(() {
                      _tencentImBaseTUILogin.logout(TUICallback(onSuccess: () {
                        TUIToast.show(content: "logout success");
                      }, onError: (int code, String message) {
                        TUIToast.show(content: "logout fail, code:${code} message:${message}");
                      }));
                    });
                  },
                ),
              ),
              Container(
                  height: 50.0,
                  padding: const EdgeInsets.only(top: 5),
                  child: ElevatedButton(
                    child: const Text("Permission-has"),
                    onPressed: () async {
                      List<TUIPermissions> permissions = [TUIPermissions.camera, TUIPermissions.microphone];
                      var result = await TUIPermission.has(permissions: permissions);
                      TUIToast.show(content: "Permission.has : ${result}");
                  },
                  )
              ),
              Container(
                  height: 50.0,
                  padding: const EdgeInsets.only(top: 5),
                  child: ElevatedButton(
                    child: const Text("Permission-request"),
                    onPressed: () async {
                      List<TUIPermissions> permissions = [TUIPermissions.camera, TUIPermissions.microphone];
                      var result = await TUIPermission.request(permissions: permissions);
                      TUIToast.show(content: "Permission.has : ${result}");
                    },
                  )
              ),
              Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("Toast-Center"),
                  onPressed: () {
                    TUIToast.show(content: "success", duration: TUIDuration.short, gravity: TUIGravity.center);
                  },
                ),
              ),
            Container(
              height: 50.0,
              padding: const EdgeInsets.only(top: 5),
              child: ElevatedButton(
                child: const Text("Toast-Bottom"),
                onPressed: () {
                  TUIToast.show(content: "success", duration: TUIDuration.short, gravity: TUIGravity.bottom);
                },
              )
            ),
            Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("Toast-Top"),
                  onPressed: () {
                    TUIToast.show(content: "success", duration: TUIDuration.short, gravity: TUIGravity.top);
                  },
                )
            ),
            Container(
                height: 50.0,
                padding: const EdgeInsets.only(top: 5),
                child: ElevatedButton(
                  child: const Text("Toast-None"),
                  onPressed: () {
                    TUIToast.show(content: "success");
                  },
                )
            ),
          ],
        ),
      ),
    ));
  }
}
