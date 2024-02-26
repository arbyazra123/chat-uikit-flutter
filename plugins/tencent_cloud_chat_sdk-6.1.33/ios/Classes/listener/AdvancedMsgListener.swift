//
//  AdvancedMsgListener.swift
//  tencent_im_sdk_plugin



import Foundation
import ImSDK_Plus
import Hydra

class AdvancedMsgListener: NSObject, V2TIMAdvancedMsgListener {
    let listenerUuid:String;
    init(listenerUid: String) {
        listenerUuid = listenerUid;
    }
	/// 新消息通知
	public func onRecvNewMessage(_ msg: V2TIMMessage!) {
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvNewMessage, method: "advancedMsgListener", data: V2MessageEntity.init(message: msg).getDict(), listenerUuid: self.listenerUuid)
        
        if( MessageManager.groupidList.contains(msg.groupID ?? "")){
            let allowTypes:[V2TIMElemType] = [V2TIMElemType.ELEM_TYPE_FILE,V2TIMElemType.ELEM_TYPE_IMAGE,V2TIMElemType.ELEM_TYPE_SOUND,V2TIMElemType.ELEM_TYPE_VIDEO]
            if(allowTypes.contains(msg.elemType)){
                if(MessageManager.eachGroupMessageNums < MessageManager.messageList.count){
                    MessageManager.messageList.append(msg)
                }else{
                    MessageManager.messageList.remove(at: 0)
                    MessageManager.messageList.append(msg)
                }
            }
        }
	}
    
    public func onRecvMessageRead(_ receiptList: [V2TIMMessageReceipt]!) {
        var data: [[String: Any]] = [];
        for item in receiptList {
            data.append(V2MessageReceiptEntity.getDict(info: item));
        }
        
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageReadReceipts, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
    }
	
	/// C2C已读回执
	public func onRecvC2CReadReceipt(_ receiptList: [V2TIMMessageReceipt]!) {
		var data: [[String: Any]] = [];
		for item in receiptList {
			data.append(V2MessageReceiptEntity.getDict(info: item));
		}
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvC2CReadReceipt, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
	}
	
	/// 消息撤回
	public func onRecvMessageRevoked(_ msgID: String!) {
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageRevoked, method: "advancedMsgListener", data: msgID, listenerUuid: listenerUuid)
	}
	

    public func onRecvMessageModified(_ msg: V2TIMMessage!) {
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageModified, method: "advancedMsgListener", data: V2MessageEntity.init(message: msg).getDict(), listenerUuid: self.listenerUuid)
	}
    public func onRecvMessageExtensionsChanged(_ msgID: String!, extensions: [V2TIMMessageExtension]!) {
        var data = [String:Any]();
        data["msgID"] = msgID ?? "";
        var resList = [[String:Any]]();
        for res:V2TIMMessageExtension in extensions ?? [] {
            var resItem = [String: Any]();
            resItem["extensionKey"] = res.extensionKey as Any;
            resItem["extensionValue"] = res.extensionValue as Any;
            resList.append(resItem);
        }
        data["extensions"] = resList;
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageExtensionsChanged, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
    }
    public func onRecvMessageExtensionsDeleted(_ msgID: String!, extensionKeys: [String]!) {
        var data = [String:Any]();
        data["msgID"] = msgID ?? "";
        data["extensionKeys"] = extensionKeys;
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageExtensionsDeleted, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
    }
    func onRecvMessageReactionsChanged(_ changeList: [V2TIMMessageReactionChangeInfo]!) {
        var data = [String:Any]();
        var resList = [[String:Any]]();
        for res:V2TIMMessageReactionChangeInfo in changeList ?? [] {
            var resItem = [String: Any]();
            resItem["messageID"] = res.msgID as Any;
            var reactionList = [[String:Any]]();
            for rec:V2TIMMessageReaction in res.reactionList ?? [] {
                var reaction = [String:Any]();
                reaction["reactionID"] = rec.reactionID;
                reaction["reactedByMyself"] = rec.reactedByMyself;
                reaction["totalUserCount"] = rec.totalUserCount;
                var partialUserList = [[String:Any]]();
                for uif:V2TIMUserInfo in rec.partialUserList ?? [] {
                    var uini = [String:Any]();
                    uini["userID"] = uif.userID;
                    uini["faceUrl"] = uif.faceURL;
                    uini["nickName"] = uif.nickName;
                    partialUserList.append(uini)
                }
                reaction["partialUserList"] = partialUserList;
                reactionList.append(reaction)
            }
            resItem["reactionList"] = reactionList as Any;
            resList.append(resItem);
        }
        data["changeInfos"] = resList;
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageReactionsChanged, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
    }
    func onRecvMessageRevoked(_ msgID: String!, operateUser: V2TIMUserFullInfo!, reason: String!) {
        
        var data = [String:Any]();
        data["msgID"] = msgID ?? "";
        data["reason"] = reason ?? "";
        data["operateUser"] = V2UserFullInfoEntity.getDict(info: operateUser);
        TencentImSDKPlugin.invokeListener(type: ListenerType.onRecvMessageRevokedWithInfo, method: "advancedMsgListener", data: data, listenerUuid: listenerUuid)
    }
}
