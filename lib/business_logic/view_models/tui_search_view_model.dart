// ignore_for_file: constant_identifier_names

import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:tencent_im_base/tencent_im_base.dart';
import 'package:tencent_cloud_chat_uikit/data_services/friendShip/friendship_services.dart';
import 'package:tencent_cloud_chat_uikit/data_services/services_locatar.dart';
import 'package:tencent_cloud_chat_uikit/data_services/message/message_services.dart';
import 'package:tencent_cloud_chat_uikit/data_services/conversation/conversation_services.dart';
import 'package:tencent_cloud_chat_uikit/data_services/group/group_services.dart';
import 'package:tencent_cloud_chat_uikit/tencent_cloud_chat_uikit.dart';

enum KeywordListMatchType {
  V2TIM_KEYWORD_LIST_MATCH_TYPE_OR,
  V2TIM_KEYWORD_LIST_MATCH_TYPE_AND
}

class TUISearchViewModel extends ChangeNotifier {
  final FriendshipServices _friendshipServices =
      serviceLocator<FriendshipServices>();
  final MessageService _messageService = serviceLocator<MessageService>();
  final ConversationService _conversationService =
      serviceLocator<ConversationService>();
  final GroupServices _groupServices = serviceLocator<GroupServices>();

  List<V2TimFriendInfoResult>? friendList = [];

  List<V2TimMessageSearchResultItem>? msgList = [];
  int msgPage = 0;
  int totalMsgCount = 0;

  int totalMsgInConversationCount = 0;
  List<V2TimMessage> currentMsgListForConversation = [];

  List<V2TimGroupInfo>? groupList = [];

  List<V2TimConversation?> conversationList = [];

  Future<List<V2TimConversation?>?> initConversationMsg() async {
    final conversationResult = await _conversationService.getConversationList(
      nextSeq: "0",
      count: 500,
    );
    final conversationListData = conversationResult?.conversationList;
    conversationList = conversationListData ?? [];
    notifyListeners();
    return conversationListData;
  }

  void initSearch() {
    friendList = [];
    msgList = [];
    groupList = [];
    totalMsgCount = 0;
    notifyListeners();
  }

  void searchFriendByKey(String searchKey) async {
    final searchResult = await _friendshipServices.searchFriends(
      searchParam: V2TimFriendSearchParam(
        keywordList: [searchKey],
        isSearchNickName: true,
        isSearchRemark: true,
        isSearchUserID: true,
      ),
    );
    friendList = searchResult;
    notifyListeners();
  }

  void searchGroupByKey(String searchKey) async {
    final searchResult = await _groupServices.searchGroups(
        searchParam: V2TimGroupSearchParam(keywordList: [searchKey]));
    groupList = searchResult.data ?? [];
    notifyListeners();
  }

  void getMsgForConversation(String keyword, int page) async {
    void clearData() {
      currentMsgListForConversation = [];
      totalMsgInConversationCount = 0;
    }

    if (page == 0) {
      clearData();
    }
    if (keyword.isEmpty) {
      clearData();
      return;
    }
    final searchResult = await _messageService.searchLocalMessages(
        searchParam: V2TimMessageSearchParam(
      keywordList: [keyword],
      pageIndex: page,
      pageSize: 1000,
      // searchTimePeriod: 0,
      // searchTimePosition: 0,
      // conversationID: conversationId,
      type: KeywordListMatchType.V2TIM_KEYWORD_LIST_MATCH_TYPE_OR.index,
    ));
    if (searchResult.code == 0 && searchResult.data != null) {
      final messageSearchResultItems =
          searchResult.data!.messageSearchResultItems!;
      totalMsgInConversationCount = messageSearchResultItems.fold(
          0,
          (previousValue, element) =>
              (element.messageList?.length ?? 0) + previousValue);

      currentMsgListForConversation = [
        ...currentMsgListForConversation,
      ];
      for (var element in messageSearchResultItems) {
        currentMsgListForConversation.addAll(element.messageList ?? []);
      }
    }
    notifyListeners();
  }

  void searchMsgByKey(String searchKey, bool isFirst) async {
    if (isFirst == true) {
      msgPage = 0;
      msgList = [];
      totalMsgCount = 0;
    }
    final searchResult = await TencentImSDKPlugin.v2TIMManager
        .getMessageManager()
        .searchLocalMessages(
            searchParam: V2TimMessageSearchParam(
          keywordList: [searchKey],
          pageIndex: msgPage,

          pageSize: 1000,
          // searchTimePeriod: 0,
          // searchTimePosition: 0,
          type: KeywordListMatchType.V2TIM_KEYWORD_LIST_MATCH_TYPE_OR.index,
        ));
    if (searchResult.code == 0 && searchResult.data != null) {
      debugPrint(
          "searchResult ${searchResult.data?.messageSearchResultItems?.length}");
      msgPage++;
      msgList = [...?msgList, ...?searchResult.data!.messageSearchResultItems];
      totalMsgCount = searchResult.data!.totalCount ?? 0;
    }
    notifyListeners();
  }

  void searchByKey(String? searchKey) async {
    if (searchKey == null || searchKey.isEmpty) {
      friendList = [];
      groupList = [];
      msgList = [];
      totalMsgCount = 0;
      notifyListeners();
    } else {
      searchFriendByKey(searchKey);
      searchMsgByKey(searchKey, true);
      searchGroupByKey(searchKey);
    }
  }
}
