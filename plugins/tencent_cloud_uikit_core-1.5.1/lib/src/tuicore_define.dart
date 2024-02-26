const String TUICALLKIT_SERVICE_NAME = 'TUICallingService';

/// uikit call method name and param keys
const String METHOD_NAME_CALL = "call"; // 1v1 call or group call
const String PARAM_NAME_USERIDS = "userIDs"; // userIDs, value is List<String>
const String PARAM_NAME_GROUPID =
    "groupId"; // groupId, value is String, "" is single call, otherwise group call
const String PARAM_NAME_TYPE =
    "type"; // call media type, value is TYPE_AUDIO or TYPE_VIDEO
const String TYPE_AUDIO = "audio";
const String TYPE_VIDEO = "video";

/// uikit enable float window method name and param keys
const String METHOD_NAME_ENABLE_FLOAT_WINDOW =
    "methodEnableFloatWindow"; // Whether to display the floating window icon
const String PARAM_NAME_ENABLE_FLOAT_WINDOW =
    "enableFloatWindow"; // true:show, false:hide

///  Permission Result
enum TUIPermissionResult {
  granted,
  denied,
  requesting,
}

/// Permission Type
enum TUIPermissions {
  camera,
  microphone,
  bluetooth,
}

/// Toast
enum TUIGravity { top, bottom, center, none }

enum TUIDuration { short, long }
