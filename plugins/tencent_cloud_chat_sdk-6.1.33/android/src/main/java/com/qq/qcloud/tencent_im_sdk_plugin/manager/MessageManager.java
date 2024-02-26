package com.qq.qcloud.tencent_im_sdk_plugin.manager;

import com.qq.qcloud.tencent_im_sdk_plugin.util.CommonUtil;
import com.qq.qcloud.tencent_im_sdk_plugin.util.DownloadCallback;
import com.qq.qcloud.tencent_im_sdk_plugin.util.GetMesasgeByIDCallback;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMCompleteCallback;
import com.tencent.imsdk.v2.V2TIMElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMergerElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageExtension;
import com.tencent.imsdk.v2.V2TIMMessageExtensionResult;
import com.tencent.imsdk.v2.V2TIMMessageListGetOption;
import com.tencent.imsdk.v2.V2TIMMessageManager;
import com.tencent.imsdk.v2.V2TIMMessageReactionChangeInfo;
import com.tencent.imsdk.v2.V2TIMMessageReactionResult;
import com.tencent.imsdk.v2.V2TIMMessageReactionUserResult;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMMessageSearchParam;
import com.tencent.imsdk.v2.V2TIMMessageSearchResult;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.tencent.imsdk.v2.V2TIMReceiveMessageOptInfo;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.imsdk.v2.V2TIMGroupMessageReadMemberList;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MessageManager {
    static List<MethodChannel> channels = new LinkedList<>();
    private static V2TIMMessageManager manager;

    private static HashMap<String,V2TIMMessage> messageIDMap = new HashMap(); // Used to temporarily store the created message
    private static LinkedList<String> listenerUuidList = new LinkedList<String>();

    private static List<String> downloadingMessageList = new LinkedList<>();

    private static List<String> avchatroomgroupIDList= new LinkedList<>();
    private static List<V2TIMMessage> avchatroomMessageStorage = new LinkedList<>();

    private static int eachGroupMessageNums = 20;
    public static void cleanChannels(){
        channels = new LinkedList<>();
    }
    public MessageManager(MethodChannel _channel){
        MessageManager.channels.add(_channel);
        MessageManager.manager = V2TIMManager.getMessageManager();
    }
    public  static void getMessageByMessageID(final String msgid,  final GetMesasgeByIDCallback callback){

        boolean finded = false;
        if(avchatroomMessageStorage.size() > 0){
            for (int i = 0; i < avchatroomMessageStorage.size(); i++) {
                V2TIMMessage msg = avchatroomMessageStorage.get(i);
                if (msg.getMsgID().equals(msgid)){
                    callback.onSuccess(msg);
                    finded = true;
                    break;
                }
            }
        }
        if(finded){
            return;
        }
        List idlist = new LinkedList();
        idlist.add(msgid);
        V2TIMManager.getMessageManager().findMessages(idlist, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    if (v2TIMMessages.get(0).getMsgID().equals(msgid)){
                        callback.onSuccess(v2TIMMessages.get(0));
                    }else {
                        callback.onError(-3,"find message id error");
                    }
                }else {
                    callback.onError(-3,"find message length error");
                }
            }

            @Override
            public void onError(int i, String s) {
                callback.onError(i,s);
            }
        });
    }
    public V2TIMElem getElem (V2TIMMessage message){
        int type = message.getElemType();
        V2TIMElem elem;
        if(type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT){
            elem = message.getTextElem();
        } else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM){
            elem = message.getCustomElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE){
            elem = message.getImageElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND){
            elem = message.getSoundElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO){
            elem = message.getVideoElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_FILE){
            elem = message.getFileElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_LOCATION){
            elem = message.getLocationElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_FACE){
            elem = message.getFaceElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_GROUP_TIPS){
            elem = message.getGroupTipsElem();
        }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_MERGER){
            elem = message.getMergerElem();
        }else{
            elem = new V2TIMElem();
        }
        return elem;
    }
    public void setAppendMessage(V2TIMMessage appendMess,V2TIMMessage baseMessage){
        V2TIMElem aElem = getElem(appendMess);
        V2TIMElem bElem = getElem(baseMessage);
        bElem.appendElem(aElem);
    }
    public void appendMessage(MethodCall call,final MethodChannel.Result result){
        final String createMessageBaseId = CommonUtil.getParam(call,result,"createMessageBaseId");
        final String createMessageAppendId = CommonUtil.getParam(call,result,"createMessageAppendId");
        if(messageIDMap.containsKey(createMessageAppendId) && messageIDMap.containsKey(createMessageAppendId)){
            V2TIMMessage baseMessage = messageIDMap.get(createMessageBaseId);
            V2TIMMessage appendMess = messageIDMap.get(createMessageAppendId);
            setAppendMessage(appendMess,baseMessage);
            CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(baseMessage));
        }else{
            CommonUtil.returnError(result,-1,"message not found");
        }
    }
    public void  getMessageOnlineUrl(MethodCall call,final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(call,result,"msgID");
        List<String> msgIDList = new LinkedList<>();
        msgIDList.add(msgID);
        final List<Integer> needList = new LinkedList<>();
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_SOUND);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_FILE);

        MessageManager.getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msgInstance) {
                super.onSuccess(msgInstance);
                    HashMap<String,HashMap<String,Object>> res = new HashMap<>();
                    if(msgInstance.getMsgID().equals(msgID) && needList.contains(msgInstance.getElemType())){
                        if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE){
                            res.put("imageElem",CommonUtil.convertImageMessageElem(msgInstance.getImageElem()));
                        }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND){
                            res.put("soundElem",CommonUtil.convertSoundMessageElem(msgInstance.getSoundElem(),true));
                        }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO){
                            res.put("videoElem",CommonUtil.convertVideoMessageElem(msgInstance.getVideoElem(),true));
                        }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_FILE){
                            res.put("fileElem",CommonUtil.convertFileMessageElem(msgInstance.getFileElem(),true));
                        }
                        CommonUtil.returnSuccess(result,res);
                    }else{
                        CommonUtil.returnError(result,-1,"message invalid");
                    }

            }

            @Override
            public void onError(int code, String desc) {
                super.onError(code, desc);
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void sendProgress(boolean isFinish, boolean isError, long currentSize, long totalSize, String msgID,int type,boolean isSnapshot,String path,int error_code,String error_desc){
        final String downloadKey = msgID + (isSnapshot?"_1":"_0");

        if(isFinish){
            downloadingMessageList.remove(downloadKey);
        }
        if(isError){
            downloadingMessageList.remove(downloadKey);
        }
        HashMap<String,Object> res = new HashMap<>();
        res.put("isFinish",isFinish);
        res.put("isError",isError);
        res.put("currentSize",currentSize);
        res.put("totalSize",totalSize);
        res.put("msgID",msgID);
        res.put("type",type);
        res.put("isSnapshot",isSnapshot);
        res.put("path",path);
        res.put("errorCode",error_code);
        res.put("errorDesc",error_desc);
        makeAddAdvancedMsgListenerEventData("onMessageDownloadProgressCallback",res);
    }
    public void  downloadMessage(MethodCall call,final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(call,result,"msgID");
        final int imageType = CommonUtil.getParam(call,result,"imageType");
        final boolean isSnapshot = CommonUtil.getParam(call,result,"isSnapshot");
        List<String> msgIDList = new LinkedList<>();
        msgIDList.add(msgID);
        final List<Integer> needList = new LinkedList<>();
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_SOUND);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO);
        needList.add(V2TIMMessage.V2TIM_ELEM_TYPE_FILE);

        final String downloadKey = msgID + (isSnapshot?"_1":"_0");

        if(downloadingMessageList.contains(downloadKey)){
            CommonUtil.returnError(result,-1,"The message is downloading");
            return;
        }


        MessageManager.getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msgInstance) {
                super.onSuccess(msgInstance);
                HashMap<String,HashMap<String,Object>> res = new HashMap<>();
                if(msgInstance.getMsgID().equals(msgID) && needList.contains(msgInstance.getElemType())){
                    downloadingMessageList.add(downloadKey);
                    CommonUtil.returnSuccess(result,res);

                    if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE){
                        CommonUtil.downloadImageMessage(msgID, msgInstance.getImageElem(), imageType,new DownloadCallback() {
                            @Override
                            public void onProgress(boolean isFinish, boolean isError, long currentSize, long totalSize, String msgID,int type,boolean isSnapshot,String path,int error_code,String error_desc) {
                                sendProgress(isFinish,isError,currentSize,totalSize,msgID,type,isSnapshot,path,error_code,error_desc);
                            }
                        });
                    }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND){
                        CommonUtil.downloadDoundMessage(msgID, msgInstance.getSoundElem(), new DownloadCallback() {
                            @Override
                            public void onProgress(boolean isFinish, boolean isError, long currentSize, long totalSize, String msgID, int type, boolean isSnapshot, String path,int error_code,String error_desc) {
                                sendProgress(isFinish,isError,currentSize,totalSize,msgID,type,isSnapshot,path,error_code,error_desc);
                            }
                        });
                    }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO){
                        CommonUtil.downloadVideoMessage(msgID, msgInstance.getVideoElem(), isSnapshot, new DownloadCallback() {
                            @Override
                            public void onProgress(boolean isFinish, boolean isError, long currentSize, long totalSize, String msgID, int type, boolean isSnapshot, String path,int error_code,String error_desc) {
                                sendProgress(isFinish,isError,currentSize,totalSize,msgID,type,isSnapshot,path,error_code,error_desc);
                            }
                        });
                    }else if(msgInstance.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_FILE){
                        CommonUtil.downloadFileMessage(msgID, msgInstance.getFileElem(), new DownloadCallback() {
                            @Override
                            public void onProgress(boolean isFinish, boolean isError, long currentSize, long totalSize, String msgID, int type, boolean isSnapshot, String path,int error_code,String error_desc) {
                                sendProgress(isFinish,isError,currentSize,totalSize,msgID,type,isSnapshot,path,error_code,error_desc);
                            }
                        });
                    }
                }else{
                    CommonUtil.returnError(result,-1,"message invalid");
                }
            }

            @Override
            public void onError(int code, String desc) {
                super.onError(code, desc);
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void  setAvChatRoomCanFindMessage(MethodCall call,final MethodChannel.Result result){
        final List<String> avchatroomIDs = CommonUtil.getParam(call,result,"avchatroomIDs");
        final int nums = CommonUtil.getParam(call,result,"eachGroupMessageNums");

        if(!avchatroomIDs.isEmpty()){
            for (int i = 0; i < avchatroomIDs.size(); i++) {
                String id = avchatroomIDs.get(i);
                if(!avchatroomgroupIDList.contains(id)){
                    avchatroomgroupIDList.add(id);
                }
            }
        }


        eachGroupMessageNums = nums;

        CommonUtil.returnSuccess(result,avchatroomgroupIDList);
    }


    public static V2TIMAdvancedMsgListener messageListenerV2 = new V2TIMAdvancedMsgListener() {
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {
            String groupID = msg.getGroupID();
            if (avchatroomgroupIDList.contains(groupID)){
                LinkedList<Integer> multiMesasgeList = new LinkedList<Integer>();
                multiMesasgeList.add(V2TIMMessage.V2TIM_ELEM_TYPE_FILE);
                multiMesasgeList.add(V2TIMMessage.V2TIM_ELEM_TYPE_SOUND);
                multiMesasgeList.add(V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO);
                multiMesasgeList.add(V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE);
                multiMesasgeList.add(V2TIMMessage.V2TIM_ELEM_TYPE_MERGER);
                if(multiMesasgeList.contains(msg.getElemType())){
                    if(avchatroomMessageStorage.size() < eachGroupMessageNums){
                        avchatroomMessageStorage.add(msg);
                    }else {
                        avchatroomMessageStorage.remove(0);
                        avchatroomMessageStorage.add(msg);
                    }
                }
            }
            makeAddAdvancedMsgListenerEventData("onRecvNewMessage",CommonUtil.convertV2TIMMessageToMap(msg));
        }

        @Override
        public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
            List<Object> list = new LinkedList<Object>();
            for(int i = 0;i<receiptList.size();i++){
                list.add(CommonUtil.convertV2TIMMessageReceiptToMap(receiptList.get(i)));
            }
            makeAddAdvancedMsgListenerEventData("onRecvC2CReadReceipt",list);
        }

        @Override
        public void onRecvMessageReadReceipts(List<V2TIMMessageReceipt> receiptList) {
            List<Object> list = new LinkedList<Object>();
            for(int i = 0;i<receiptList.size();i++){
                list.add(CommonUtil.convertV2TIMMessageReceiptToMap(receiptList.get(i)));
            }
            makeAddAdvancedMsgListenerEventData("onRecvMessageReadReceipts",list);
        }

        @Override
        public void onRecvMessageRevoked(String msgID) {
            makeAddAdvancedMsgListenerEventData("onRecvMessageRevoked",msgID);
        }

        @Override
        public void onRecvMessageModified(V2TIMMessage msg) {
            makeAddAdvancedMsgListenerEventData("onRecvMessageModified",CommonUtil.convertV2TIMMessageToMap(msg));
        }

        @Override
        public void onRecvMessageExtensionsChanged(String msgID, List<V2TIMMessageExtension> extensions) {
            List<Object> list = new LinkedList<Object>();
            for(int i = 0;i<extensions.size();i++){
                list.add(CommonUtil.convertV2TIMMessageExtensionToMap(extensions.get(i)));
            }
            Map<String,Object> res = new HashMap<>();
            res.put("msgID",msgID);
            res.put("extensions",list);
            makeAddAdvancedMsgListenerEventData("onRecvMessageExtensionsChanged",res);
        }

        @Override
        public void onRecvMessageExtensionsDeleted(String msgID, List<String> extensionKeys) {
            Map<String,Object> res = new HashMap<>();
            res.put("msgID",msgID);
            res.put("extensionKeys",extensionKeys);
            makeAddAdvancedMsgListenerEventData("onRecvMessageExtensionsDeleted",res);
        }

        @Override
        public void onRecvMessageReactionsChanged(List<V2TIMMessageReactionChangeInfo> changeInfos) {
            List<Object> list = new LinkedList<Object>();
            for(int i = 0;i<changeInfos.size();i++){
                list.add(CommonUtil.convertV2TIMMessageReactionChangeInfoToMap(changeInfos.get(i)));
            }
            Map<String,Object> res = new HashMap<>();
            res.put("changeInfos",list);
            makeAddAdvancedMsgListenerEventData("onRecvMessageReactionsChanged",res);
        }

        @Override
        public void onRecvMessageRevoked(String msgID, V2TIMUserFullInfo operateUser, String reason) {
            Map<String,Object> res = new HashMap<>();
            res.put("msgID",msgID);
            res.put("operateUser",CommonUtil.convertV2TIMUserFullInfoToMap(operateUser));
            res.put("reason",reason);
            makeAddAdvancedMsgListenerEventData("onRecvMessageRevokedWithInfo",res);
        }
    };

    public void  addAdvancedMsgListener(MethodCall call,final MethodChannel.Result result){
        final String listenerUuid = CommonUtil.getParam(call,result,"listenerUuid");
        int size  = listenerUuidList.size();
        if(size == 0){
            System.out.println("current adapter layer listener is empty . add listener.");
            V2TIMManager.getMessageManager().addAdvancedMsgListener(messageListenerV2);
        }else{
            System.out.println("current adapter layer listener size is " + size);
        }
        listenerUuidList.add(listenerUuid);
        result.success("add advance msg listener success");
    }

    public void removeAdvancedMsgListener(MethodCall call,final MethodChannel.Result result){
        final String listenerUuid = CommonUtil.getParam(call,result,"listenerUuid");

        if (!listenerUuid.isEmpty()) {

            listenerUuidList.remove(listenerUuid);
            System.out.println("remove message listener. current message listener size id" + listenerUuidList.size());
            if(listenerUuidList.isEmpty()){
                V2TIMManager.getMessageManager().removeAdvancedMsgListener(messageListenerV2);
            }
            result.success("removeAdvancedMsgListener is done");
        } else {
            listenerUuidList.clear();
            V2TIMManager.getMessageManager().removeAdvancedMsgListener(messageListenerV2);
            result.success("all advanced message is removed");
        }
    }
    private static  <T> void  makeAddAdvancedMsgListenerEventData(String type,T data){
        for (MethodChannel channel : channels) {
            CommonUtil.emitEvent(channel,"advancedMsgListener",type,data, "");
        }

    }

    private <T> T getMapValue(HashMap<String,T> map,String key){
        T value =  map.get(key);
        return value;
    }
    // Encapsulates the method of processing offline push MethodCall methodCall, final MethodChannel.Result result
    private V2TIMOfflinePushInfo handleOfflinePushInfo(MethodCall methodCall,final MethodChannel.Result result){
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            String androidSound = (String) offlinePushInfoParams.get("androidSound");
            int androidVIVOClassification = (int) (offlinePushInfoParams.get("androidVIVOClassification") == null ? 1 : offlinePushInfoParams.get("androidVIVOClassification") );
            String androidFCMChannelID = (String) offlinePushInfoParams.get("androidFCMChannelID");
            String androidXiaoMiChannelID = (String) offlinePushInfoParams.get("androidXiaoMiChannelID");
            int iOSPushType = (int) (offlinePushInfoParams.get("iOSPushType") == null ? 0 :offlinePushInfoParams.get("iOSPushType") );
            String androidHuaWeiCategory = (String) offlinePushInfoParams.get("androidHuaWeiCategory");
            String androidVIVOCategory = (String) offlinePushInfoParams.get("androidVIVOCategory");

            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
            if(androidSound!=null){
                offlinePushInfo.setAndroidSound(androidSound);
            }


            if(androidFCMChannelID!=null){
                offlinePushInfo.setAndroidFCMChannelID(androidFCMChannelID);
            }
            if(androidXiaoMiChannelID!=null){
                offlinePushInfo.setAndroidXiaoMiChannelID(androidXiaoMiChannelID);
            }
            if(androidVIVOCategory!=null){
                offlinePushInfo.setAndroidVIVOCategory(androidVIVOCategory);
            }
            offlinePushInfo.setIOSPushType(iOSPushType);
            offlinePushInfo.setAndroidVIVOClassification(androidVIVOClassification);

            if(androidHuaWeiCategory!=null){
                offlinePushInfo.setAndroidHuaWeiCategory(androidHuaWeiCategory);
            }
        }
        return offlinePushInfo;
    }
    // encapsulate send message
    private void handleSendMessage(
        final V2TIMMessage msg,
        String receiver,
        String groupID,
        int priority,
        boolean onlineUserOnly,
        V2TIMOfflinePushInfo offlinePushInfo, 
        final MethodChannel.Result result,
        final String id
    ){
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i,id));
                data.put("progress",i);

                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                HashMap<String,Object> msgMap = CommonUtil.convertV2TIMMessageToMap(msg);
                msgMap.put("id",id);
                msgMap.put("status",V2TIMMessage.V2TIM_MSG_STATUS_SEND_FAIL);
                messageIDMap.remove(id);
                CommonUtil.returnError(result,i,s,msgMap);
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                HashMap<String,Object> data = CommonUtil.convertV2TIMMessageToMap(v2TIMMessage);
                data.put("id",id);
                messageIDMap.remove(id);
                CommonUtil.returnSuccess(result,data);
            }
        });
    }
    public static String createRandomStr(int length){

        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Random random = new Random();

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < length; i++) {

            int number = random.nextInt(62);

            stringBuffer.append(str.charAt(number));

        }

        return stringBuffer.toString();

    }
    // Encapsulate the process of setting local id
    private void handleSetMessageMap(V2TIMMessage message, final MethodChannel.Result result){
        final String id = String.valueOf(System.nanoTime()) + createRandomStr(10);
        messageIDMap.put(id,message);

        HashMap<String,Object> resultMap = new HashMap<String,Object>();
        HashMap<String,Object> messageMap = CommonUtil.convertV2TIMMessageToMap(message);

        messageMap.put("id",id);
        resultMap.put("messageInfo",messageMap);
        resultMap.put("id",id);

        CommonUtil.returnSuccess(result,resultMap);
    }

    public void createTextMessage(MethodCall methodCall, final MethodChannel.Result result){
        String text = CommonUtil.getParam(methodCall,result,"text");
        final V2TIMMessage msg = V2TIMManager.getMessageManager().createTextMessage(text);
        handleSetMessageMap(msg,result);
    }

    public void modifyMessage(MethodCall methodCall, final MethodChannel.Result result){
        final Map<String,Object> message = CommonUtil.getParam(methodCall,result,"message");

        if(message.get("msgID")==null){
            CommonUtil.returnError(result,-1,"message not found");
            return;
        }
        String messageID = (String) message.get("msgID");

        MessageManager.getMessageByMessageID(messageID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage currentMessage) {
                super.onSuccess(currentMessage);
                if(message.get("cloudCustomData")!=null){
                    currentMessage.setCloudCustomData((String) message.get("cloudCustomData"));
                }
                if(message.get("localCustomInt")!=null){
                    currentMessage.setLocalCustomInt((int) message.get("localCustomInt"));
                }
                if(message.get("localCustomData")!=null){
                    currentMessage.setLocalCustomData((String) message.get("localCustomData"));
                }
                if(currentMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT){
                    Map<String,Object> text = (Map<String, Object>) message.get("textElem");
                    if(text.get("text")!=null){
                        currentMessage.getTextElem().setText((String) text.get("text"));
                    }
                }
                if(currentMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM){
                    Map<String,Object> custom = (Map<String, Object>) message.get("customElem");
                    if(custom.get("data")!=null){
                        currentMessage.getCustomElem().setData(((String)custom.get("data")).getBytes());
                    }
                    if(custom.get("desc")!=null){
                        currentMessage.getCustomElem().setDescription(((String)custom.get("desc")));
                    }
                    if(custom.get("extension")!=null){
                        currentMessage.getCustomElem().setExtension(((String)custom.get("extension")).getBytes());
                    }
                }
                V2TIMManager.getMessageManager().modifyMessage(currentMessage, new V2TIMCompleteCallback<V2TIMMessage>() {
                    @Override
                    public void onComplete(int i, String s, V2TIMMessage v2TIMMessage) {
                        HashMap<String,Object> res = new HashMap<String,Object>();
                        res.put("code",i);
                        res.put("desc",s);
                        res.put("message",CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
                        CommonUtil.returnSuccess(result,res);
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
                super.onError(code, desc);
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public  void createTargetedGroupMessage(MethodCall methodCall, final MethodChannel.Result result){
        String id = CommonUtil.getParam(methodCall,result,"id");
        List<String> receiverList = CommonUtil.getParam(methodCall,result,"receiverList");
        V2TIMMessage msg = messageIDMap.get(id);
        final V2TIMMessage newMsg = V2TIMManager.getMessageManager().createTargetedGroupMessage(msg,receiverList);
        handleSetMessageMap(newMsg,result);
    }
    public void createCustomMessage(MethodCall methodCall, final MethodChannel.Result result){
        String data = CommonUtil.getParam(methodCall,result,"data");
        String desc = CommonUtil.getParam(methodCall,result,"desc");
        String extension = CommonUtil.getParam(methodCall,result,"extension");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createCustomMessage(data.getBytes(), desc, extension.getBytes());
        handleSetMessageMap(msg,result);
    }
    public void createImageMessage(MethodCall methodCall, final MethodChannel.Result result){
        String imagePath = CommonUtil.getParam(methodCall,result,"imagePath");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createImageMessage(imagePath);
        handleSetMessageMap(msg,result);
    }
    public void createSoundMessage(MethodCall methodCall, final MethodChannel.Result result){
        String soundPath = CommonUtil.getParam(methodCall,result,"soundPath");
        int duration = 0;
        if(CommonUtil.getParam(methodCall,result,"duration") != null){
            duration = CommonUtil.getParam(methodCall,result,"duration");
        }
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createSoundMessage(soundPath,duration);
        handleSetMessageMap(msg,result);
    }
    public void createVideoMessage(MethodCall methodCall, final MethodChannel.Result result){
        String videoFilePath = CommonUtil.getParam(methodCall,result,"videoFilePath");
        String type = CommonUtil.getParam(methodCall,result,"type");
        String snapshotPath = CommonUtil.getParam(methodCall,result,"snapshotPath");
        int duration = 0;
        if(CommonUtil.getParam(methodCall,result,"duration") != null){
            duration = CommonUtil.getParam(methodCall,result,"duration");
        }
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createVideoMessage(videoFilePath,type,duration,snapshotPath);
        handleSetMessageMap(msg,result);
    }
    public void createFileMessage(MethodCall methodCall, final MethodChannel.Result result){
        String filePath = CommonUtil.getParam(methodCall,result,"filePath");
        String fileName = CommonUtil.getParam(methodCall,result,"fileName");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createFileMessage(filePath,fileName);
        handleSetMessageMap(msg,result);
    }
    public void createLocationMessage(MethodCall methodCall, final MethodChannel.Result result){
        String desc = CommonUtil.getParam(methodCall,result,"desc");
        double longitude = CommonUtil.getParam(methodCall,result,"longitude");
        double latitude = CommonUtil.getParam(methodCall,result,"latitude");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createLocationMessage(desc,longitude,latitude);
        handleSetMessageMap(msg,result);
    }
    public void createFaceMessage(MethodCall methodCall, final MethodChannel.Result result){
        int index = CommonUtil.getParam(methodCall,result,"index");
        String data = CommonUtil.getParam(methodCall,result,"data");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createFaceMessage(index,data.getBytes());
        handleSetMessageMap(msg,result);
    }
    public void createTextAtMessage(MethodCall methodCall, final MethodChannel.Result result){
        String text = CommonUtil.getParam(methodCall,result,"text");
        List<String> atUserList = CommonUtil.getParam(methodCall,result,"atUserList");
        final V2TIMMessage msg =  V2TIMManager.getMessageManager().createTextAtMessage(text,atUserList);
        handleSetMessageMap(msg,result);
    }
    public void createMergerMessage(MethodCall methodCall, final MethodChannel.Result result){
        final String title = CommonUtil.getParam(methodCall,result,"title");
        final String compatibleText = CommonUtil.getParam(methodCall,result,"compatibleText");
        List<String> msgIDList = CommonUtil.getParam(methodCall,result,"msgIDList");
        final List<String> abstractList = CommonUtil.getParam(methodCall,result,"abstractList");

        final V2TIMMessageManager mannager = V2TIMManager.getMessageManager();

        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 0){
                    CommonUtil.returnError(result,-1,"message not found");
                    return;
                }
                final V2TIMMessage msg =  mannager.createMergerMessage(v2TIMMessages,title,abstractList,compatibleText);
                handleSetMessageMap(msg,result);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });

    }
    public void createForwardMessage(MethodCall methodCall, final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        LinkedList<String> msgIDList = new LinkedList<String>();
        msgIDList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> messageList) {
                if(messageList.size() != 1){
                    CommonUtil.returnError(result,-1,"message not found");
                    return;
                }
                final V2TIMMessage msg =  mannager.createForwardMessage(messageList.get(0));
                handleSetMessageMap(msg,result);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void sendMessage(final MethodCall methodCall, final MethodChannel.Result result){
        String id = CommonUtil.getParam(methodCall,result,"id");
        String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final String cloudCustomData = CommonUtil.getParam(methodCall,result,"cloudCustomData");
        final String localCustomData = CommonUtil.getParam(methodCall,result,"localCustomData");
        final Boolean needReadReceipt = CommonUtil.getParam(methodCall,result,"needReadReceipt");

        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        boolean isExcludedFromLastMessage = CommonUtil.getParam(methodCall,result,"isExcludedFromLastMessage");
        boolean isSupportMessageExtension = CommonUtil.getParam(methodCall,result,"isSupportMessageExtension");
        boolean isExcludedFromContentModeration = CommonUtil.getParam(methodCall,result,"isExcludedFromContentModeration");

        int priority;

        V2TIMMessage msg = messageIDMap.get(id);

        if(id == null){
            CommonUtil.returnError(result,-1,"id not exist please try create again");
        }
        if(CommonUtil.getParam(methodCall,result,"priority") == null){
            priority = 0;
        }else {
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly ;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = false;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }

        if(cloudCustomData !=null){
            msg.setCloudCustomData(cloudCustomData);
        }
        if(localCustomData != null){
            msg.setLocalCustomData(localCustomData);
        }

        if(needReadReceipt !=null) {
            msg.setNeedReadReceipt(needReadReceipt);
        }

        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        V2TIMOfflinePushInfo offlinePushInfo = handleOfflinePushInfo(methodCall,result);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        msg.setExcludedFromLastMessage(isExcludedFromLastMessage);
        msg.setSupportMessageExtension(isSupportMessageExtension);
        msg.setExcludedFromContentModeration(isExcludedFromContentModeration);
        handleSendMessage(msg,receiver,groupID,priority,onlineUserOnly,offlinePushInfo,result,id);
    }
    // Deprecated since 3.6.0
    public  void  sendTextMessage(MethodCall methodCall, final MethodChannel.Result result){
        String message = CommonUtil.getParam(methodCall,result,"text");
        String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority") == null){
            priority = 0;
        }else {
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly ;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = false;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();

        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();

        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String desc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(desc!=null){
                offlinePushInfo.setDesc(desc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }

        final V2TIMMessage msg =  mannager.createTextMessage(message);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {

                CommonUtil.returnError(result,i,s, CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendCustomMessage(MethodCall methodCall, final MethodChannel.Result result){
        String data = CommonUtil.getParam(methodCall,result,"data");
        String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        String desc = CommonUtil.getParam(methodCall,result,"desc");
        String extension = CommonUtil.getParam(methodCall,result,"extension");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority") == null){
            priority = 0;
        }else {
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly ;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = false;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
       final V2TIMMessage msg =  mannager.createCustomMessage(data.getBytes(), desc, extension.getBytes());
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendImageMessage(MethodCall methodCall, final MethodChannel.Result result){
        String imagePath = CommonUtil.getParam(methodCall,result,"imagePath");
         String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority  = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly")==null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createImageMessage(imagePath);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendSoundMessage(MethodCall methodCall, final MethodChannel.Result result){
        String soundPath = CommonUtil.getParam(methodCall,result,"soundPath");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        int duration = 0;
        if(CommonUtil.getParam(methodCall,result,"duration") != null){
            duration = CommonUtil.getParam(methodCall,result,"duration");
        }
         String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority  = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly")==null){
            onlineUserOnly = false;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createSoundMessage(soundPath,duration);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly,offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendVideoMessage(MethodCall methodCall, final MethodChannel.Result result){
        String videoFilePath = CommonUtil.getParam(methodCall,result,"videoFilePath");
        String type = CommonUtil.getParam(methodCall,result,"type");
        String snapshotPath = CommonUtil.getParam(methodCall,result,"snapshotPath");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        int duration;
         String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        if(CommonUtil.getParam(methodCall,result,"duration") == null){
            duration = 0;
        }else{
            duration   = CommonUtil.getParam(methodCall,result,"duration");
        }
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority  = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly")==null){
            onlineUserOnly = false;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }


        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createVideoMessage(videoFilePath,type,duration,snapshotPath);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);

        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendFileMessage(MethodCall methodCall, final MethodChannel.Result result){
        String filePath = CommonUtil.getParam(methodCall,result,"filePath");
        String fileName = CommonUtil.getParam(methodCall,result,"fileName");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
         String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createFileMessage(filePath,fileName);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);

        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendLocationMessage(MethodCall methodCall, final MethodChannel.Result result){
        String desc = CommonUtil.getParam(methodCall,result,"desc");
        double longitude = CommonUtil.getParam(methodCall,result,"longitude");
        double latitude = CommonUtil.getParam(methodCall,result,"latitude");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        final  String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createLocationMessage(desc,longitude,latitude);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));

            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendFaceMessage(MethodCall methodCall, final MethodChannel.Result result){
        int index = CommonUtil.getParam(methodCall,result,"index");
        String data = CommonUtil.getParam(methodCall,result,"data");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
         String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createFaceMessage(index,data.getBytes());

        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });

    }
    public  void  sendTextAtMessage(MethodCall methodCall, final MethodChannel.Result result){
        String text = CommonUtil.getParam(methodCall,result,"text");
        List<String> atUserList = CommonUtil.getParam(methodCall,result,"atUserList");
        boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String title = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(title!=null){
                offlinePushInfo.setTitle(title);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        final V2TIMMessage msg =  mannager.createTextAtMessage(text,atUserList);
        msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);

        mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int i) {
                HashMap<String,Object> data = new HashMap<String,Object>();
                data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                data.put("progress",i);
                makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });
    }
    public  void  downloadMergerMessage(MethodCall methodCall, final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(methodCall,result,"msgID");

        MessageManager.getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msg) {
                super.onSuccess(msg);
                V2TIMMergerElem message =  msg.getMergerElem();
                if(message!=null){
                    message.downloadMergerMessage(new V2TIMValueCallback<List<V2TIMMessage>>() {
                        @Override
                        public void onSuccess(List<V2TIMMessage> downMessages) {
                            List<HashMap<String,Object>> downloadMessageMap = new LinkedList<>();
                            for (int i = 0;i<downMessages.size();i++){
                                HashMap<String,Object> obj = CommonUtil.convertV2TIMMessageToMap(downMessages.get(i));
                                downloadMessageMap.add(obj);
                            }
                            CommonUtil.returnSuccess(result,downloadMessageMap);
                        }

                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s,null);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"this message is not mergeElem");
                }
            }

            @Override
            public void onError(int code, String desc) {
                super.onError(code, desc);
                CommonUtil.returnError(result,code,desc,null);
            }
        });
    }
    public  void  sendMergerMessage(MethodCall methodCall, final MethodChannel.Result result){
       final String title = CommonUtil.getParam(methodCall,result,"title");
        final String compatibleText = CommonUtil.getParam(methodCall,result,"compatibleText");
        List<String> msgIDList = CommonUtil.getParam(methodCall,result,"msgIDList");
        final List<String> abstractList = CommonUtil.getParam(methodCall,result,"abstractList");
        final boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        final String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        final String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        final boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        final V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String offTitle = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(offTitle!=null){
                offlinePushInfo.setTitle(offTitle);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
       final V2TIMMessageManager mannager = V2TIMManager.getMessageManager();

        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 0){
                    CommonUtil.returnError(result,-1,"message not found");
                    return;
                }
                final V2TIMMessage msg =  mannager.createMergerMessage(v2TIMMessages,title,abstractList,compatibleText);
                msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
                mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int i) {
                        HashMap<String,Object> data = new HashMap<String,Object>();
                        data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                        data.put("progress",i);
                        makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public  void  sendForwardMessage(MethodCall methodCall, final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final boolean isExcludedFromUnreadCount = CommonUtil.getParam(methodCall,result,"isExcludedFromUnreadCount");
        final String receiver = CommonUtil.getParam(methodCall,result,"receiver");
        final String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final int priority;
        if(CommonUtil.getParam(methodCall,result,"priority")==null){
            priority = 0;
        }else{
            priority = CommonUtil.getParam(methodCall,result,"priority");
        }
        final boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        final V2TIMOfflinePushInfo offlinePushInfo = new V2TIMOfflinePushInfo();
        HashMap<String,Object> offlinePushInfoParams = CommonUtil.getParam(methodCall,result,"offlinePushInfo");
        if(CommonUtil.getParam(methodCall,result,"offlinePushInfo")!=null){
            String offTitle = (String) offlinePushInfoParams.get("title");
            String offlineDesc = (String) offlinePushInfoParams.get("desc");
            Boolean disablePush = (Boolean) offlinePushInfoParams.get("disablePush");
            String ext = (String) offlinePushInfoParams.get("ext");
            String iOSSound = (String) offlinePushInfoParams.get("iOSSound");
            Boolean ignoreIOSBadge = (Boolean) offlinePushInfoParams.get("ignoreIOSBadge");
            String androidOPPOChannelID = (String) offlinePushInfoParams.get("androidOPPOChannelID");
            if(offTitle!=null){
                offlinePushInfo.setTitle(offTitle);
            }
            if(offlineDesc!=null){
                offlinePushInfo.setDesc(offlineDesc);
            }
            if(offlinePushInfoParams.get("disablePush")!=null){
                offlinePushInfo.disablePush(disablePush);
            }
            if(ext!=null){
                offlinePushInfo.setExt(ext.getBytes());
            }
            if (iOSSound!=null){
                offlinePushInfo.setIOSSound(iOSSound);
            }
            if(offlinePushInfoParams.get("ignoreIOSBadge")!=null){
                offlinePushInfo.setIgnoreIOSBadge(ignoreIOSBadge);
            }
            if(androidOPPOChannelID!=null){
                offlinePushInfo.setAndroidOPPOChannelID(androidOPPOChannelID);
            }
        }
        final V2TIMMessageManager mannager = V2TIMManager.getMessageManager();
        LinkedList<String> msgIDList = new LinkedList<String>();
        msgIDList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() != 1){
                    CommonUtil.returnError(result,-1,"message not found");
                    return;
                }
                final V2TIMMessage msg =  mannager.createForwardMessage(v2TIMMessages.get(0));
                msg.setExcludedFromUnreadCount(isExcludedFromUnreadCount);
                mannager.sendMessage(msg, receiver, groupID, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int i) {
                        HashMap<String,Object> data = new HashMap<String,Object>();
                        data.put("message",CommonUtil.convertV2TIMMessageToMap(msg,i));
                        data.put("progress",i);
                        makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(msg));
                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });




    }
    public void reSendMessage(MethodCall methodCall, final MethodChannel.Result result){
        String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final boolean onlineUserOnly;
        if(CommonUtil.getParam(methodCall,result,"onlineUserOnly") == null){
            onlineUserOnly = true;
        }else{
            onlineUserOnly = CommonUtil.getParam(methodCall,result,"onlineUserOnly");
        }
        List<String> msgList = new LinkedList<String>();
        msgList.add(msgID);
        final V2TIMMessageManager manager = V2TIMManager.getMessageManager();
        manager.findMessages(msgList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    final V2TIMMessage message = v2TIMMessages.get(0);
                    V2TIMOfflinePushInfo offlinePushInfo =  message.getOfflinePushInfo();
                    String groupId =  message.getGroupID();
                    String reciever = message.getUserID();
                    int priority = message.getPriority();
                    manager.sendMessage(message, reciever, groupId, priority, onlineUserOnly, offlinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
                        @Override
                        public void onProgress(int i) {
                            HashMap<String,Object> data = new HashMap<String,Object>();
                            data.put("message",CommonUtil.convertV2TIMMessageToMap(message,i));
                            data.put("progress",i);
                            makeAddAdvancedMsgListenerEventData("onSendMessageProgress",data);
                        }

                        @Override
                        public void onSuccess(V2TIMMessage v2TIMMessage) {
                            CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
                        }

                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s,CommonUtil.convertV2TIMMessageToMap(message));
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void setC2CReceiveMessageOpt(MethodCall methodCall, final MethodChannel.Result result){
        List<String> userIDList = CommonUtil.getParam(methodCall,result,"userIDList");
        int opt = CommonUtil.getParam(methodCall,result,"opt");
        V2TIMManager.getMessageManager().setC2CReceiveMessageOpt(userIDList, opt, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void getC2CReceiveMessageOpt(MethodCall methodCall, final MethodChannel.Result result){
        List<String> userIDList = CommonUtil.getParam(methodCall,result,"userIDList");
        V2TIMManager.getMessageManager().getC2CReceiveMessageOpt(userIDList, new V2TIMValueCallback<List<V2TIMReceiveMessageOptInfo>>() {
            @Override
            public void onSuccess(List<V2TIMReceiveMessageOptInfo> v2TIMReceiveMessageOptInfos) {
                LinkedList<HashMap<String,Object>> list = new LinkedList<HashMap<String, Object>>();
                for(int i =0;i<v2TIMReceiveMessageOptInfos.size();i++){
                    list.add(CommonUtil.convertV2TIMReceiveMessageOptInfoToMap(v2TIMReceiveMessageOptInfos.get(i)));
                }
                CommonUtil.returnSuccess(result,list);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void setGroupReceiveMessageOpt(MethodCall methodCall, final MethodChannel.Result result){
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        int opt = CommonUtil.getParam(methodCall,result,"opt");
        V2TIMManager.getMessageManager().setGroupReceiveMessageOpt(groupID, opt, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }



    public void setLocalCustomData(MethodCall methodCall, final MethodChannel.Result result){
        String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final String localCustomData = CommonUtil.getParam(methodCall,result,"localCustomData");
        List<String> msgList = new LinkedList<String>();
        msgList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    v2TIMMessages.get(0).setLocalCustomData(localCustomData);
                    CommonUtil.returnSuccess(result,null);
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void setLocalCustomInt(MethodCall methodCall, final MethodChannel.Result result){
        String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final int localCustomInt = CommonUtil.getParam(methodCall,result,"localCustomInt");
        List<String> msgList = new LinkedList<String>();
        msgList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    v2TIMMessages.get(0).setLocalCustomInt(localCustomInt);
                    CommonUtil.returnSuccess(result,null);
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void setCloudCustomData(MethodCall methodCall, final MethodChannel.Result result){
        String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        final String data = CommonUtil.getParam(methodCall,result,"data");
        List<String> msgList = new LinkedList<String>();
        msgList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    v2TIMMessages.get(0).setCloudCustomData(data);
                    CommonUtil.returnSuccess(result,null);
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }

    public void getC2CHistoryMessageList(MethodCall methodCall, final MethodChannel.Result result){

       final int count = CommonUtil.getParam(methodCall,result,"count");
        final String userID = CommonUtil.getParam(methodCall,result,"userID");
        final String lastMsgID = CommonUtil.getParam(methodCall,result,"lastMsgID");
        final List<String> list = new LinkedList<String>();
        if(lastMsgID != null){
            list.add(lastMsgID);
        }
        if(list.size()>0){
            V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int i, String s) {
                    CommonUtil.returnError(result,i,s);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    if(v2TIMMessages.size()>0){
                        V2TIMMessage msg = v2TIMMessages.get(0);
                        V2TIMManager.getMessageManager().getC2CHistoryMessageList(userID, count, msg, new V2TIMValueCallback<List<V2TIMMessage>>() {
                            @Override
                            public void onError(int i, String s) {
                                CommonUtil.returnError(result,i,s);
                            }

                            @Override
                            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                                LinkedList<HashMap<String,Object>> list = new LinkedList<HashMap<String,Object>>();
                                for(int i= 0;i<v2TIMMessages.size();i++){
                                    list.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                                }
                                CommonUtil.returnSuccess(result,list);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"message not found");
                    }
                }
            });
        }else{
            V2TIMManager.getMessageManager().getC2CHistoryMessageList(userID, count, null, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int i, String s) {
                    CommonUtil.returnError(result,i,s);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    LinkedList<HashMap<String,Object>> list = new LinkedList<HashMap<String,Object>>();
                    for(int i= 0;i<v2TIMMessages.size();i++){
                        list.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                    }
                    CommonUtil.returnSuccess(result,list);
                }
            });
        }

    }
    public void getGroupHistoryMessageList (MethodCall methodCall, final MethodChannel.Result result){
        final int count = CommonUtil.getParam(methodCall,result,"count");
        final String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final String lastMsgID = CommonUtil.getParam(methodCall,result,"lastMsgID");
        final List<String> list = new LinkedList<String>();
        if(lastMsgID != null){
            list.add(lastMsgID);
        }
        if(list.size() > 0){
            V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int i, String s) {
                    CommonUtil.returnError(result,i,s);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    if(v2TIMMessages.size()>0){
                        V2TIMManager.getMessageManager().getGroupHistoryMessageList(groupID, count, v2TIMMessages.get(0), new V2TIMValueCallback<List<V2TIMMessage>>() {
                            @Override
                            public void onError(int i, String s) {
                                CommonUtil.returnError(result,i,s);
                            }

                            @Override
                            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                                LinkedList<HashMap<String,Object>> list = new LinkedList<HashMap<String,Object>>();
                                
                                for(int i= 0;i<v2TIMMessages.size();i++){
                                    list.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                                }
                                CommonUtil.returnSuccess(result,list);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"message not found");
                    }
                }
            });
        }else{
            V2TIMManager.getMessageManager().getGroupHistoryMessageList(groupID, count, null, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int i, String s) {
                    CommonUtil.returnError(result,i,s);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    LinkedList<HashMap<String,Object>> list = new LinkedList<HashMap<String,Object>>();
                    for(int i= 0;i<v2TIMMessages.size();i++){
                        list.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                    }
                    CommonUtil.returnSuccess(result,list);
                }
            });
        }
    }

    public void getHistoryMessageList(MethodCall methodCall, final MethodChannel.Result result){
        int getType = CommonUtil.getParam(methodCall,result,"getType");
        String userID = CommonUtil.getParam(methodCall,result,"userID");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final String lastMsgID = CommonUtil.getParam(methodCall,result,"lastMsgID");
        int lastMsgSeq = CommonUtil.getParam(methodCall, result, "lastMsgSeq");
        int count  = CommonUtil.getParam(methodCall,result,"count");


        final V2TIMMessageListGetOption option = new V2TIMMessageListGetOption();
        option.setCount(count);
        option.setGetType(getType);
        if(groupID!=null){
            option.setGroupID(groupID);
        }
        if(userID!=null){
            option.setUserID(userID);
        }
        if (lastMsgSeq != -1) {
            option.setLastMsgSeq(lastMsgSeq);
        }
        if(CommonUtil.getParam(methodCall,result,"messageTypeList")!=null){
            List<Integer> messageTypeList = CommonUtil.getParam(methodCall,result,"messageTypeList");
            option.setMessageTypeList(messageTypeList);
        }
        if(CommonUtil.getParam(methodCall,result,"messageSeqList")!=null){
            List<Integer> messageTypeList = CommonUtil.getParam(methodCall,result,"messageSeqList");
            List<Long> longList = new LinkedList<Long>();

            for (Integer intValue : messageTypeList) {
                longList.add(intValue.longValue());
            }
            option.setMessageSeqList(longList);
        }
        if(CommonUtil.getParam(methodCall,result,"timeBegin")!=null){
            int timeBegin  = CommonUtil.getParam(methodCall,result,"timeBegin");
            option.setGetTimeBegin(Long.valueOf(timeBegin));
        }
        if(CommonUtil.getParam(methodCall,result,"timePeriod")!=null){
            int timePeriod  = CommonUtil.getParam(methodCall,result,"timePeriod");
            option.setGetTimePeriod(Long.valueOf(timePeriod));
        }

        List<String> msglist =new  LinkedList<String>();
        if(lastMsgID!=null){
            msglist.add(lastMsgID);
            V2TIMManager.getMessageManager().findMessages(msglist, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    if(v2TIMMessages.size() == 1){
                        // found message
                        option.setLastMsg(v2TIMMessages.get(0));
                        V2TIMManager.getMessageManager().getHistoryMessageList(option, new V2TIMValueCallback<List<V2TIMMessage>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                                LinkedList<HashMap<String,Object>> msgs =new  LinkedList<HashMap<String,Object>>();
                                for(int i = 0;i<v2TIMMessages.size();i++){
                                    HashMap<String,Object> msg = CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i));
                                    msgs.add(msg);
                                }
                                CommonUtil.returnSuccess(result,msgs);
                            }

                            @Override
                            public void onError(int code, String desc) {
                                CommonUtil.returnError(result,code,desc);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"message not found");
                    }
                }

                @Override
                public void onError(int code, String desc) {
                    CommonUtil.returnError(result,code,desc);
                }
            });
        }else{
            V2TIMManager.getMessageManager().getHistoryMessageList(option, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    List<HashMap<String,Object>> msgs =new  LinkedList<HashMap<String,Object>>();
                    for(int i = 0;i<v2TIMMessages.size();i++){
                        msgs.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                    }
                    CommonUtil.returnSuccess(result,msgs);
                }

                @Override
                public void onError(int code, String desc) {
                    CommonUtil.returnError(result,code,desc);
                }
            });
        }
    }
    public void getHistoryMessageListV2(MethodCall methodCall, final MethodChannel.Result result){
        int getType = CommonUtil.getParam(methodCall,result,"getType");
        String userID = CommonUtil.getParam(methodCall,result,"userID");
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        final String lastMsgID = CommonUtil.getParam(methodCall,result,"lastMsgID");
        int lastMsgSeq = CommonUtil.getParam(methodCall, result, "lastMsgSeq");
        int count  = CommonUtil.getParam(methodCall,result,"count");


        final V2TIMMessageListGetOption option = new V2TIMMessageListGetOption();
        option.setCount(count);
        option.setGetType(getType);
        if(groupID!=null){
            option.setGroupID(groupID);
        }
        if(userID!=null){
            option.setUserID(userID);
        }
        if (lastMsgSeq != -1) {
            option.setLastMsgSeq(lastMsgSeq);
        }
        if(CommonUtil.getParam(methodCall,result,"messageTypeList")!=null){
            List<Integer> messageTypeList = CommonUtil.getParam(methodCall,result,"messageTypeList");
            option.setMessageTypeList(messageTypeList);
        }
        if(CommonUtil.getParam(methodCall,result,"messageSeqList")!=null){
            List<Integer> messageTypeList = CommonUtil.getParam(methodCall,result,"messageSeqList");
            List<Long> longList = new LinkedList<Long>();

            for (Integer intValue : messageTypeList) {
                longList.add(intValue.longValue());
            }
            option.setMessageSeqList(longList);
        }
        if(CommonUtil.getParam(methodCall,result,"timeBegin")!=null){
            int timeBegin  = CommonUtil.getParam(methodCall,result,"timeBegin");
            option.setGetTimeBegin(Long.valueOf(timeBegin));
        }
        if(CommonUtil.getParam(methodCall,result,"timePeriod")!=null){
            int timePeriod  = CommonUtil.getParam(methodCall,result,"timePeriod");
            option.setGetTimePeriod(Long.valueOf(timePeriod));
        }
        List<String> msglist =new  LinkedList<String>();
        if(lastMsgID!=null){
            msglist.add(lastMsgID);
            V2TIMManager.getMessageManager().findMessages(msglist, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    if(v2TIMMessages.size() == 1){
                        // found message
                        option.setLastMsg(v2TIMMessages.get(0));
                        V2TIMManager.getMessageManager().getHistoryMessageList(option, new V2TIMValueCallback<List<V2TIMMessage>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                                LinkedList<HashMap<String,Object>> msgs =new  LinkedList<HashMap<String,Object>>();
                                for(int i = 0;i<v2TIMMessages.size();i++){
                                    HashMap<String,Object> msg = CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i));
                                    msgs.add(msg);
                                }
                                HashMap<String,Object> res = new HashMap<>();
                                res.put("isFinished",v2TIMMessages.size() < option.getCount());
                                res.put("messageList",msgs);
                                CommonUtil.returnSuccess(result, res);
                            }

                            @Override
                            public void onError(int code, String desc) {
                                CommonUtil.returnError(result,code,desc);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"message not found");
                    }
                }

                @Override
                public void onError(int code, String desc) {
                    CommonUtil.returnError(result,code,desc);
                }
            });
        }else{
            V2TIMManager.getMessageManager().getHistoryMessageList(option, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    List<HashMap<String,Object>> msgs =new  LinkedList<HashMap<String,Object>>();
                    for(int i = 0;i<v2TIMMessages.size();i++){
                        msgs.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                    }
                    HashMap<String,Object> res = new HashMap<>();
                    res.put("isFinished",v2TIMMessages.size() < option.getCount());
                    res.put("messageList",msgs);
                    CommonUtil.returnSuccess(result,res);
                }

                @Override
                public void onError(int code, String desc) {
                    CommonUtil.returnError(result,code,desc);
                }
            });
        }
    }

    public void revokeMessage(MethodCall methodCall, final MethodChannel.Result result){
       final String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        LinkedList<String> list = new LinkedList<>();
        list.add(msgID);
        V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size()>0){
                    V2TIMManager.getMessageManager().revokeMessage(v2TIMMessages.get(0), new V2TIMCallback() {
                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess() {
                            CommonUtil.returnSuccess(result,null);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });

    }
    public void markC2CMessageAsRead(MethodCall methodCall, final MethodChannel.Result result){
        String userID = CommonUtil.getParam(methodCall,result,"userID");
        V2TIMManager.getMessageManager().markC2CMessageAsRead(userID, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }
        });
    }
    public void markGroupMessageAsRead(MethodCall methodCall, final MethodChannel.Result result){
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        V2TIMManager.getMessageManager().markGroupMessageAsRead(groupID, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }
        });
    }

    public void markAllMessageAsRead(MethodCall methodCall, final MethodChannel.Result result){
        V2TIMManager.getMessageManager().markAllMessageAsRead(new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }
        });
    }
    public void deleteMessageFromLocalStorage(MethodCall methodCall, final MethodChannel.Result result){
        final String msgID = CommonUtil.getParam(methodCall,result,"msgID");
        LinkedList<String> list = new LinkedList<>();
        list.add(msgID);
        V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size()>0){
                    V2TIMManager.getMessageManager().deleteMessageFromLocalStorage(v2TIMMessages.get(0), new V2TIMCallback() {
                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess() {
                            CommonUtil.returnSuccess(result,null);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });
    }
    public void deleteMessages(MethodCall methodCall, final MethodChannel.Result result){
        final List<String> msgIDs = CommonUtil.getParam(methodCall,result,"msgIDs");
        V2TIMManager.getMessageManager().findMessages(msgIDs, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                System.out.println("find arrived message"+v2TIMMessages.size());
                if(v2TIMMessages.size()>0){
                    V2TIMManager.getMessageManager().deleteMessages(v2TIMMessages, new V2TIMCallback() {
                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess() {
                            CommonUtil.returnSuccess(result,null);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });
    }
    public void sendMessageReadReceipts(MethodCall methodCall, final MethodChannel.Result result){
        final List<String> messageIDList = CommonUtil.getParam(methodCall,result,"messageIDList");
        V2TIMManager.getMessageManager().findMessages(messageIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == messageIDList.size()){
                    V2TIMManager.getMessageManager().sendMessageReadReceipts(v2TIMMessages, new V2TIMCallback() {
                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess() {
                            CommonUtil.returnSuccess(result,null);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });
    } 
    public void getMessageReadReceipts(MethodCall methodCall, final MethodChannel.Result result){
        final List<String> messageIDList = CommonUtil.getParam(methodCall,result,"messageIDList");
        V2TIMManager.getMessageManager().findMessages(messageIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == messageIDList.size()){
                    V2TIMManager.getMessageManager().getMessageReadReceipts(v2TIMMessages, new V2TIMValueCallback<List<V2TIMMessageReceipt>>() {
                        @Override
                        public void onError(int i, String s) {
                            
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess(List<V2TIMMessageReceipt> receiptList) {
                            List<Object> list = new LinkedList<Object>();
                            for(int i = 0;i<receiptList.size();i++){
                                list.add(CommonUtil.convertV2TIMMessageReceiptToMap(receiptList.get(i)));
                            }
                            CommonUtil.returnSuccess(result,list);
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });
    } 
    public void getGroupMessageReadMemberList(MethodCall methodCall, final MethodChannel.Result result){
        final String messageID = CommonUtil.getParam(methodCall,result,"messageID");
        final int filter = CommonUtil.getParam(methodCall,result,"filter");
        final int nextSeqParams = CommonUtil.getParam(methodCall,result,"nextSeq");
        final int count = CommonUtil.getParam(methodCall,result,"count");
        final long nextSeq = new Long(nextSeqParams);
        
        LinkedList<String> msgList = new LinkedList<>();

        msgList.add(messageID);

        V2TIMManager.getMessageManager().findMessages(msgList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    V2TIMManager.getMessageManager().getGroupMessageReadMemberList(v2TIMMessages.get(0), filter,nextSeq,count, new V2TIMValueCallback<V2TIMGroupMessageReadMemberList>() {
                        @Override
                        public void onError(int i, String s) {
                            
                            CommonUtil.returnError(result,i,s);
                        }

                        @Override
                        public void onSuccess(V2TIMGroupMessageReadMemberList memberlist) {
                            CommonUtil.returnSuccess(result,CommonUtil.converV2TIMGroupMessageReadMemberListToMap(memberlist));
                        }
                    });
                }else{
                    CommonUtil.returnError(result,-1,"messages not found");
                }
            }
        });
    } 
    
    public void insertGroupMessageToLocalStorage(final MethodCall methodCall, final MethodChannel.Result result){
        final String data = CommonUtil.getParam(methodCall,result,"data");
        final String groupID= CommonUtil.getParam(methodCall,result,"groupID");
        final String sender= CommonUtil.getParam(methodCall,result,"sender");

        V2TIMMessageManager msgManager =  V2TIMManager.getInstance().getMessageManager();
        final V2TIMMessage msg = msgManager.createCustomMessage(data.getBytes());

        V2TIMManager.getMessageManager().insertGroupMessageToLocalStorage(msg, groupID, sender, new V2TIMValueCallback<V2TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }
        });
    }
    public void insertC2CMessageToLocalStorage(final MethodCall methodCall, final MethodChannel.Result result){
        final String data = CommonUtil.getParam(methodCall,result,"data");
        final String userID =  CommonUtil.getParam(methodCall,result,"userID");
        final String sender= CommonUtil.getParam(methodCall,result,"sender");
        V2TIMMessageManager msgManager =  V2TIMManager.getInstance().getMessageManager();
        V2TIMMessage msg = msgManager.createCustomMessage(data.getBytes());
        msgManager.insertC2CMessageToLocalStorage(msg, userID, sender, new V2TIMValueCallback<V2TIMMessage>() {
            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageToMap(v2TIMMessage));
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });

    }
    public void clearC2CHistoryMessage(final MethodCall methodCall, final MethodChannel.Result result){
        String userID = CommonUtil.getParam(methodCall,result,"userID");
        V2TIMManager.getMessageManager().clearC2CHistoryMessage(userID, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }

    public void clearGroupHistoryMessage(final MethodCall methodCall, final MethodChannel.Result result){
        String groupID = CommonUtil.getParam(methodCall,result,"groupID");
        V2TIMManager.getMessageManager().clearGroupHistoryMessage(groupID, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }

    public void searchLocalMessages(final MethodCall methodCall, final MethodChannel.Result result){
        HashMap<String,Object> searchParam = CommonUtil.getParam(methodCall,result,"searchParam");
        V2TIMMessageSearchParam param = new V2TIMMessageSearchParam();
        if(searchParam.get("conversationID")!=null){
            param.setConversationID((String) searchParam.get("conversationID"));
        }
        if(searchParam.get("keywordList")!=null){
            param.setKeywordList((List<String>) searchParam.get("keywordList"));
        }
        if(searchParam.get("type")!=null){
            param.setKeywordListMatchType((Integer) searchParam.get("type"));
        }
        if(searchParam.get("userIDList")!=null){
            param.setSenderUserIDList((List<String>) searchParam.get("userIDList"));
        }
        if(searchParam.get("messageTypeList")!=null){
            param.setMessageTypeList((List<Integer>) searchParam.get("messageTypeList"));
        }
        if(searchParam.get("searchTimePosition")!=null){
            param.setSearchTimePosition((Integer) searchParam.get("searchTimePosition"));
        }
        if(searchParam.get("searchTimePeriod")!=null){
            param.setSearchTimePeriod((Integer) searchParam.get("searchTimePeriod"));
        }
        if(searchParam.get("pageSize")!=null){
            param.setPageSize((Integer) searchParam.get("pageSize"));
        }
        if(searchParam.get("pageIndex")!=null){
            param.setPageIndex((Integer) searchParam.get("pageIndex"));
        }
        if(searchParam.get("searchCount")!=null){
            param.setSearchCount((Integer) searchParam.get("searchCount"));
        }
        if(searchParam.get("searchCursor")!=null){
            param.setSearchCursor((String) searchParam.get("searchCursor"));
        }
        V2TIMManager.getMessageManager().searchLocalMessages(param, new V2TIMValueCallback<V2TIMMessageSearchResult>() {
            @Override
            public void onSuccess(V2TIMMessageSearchResult v2TIMMessageSearchResult) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageSearchResultToMap(v2TIMMessageSearchResult));
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void searchCloudMessages(final MethodCall methodCall, final MethodChannel.Result result){
        HashMap<String,Object> searchParam = CommonUtil.getParam(methodCall,result,"searchParam");
        V2TIMMessageSearchParam param = new V2TIMMessageSearchParam();
        if(searchParam.get("conversationID")!=null){
            param.setConversationID((String) searchParam.get("conversationID"));
        }
        if(searchParam.get("keywordList")!=null){
            param.setKeywordList((List<String>) searchParam.get("keywordList"));
        }
        if(searchParam.get("type")!=null){
            param.setKeywordListMatchType((Integer) searchParam.get("type"));
        }
        if(searchParam.get("userIDList")!=null){
            param.setSenderUserIDList((List<String>) searchParam.get("userIDList"));
        }
        if(searchParam.get("messageTypeList")!=null){
            param.setMessageTypeList((List<Integer>) searchParam.get("messageTypeList"));
        }
        if(searchParam.get("searchTimePosition")!=null){
            param.setSearchTimePosition((Integer) searchParam.get("searchTimePosition"));
        }
        if(searchParam.get("searchTimePeriod")!=null){
            param.setSearchTimePeriod((Integer) searchParam.get("searchTimePeriod"));
        }
        if(searchParam.get("pageSize")!=null){
            param.setPageSize((Integer) searchParam.get("pageSize"));
        }
        if(searchParam.get("pageIndex")!=null){
            param.setPageIndex((Integer) searchParam.get("pageIndex"));
        }
        if(searchParam.get("searchCount")!=null){
            param.setSearchCount((Integer) searchParam.get("searchCount"));
        }
        if(searchParam.get("searchCursor")!=null){
            param.setSearchCursor((String) searchParam.get("searchCursor"));
        }
        V2TIMManager.getMessageManager().searchCloudMessages(param, new V2TIMValueCallback<V2TIMMessageSearchResult>() {
            @Override
            public void onSuccess(V2TIMMessageSearchResult v2TIMMessageSearchResult) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageSearchResultToMap(v2TIMMessageSearchResult));
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void findMessages(final MethodCall methodCall, final MethodChannel.Result result){
        List<String> messageIDList = CommonUtil.getParam(methodCall,result,"messageIDList");
        V2TIMManager.getMessageManager().findMessages(messageIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                LinkedList<HashMap<String,Object>> messageList = new LinkedList<>();
                for (int i = 0;i<v2TIMMessages.size();i++){
                    messageList.add(CommonUtil.convertV2TIMMessageToMap(v2TIMMessages.get(i)));
                }
                CommonUtil.returnSuccess(result,messageList);
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void setMessageExtensions(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        List<Map<String,String>> extensions = methodCall.argument("extensions");
        List<String> msgIDList = new LinkedList<>();
        msgIDList.add(msgID);
        if(extensions.size() == 0){
            CommonUtil.returnError(result,-1,"error extensions");
            return;
        }
        final List<V2TIMMessageExtension> extensionList  = new LinkedList<>();
        for (Map<String, String> extension : extensions) {
            V2TIMMessageExtension item = new V2TIMMessageExtension();
                if(extension.containsKey("key")){
                    item.setExtensionKey(extension.get("key"));
                }
                if(extension.containsKey("value")){
                    item.setExtensionValue(extension.get("value"));
                }
            extensionList.add(item);
        }
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    V2TIMMessage msg = v2TIMMessages.get(0);
                    if(msg.isSupportMessageExtension() && msg.getStatus()== 2){
                        V2TIMManager.getMessageManager().setMessageExtensions(msg, extensionList, new V2TIMValueCallback<List<V2TIMMessageExtensionResult>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessageExtensionResult> v2TIMMessageExtensionResults) {
                                LinkedList<HashMap<String,Object>> extentionResultList = new LinkedList<>();
                                for (int i = 0;i<v2TIMMessageExtensionResults.size();i++){
                                    extentionResultList.add(CommonUtil.convertV2TIMMessageExtensionResultToMap(v2TIMMessageExtensionResults.get(i)));
                                }
                                CommonUtil.returnSuccess(result,extentionResultList);
                            }

                            @Override
                            public void onError(int i, String s) {
                                CommonUtil.returnError(result,i,s);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"error message info");
                    }
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void getMessageExtensions(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        List<String> msgIDList = new LinkedList<>();
        msgIDList.add(msgID);
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    V2TIMMessage msg = v2TIMMessages.get(0);
                    if(msg.isSupportMessageExtension() && msg.getStatus()== 2){
                        V2TIMManager.getMessageManager().getMessageExtensions(msg, new V2TIMValueCallback<List<V2TIMMessageExtension>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessageExtension> v2TIMMessageExtensions) {
                                LinkedList<HashMap<String,Object>> extentionList = new LinkedList<>();
                                for (int i = 0;i<v2TIMMessageExtensions.size();i++){
                                    extentionList.add(CommonUtil.convertV2TIMMessageExtensionToMap(v2TIMMessageExtensions.get(i)));
                                }
                                CommonUtil.returnSuccess(result,extentionList);
                            }

                            @Override
                            public void onError(int i, String s) {
                                CommonUtil.returnError(result,i,s);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"error message info");
                    }
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void deleteMessageExtensions(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        List<String> msgIDList = new LinkedList<>();
        msgIDList.add(msgID);
        final List<String> keys = methodCall.argument("keys");
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if(v2TIMMessages.size() == 1){
                    V2TIMMessage msg = v2TIMMessages.get(0);
                    if(msg.isSupportMessageExtension() && msg.getStatus()== 2){
                        V2TIMManager.getMessageManager().deleteMessageExtensions(msg,keys,new V2TIMValueCallback<List<V2TIMMessageExtensionResult>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessageExtensionResult> v2TIMMessageExtensionResults) {
                                LinkedList<HashMap<String,Object>> extentionResultList = new LinkedList<>();
                                for (int i = 0;i<v2TIMMessageExtensionResults.size();i++){
                                    extentionResultList.add(CommonUtil.convertV2TIMMessageExtensionResultToMap(v2TIMMessageExtensionResults.get(i)));
                                }
                                CommonUtil.returnSuccess(result,extentionResultList);
                            }

                            @Override
                            public void onError(int i, String s) {
                                CommonUtil.returnError(result,i,s);
                            }
                        });
                    }else{
                        CommonUtil.returnError(result,-1,"error message info");
                    }
                }else{
                    CommonUtil.returnError(result,-1,"message not found");
                }
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void translateText(final MethodCall methodCall, final MethodChannel.Result result){
        List<String> texts = CommonUtil.getParam(methodCall,result,"texts");
        String sourceLanguage = methodCall.argument("sourceLanguage");
        String targetLanguage = methodCall.argument("targetLanguage");
        V2TIMManager.getMessageManager().translateText(texts, sourceLanguage, targetLanguage, new V2TIMValueCallback<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> stringStringHashMap) {
                CommonUtil.returnSuccess(result,stringStringHashMap);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });

    }
    public void setAllReceiveMessageOpt(final MethodCall methodCall, final MethodChannel.Result result){
        int opt = methodCall.argument("opt");
        int startHour = methodCall.argument("startHour");
        int startMinute = methodCall.argument("startMinute");
        int startSecond = methodCall.argument("startSecond");
        long duration = new Long((int)methodCall.argument("duration"));
        V2TIMManager.getMessageManager().setAllReceiveMessageOpt(opt, startHour, startMinute, startSecond, duration, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void convertVoiceToText(final MethodCall methodCall, final MethodChannel.Result result){
        final String msgID = methodCall.argument("msgID");
        final String language = methodCall.argument("language");
        getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msg) {
                if(msg.getElemType() != 4){
                    CommonUtil.returnError(result,-3 ,"error message elem type");
                }else{
                    msg.getSoundElem().convertVoiceToText(language, new V2TIMValueCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            CommonUtil.returnSuccess(result,s);
                        }

                        @Override
                        public void onError(int i, String s) {
                            CommonUtil.returnError(result,i ,s);
                        }
                    });
                }
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code ,desc);
            }
        });
    }

    public void setAllReceiveMessageOptWithTimestamp(final MethodCall methodCall, final MethodChannel.Result result){
        int opt = methodCall.argument("opt");
        long duration = new Long((int)methodCall.argument("duration"));
        long startTimeStamp = new Long((int)methodCall.argument("startTimeStamp"));
        V2TIMManager.getMessageManager().setAllReceiveMessageOpt(opt, startTimeStamp, duration,  new V2TIMCallback() {
            @Override
            public void onSuccess() {
                CommonUtil.returnSuccess(result,null);
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void getAllReceiveMessageOpt(final MethodCall methodCall, final MethodChannel.Result result){
        V2TIMManager.getMessageManager().getAllReceiveMessageOpt(new V2TIMValueCallback<V2TIMReceiveMessageOptInfo>() {
            @Override
            public void onSuccess(V2TIMReceiveMessageOptInfo v2TIMReceiveMessageOptInfo) {
                CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMReceiveMessageOptInfoToMap(v2TIMReceiveMessageOptInfo));
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void addMessageReaction(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        final String reactionID = methodCall.argument("reactionID");
        getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msg) {
                V2TIMManager.getMessageManager().addMessageReaction(msg, reactionID, new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        CommonUtil.returnSuccess(result,null);
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s);
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void removeMessageReaction(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        final String reactionID = methodCall.argument("reactionID");
        getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msg) {
                V2TIMManager.getMessageManager().removeMessageReaction(msg, reactionID, new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        CommonUtil.returnSuccess(result,null);
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s);
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
    public void getMessageReactions(final MethodCall methodCall, final MethodChannel.Result result){
        List<String> msgIDList = CommonUtil.getParam(methodCall,result,"msgIDList");
        final int maxUserCountPerReaction = methodCall.argument("maxUserCountPerReaction");
        V2TIMManager.getMessageManager().findMessages(msgIDList, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                V2TIMManager.getMessageManager().getMessageReactions(v2TIMMessages, maxUserCountPerReaction, new V2TIMValueCallback<List<V2TIMMessageReactionResult>>() {
                    @Override
                    public void onSuccess(List<V2TIMMessageReactionResult> v2TIMMessageReactionResults) {
                        List<Object> list = new LinkedList<Object>();
                        for(int i = 0;i<v2TIMMessageReactionResults.size();i++){
                            list.add(CommonUtil.convertV2TIMMessageReactionResultToMap(v2TIMMessageReactionResults.get(i)));
                        }
                        CommonUtil.returnSuccess(result,list);
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s);
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                CommonUtil.returnError(result,i,s);
            }
        });
    }
    public void getAllUserListOfMessageReaction(final MethodCall methodCall, final MethodChannel.Result result){
        String msgID = methodCall.argument("msgID");
        final String reactionID = methodCall.argument("reactionID");
        final int nextSeq = methodCall.argument("nextSeq");
        final int count = methodCall.argument("count");
        getMessageByMessageID(msgID, new GetMesasgeByIDCallback() {
            @Override
            public void onSuccess(V2TIMMessage msg) {
                V2TIMManager.getMessageManager().getAllUserListOfMessageReaction(msg, reactionID, nextSeq, count, new V2TIMValueCallback<V2TIMMessageReactionUserResult>() {
                    @Override
                    public void onSuccess(V2TIMMessageReactionUserResult v2TIMMessageReactionUserResult) {
                        CommonUtil.returnSuccess(result,CommonUtil.convertV2TIMMessageReactionUserResultToMap(v2TIMMessageReactionUserResult));
                    }

                    @Override
                    public void onError(int i, String s) {
                        CommonUtil.returnError(result,i,s);
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
                CommonUtil.returnError(result,code,desc);
            }
        });
    }
}
