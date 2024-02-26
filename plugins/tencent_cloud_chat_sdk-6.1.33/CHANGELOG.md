


## 6.1.33
*  remove write log 

## 6.1.32
*  change create group approveopt default value to forbid

## 6.1.31
*  add application type

## 6.1.29
*  add create group approve opt support

## 6.1.28
*  add is peer read field

## 6.1.27
*  add getwidget for plugin

## 6.1.21
* fix download bug 

## 6.1.20
* change topicfaceurl to faceurl


## 6.1.16
* fix get history by time bug

## 6.1.15
* fix marktype bug

## 6.1.14
* Compatible with lower version java sdk

## 6.1.13
* remove uninitSDK before initSDK

## 6.1.12
* add plugin support

## 6.1.8 & 6.1.9 & 6.1.10 & 6.1.11
* add default unreadcount filter is false;

## 6.1.6 & 6.1.7
* add get history message by time support

## 6.1.5
* add get history message by time support


## 6.1.4
* upgrade interface

## 6.1.3
* add send message config

## 6.1.2
* add donwload temp file

## 6.1.1
* add groupinfo change int value

## 6.0.9 && 6.1.0
* fix fileelem local url bug

## 6.0.8
* fix getCOnversationListByFilter bug on android

## 6.0.6
* add example and kickgroupmember duration param

## 6.0.3
* add delete converastion list

## 6.0.2
* fix getMessageById bug

## 6.0.0
* Non-friend user profile update monitoring
* Non-friend user profile update callback
* Callback for banning all members of the group
* Group member mark & ​​group member mark callback
* Added message response interface and callback
* Added message recall with recall information callback
* Set global message receiving options Complete
* Message cloud search
* Session delete callback
* The session performs unread statistics callback according to the specified classification.
* Delete sessions in batches
* Go back to session unread by category Done
* Listen to session unread changes according to the specified type
* Clear session unreads (markxxxAsRead interface is deprecated)
* Improve offline push fields
* speech to text

## 5.2.5 && 5.2.6
* Compatible with lower versions of flutter

## 5.2.4
* add message hasRiskContent feild

## 5.2.2 && 5.2.3
* avchat room support find message bug fix

## 5.2.1
* avchat room support find message

## 5.2.0
* update interface

## 5.1.9
* fix log bug

## 5.1.8
* fix create topic bug

## 5.1.7
* fix create avchatroom bug

## 5.1.6
* update native dep

## 5.1.5
* update interface

## 5.1.3
* message extension bug fix
* file path exsit logic

## 5.1.3
* Support multiple listeners to register and remove multiple times
* Support upgrade to the underlying SDK to the latest plus version
* fix bugs

## 5.0.9
* Support setting voip
* Support quic acceleration & local database encryption
* Taking into account the bug that the web sends the file and then downloads the native file without the file.
* Fix some bugs on the desktop

## 5.0.8
* Added: group counting capability, common group and live group support group counter meta counter, for details, please refer to groupCounter related API
* Added: text message translation capability, see [translateText](https://cloud.tencent.com/document/product/269/85380) for details.
* Upgrade: Upgrade Native SDK to 7.0


## 5.0.6
* update native sdk

## 5.0.4
* Migration from tencent_im_sdk_plugin

## 5.0.2
* [Incompatible update] Multimedia messages no longer return url by default, and need to be obtained through getMessageOnlineUrl
* [Partially incompatible update] Multimedia messages will not return localurl by default, and will only return after the message is successfully downloaded through downloadMessage
* Add onMessageDownloadProgressCallback to advanceMessageListener, which will be triggered when the multimedia message download progress is updated
* The disableBadgeNumber method is added on the ios side. After calling, the IMSDK is in the background of the application, and the application badge will not be set by default.
* Optimized the problem of channel instance coverage in multiple flutter engine scenarios
* The bottom dynamic library download logic is optimized on the PC side
* Upgrade the underlying SDK to 6.8

## 4.2.0
* Fix invite api miss offlinepushInfo


## 4.1.9
* Fix high version jdk conversion problem
* Support macOS and Windows
* Upgrade the underlying SDK
* Support message extension
* Support signaling editing
* Fixed several issues

## 4.1.3
* flutter for web 

## 4.1.1+2
* Upgrade native SDK to 6.6.x
* web signal support
* flutter for web support

## 4.1.0
* Upgrade native SDK
* Fix iOS search group member bug
* web sdk only supports the latest version

## 4.0.8-bugfix
* fix modifyMessage bug on Android

## 4.0.8
* Added an advanced interface for obtaining sessions, which supports pulling sessions by session type, tag, and grouping
* Support marked sessions, such as star, fold, hide, etc.
* Support setting session custom fields
* Support session grouping
* The SDK dependency flutter version is reduced to 2.0.0
* Support multiple flutter engines
* Offline push support to configure Android push sound
* Support subscriber online status change by user id
* Fix the bug that the group information cannot be found in the topic group
* Upgrade the native sdk version to 6.5

## 4.0.7
* ios newly added front-end and back-end api, cut back-end can set the unread to the corner mark
* Optimize group application processing logic

## 4.0.5
* Fix doBackgroup bug

## 4.0.5
* Fix upload token bug

## 4.0.4
* Support user online status query
* Get the list of historical messages and support pulling by message type
* Fix thread safety issues in special cases
* Support sending multi-element messages

## 4.0.3-bugfix
* fix InitSDKListener bug

## 4.0.2
* Local video url bug fix

## 4.0.1
* Added topic related interface
* Added message editing interface

## 4.0.0
* Upgrade the underlying SDK version to 6.2.x
* fix offlinePush info bug

## 3.9.3
* Upgrade the underlying SDK version to 6.2.x
* Fix the problem that the group ban group tips boolValue is lost
* Fixed the problem that the nameCard field was not parsed for session instances
* Added group read receipt related interface
* flutter for web perfect

## 3.9.2
* Upgrade the ios library version to 6.1.2155.1

## 3.9.1
* Upgrade the underlying library version to 6.1.2155

## 3.9.0
* Modify grouplistener

## 3.8.9
* Monitor registration problem fix

## 3.8.8
* Monitor registration problem fix

## 3.8.7
* Modify add friends enumeration
