package formula;

/**
 * MatDeterminerによって非同期に検索された結果の通知を受け取るための
 * インターフェース
 */
public interface MatDeterminListener {
  void matDetermined(boolean canceled, int code, String name, int status);
}
