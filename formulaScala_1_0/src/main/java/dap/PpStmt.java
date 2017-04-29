package dap;

/**
PpStmt - prepared statementとINパラメータのタイプの組を保持するクラス
*/

import java.sql.*;

class PpStmt {
  private PreparedStatement ppstmt;
  private int[] inTypes;

  PreparedStatement getStmt() {
    return ppstmt;
  }
  int getType(int index) {
    return inTypes[index];
  }
  int getTypeNum() {
    return inTypes.length;
  }
  void setStmt(PreparedStatement p) {
    ppstmt = p;
  }
  void setTypes(int[] types) {
    inTypes = types;
  }
}
