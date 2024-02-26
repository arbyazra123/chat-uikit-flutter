class TUICallback {
  const TUICallback({this.onSuccess, this.onError});

  final void Function()? onSuccess;
  final void Function(int code, String message)? onError;
}
