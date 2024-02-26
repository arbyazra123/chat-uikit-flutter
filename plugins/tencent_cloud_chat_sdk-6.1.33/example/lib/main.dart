import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:tencent_cloud_chat_sdk/enum/V2TimAdvancedMsgListener.dart';
import 'package:tencent_cloud_chat_sdk/enum/V2TimConversationListener.dart';
import 'package:tencent_cloud_chat_sdk/enum/V2TimSDKListener.dart';
import 'package:tencent_cloud_chat_sdk/enum/group_member_role_enum.dart';
import 'package:tencent_cloud_chat_sdk/enum/history_msg_get_type_enum.dart';
import 'package:tencent_cloud_chat_sdk/enum/log_level_enum.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_callback.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_conversation.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_conversation_filter.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_conversation_result.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_group_member.dart';
import 'package:tencent_cloud_chat_sdk/models/v2_tim_value_callback.dart';
import 'package:tencent_cloud_chat_sdk/tencent_im_sdk_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a blue toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Tencent Cloud Chat Test'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int sdkappid = 1400664990; // get this param from tencent cloud chat console . https://console.cloud.tencent.com/im
  String userid = "109442"; // any string. recomand length 8-32.
  String usersig =
      "eJyrVgrxCdYrSy1SslIy0jNQ0gHzM1NS80oy0zLBwoYGliYmRlCZ4pTsxIKCzBQlK0MTAwMzMxNLSwOITGpFQWZRKlDc1NTUyMAAKlqSmQsSMzcwNjAxNjUzg5qSmQ40ODA9MMBRu7LUzcnLO98tKD3d3MjV2DeiPLswszjR1NEn1DHZ3SO7ojInxcJWqRYAv3YwoA__"; // like a password. you cloud get this param form tencent cloud console (dev tools tab) in dev env. or generate by your server in prod env. the document is https://cloud.tencent.com/document/product/269/32688#.E6.9C.8D.E5.8A.A1.E7.AB.AF.E8.AE.A1.E7.AE.97-usersig
  V2TimSDKListener initListener = V2TimSDKListener(
    onAllReceiveMessageOptChanged: (receiveMessageOptInfo) {},
    onConnectFailed: (code, error) {},
    onConnectSuccess: () {
      debugPrint("onConnectSuccess");
    },
    onConnecting: () {
      debugPrint("onConnecting");
    },
    onExperimentalNotify: (key, param) {},
    onKickedOffline: () {},
    onSelfInfoUpdated: (info) {},
    onUserInfoChanged: (userInfoList) {},
    onUserSigExpired: () {},
    onLog: (logLevel, logContent) {},
    onUserStatusChanged: (userStatusList) {},
  );

  V2TimConversationListener conversationListener = V2TimConversationListener(
    onConversationChanged: (conversationList) {
      // print("onConversationChanged");
      // conversationList.forEach(
      //   (element) {
      //     print(element.toJson());
      //   },
      // );
      // print("onConversationChanged end");
    },
    onTotalUnreadMessageCountChanged: (totalUnreadCount) async {
      print("当前会话分组信息");
      V2TimValueCallback<V2TimConversationResult> res = await TencentImSDKPlugin.v2TIMManager.getConversationManager().getConversationListByFilter(count: 20, filter: V2TimConversationFilter(conversationGroup: "fri"), nextSeq: 0);
      if (res.data != null) {
        if (res.data!.conversationList != null) {
          print("包含fri的会话有${res.data!.conversationList!.map((e) => e?.conversationID).join(",")}");
        }
      }
      print("onTotalUnreadMessageCountChanged");
      print(totalUnreadCount);
      print("onTotalUnreadMessageCountChanged end");
    },
    onUnreadMessageCountChangedByFilter: (filter, totalUnreadCount) {
      print("onUnreadMessageCountChangedByFilter");
      print(totalUnreadCount);
      print(filter.conversationGroup);
      print("onUnreadMessageCountChangedByFilter end");
    },
    onConversationGroupCreated: (groupName, conversationList) {},
    onConversationsAddedToGroup: (groupName, conversationList) {},
  );

  init() async {
    V2TimValueCallback<bool> initRes = await TencentImSDKPlugin.v2TIMManager.initSDK(
      sdkAppID: sdkappid,
      loglevel: LogLevelEnum.V2TIM_LOG_DEBUG,
      listener: initListener,
    );
    if (initRes.code == 0 && initRes.data == true) {
      debugPrint("you have init tencent cloud chat sdk sussess");
    } else {
      debugPrint("init failed!! ");
    }
  }

  V2TimAdvancedMsgListener messageListener = V2TimAdvancedMsgListener(
    onRecvNewMessage: (msg) {
      debugPrint("onRecvNewMessage msgid${msg.msgID}");
    },
    onRecvMessageModified: (msg) {
      debugPrint("message was modified");
      print(msg.textElem?.toJson());
    },
    // others message callback
  );
  addMessageListeners() async {
    await TencentImSDKPlugin.v2TIMManager.getConversationManager().subscribeUnreadMessageCountByFilter(filter: V2TimConversationFilter(conversationGroup: "fri"));
    await TencentImSDKPlugin.v2TIMManager.getConversationManager().addConversationListener(listener: conversationListener);
    await TencentImSDKPlugin.v2TIMManager.getMessageManager().addAdvancedMsgListener(
          listener: messageListener,
        );
  }

  login() async {
    V2TimCallback loginRes = await TencentImSDKPlugin.v2TIMManager.login(userID: userid, userSig: usersig);
    if (loginRes.code == 0) {
      debugPrint("login success!!!");
    } else {
      debugPrint("login failed!!! ${loginRes.desc}");
    }
    await TencentImSDKPlugin.v2TIMManager.getConversationManager().getTotalUnreadMessageCount();
  }

  getMessageListByTime() async {
    var convinfo = await TencentImSDKPlugin.v2TIMManager.getConversationManager().getConversation(conversationID: "c2c_1074880871939530");
    print("当前会话信息");
    print(json.encode(convinfo.toJson()));
    if (convinfo.data != null) {
      var conv = convinfo.data!;
      var time = conv.c2cReadTimestamp;
      var messageList = await TencentImSDKPlugin.v2TIMManager.getMessageManager().getHistoryMessageListV2(
            count: 20,
            userID: "1074880871939530",
            timeBegin: time,
            lastMsgSeq: -1,
            getType: HistoryMsgGetTypeEnum.V2TIM_GET_CLOUD_NEWER_MSG,
          );
      print(messageList.toJson());
      if (messageList.data != null) {
        var msgList = messageList.data!;
        print("当前获取新消息条数 ${msgList.messageList.length}");
      }
    }
  }

  sendMessage() async {
    var cmessage = await TencentImSDKPlugin.v2TIMManager.getMessageManager().createTextMessage(text: "red packet 11111");
    if (cmessage.data != null) {
      var id = cmessage.data!.id ?? "";
      var receiver = "@TGS#2OIFJJUNV";
      var a = await TencentImSDKPlugin.v2TIMManager.getMessageManager().sendMessage(id: id, receiver: "", groupID: receiver);
      print(a.data?.toJson());
    }
  }

  createGroup() async {
    var d = await TencentImSDKPlugin.v2TIMManager.getGroupManager().createGroup(groupType: "public", groupName: "test", memberList: [
      V2TimGroupMember(
        userID: "administrator",
        role: GroupMemberRoleTypeEnum.V2TIM_GROUP_MEMBER_ROLE_MEMBER,
      ),
    ]);
    print(d.toJson());
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        // TRY THIS: Try changing the color here to a specific color (to
        // Colors.amber, perhaps?) and trigger a hot reload to see the AppBar
        // change color while the other colors stay the same.
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Column(
          // Column is also a layout widget. It takes a list of children and
          // arranges them vertically. By default, it sizes itself to fit its
          // children horizontally, and tries to be as tall as its parent.
          //
          // Column has various properties to control how it sizes itself and
          // how it positions its children. Here we use mainAxisAlignment to
          // center the children vertically; the main axis here is the vertical
          // axis because Columns are vertical (the cross axis would be
          // horizontal).
          //
          // TRY THIS: Invoke "debug painting" (choose the "Toggle Debug Paint"
          // action in the IDE, or press "p" in the console), to see the
          // wireframe for each widget.
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              onPressed: init,
              child: const Text("Init（step one）"),
            ),
            ElevatedButton(
              onPressed: addMessageListeners,
              child: const Text("Add MessageListener（step two）"),
            ),
            ElevatedButton(
              onPressed: login,
              child: const Text("Login（step three）"),
            ),
            ElevatedButton(
              onPressed: getMessageListByTime,
              child: const Text("GetMessageListByTime（api test）"),
            ),
            ElevatedButton(
              onPressed: createGroup,
              child: const Text("createGroup"),
            ),
            ElevatedButton(
              onPressed: sendMessage,
              child: const Text("send message"),
            ),
            const Text("then you can call other api as you want"),
          ],
        ),
      ),
      // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
