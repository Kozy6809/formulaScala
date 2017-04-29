package dap;

/**
DapServer - サーバーソケットをオープンし、クライアントの接続を待つ
現状のコードには終了条件が組み込まれていないことに注意
*/

import java.io.*;
import java.net.*;
//import ibm.sql.*;
import java.sql.*;

class DapServer {
  /*
    static {
    try {
    Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
    } catch (Exception e) {
    System.out.println(e.getMessage());
    }
    try {
    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
    } catch (ClassNotFoundException e) {
    System.out.println(e.getMessage());
    }
    }
  */
  public static void main(String[] args) {
    // set timezone to GMT+9:00
    java.util.TimeZone.getDefault().setRawOffset(32400000);
    ServerSocket serverSocket = null;
    boolean listening = true;

    try {
      serverSocket = new ServerSocket(4444);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.out.println("database server invoked");

    while (listening) {
      Socket socket;
      try {
	socket = serverSocket.accept();
	System.out.println("connection from " + socket.getInetAddress() +" accepted");
	new Thread(new Dap(socket)).start();
      } catch (IOException e) {
	System.err.println(e.getMessage());
	continue;
      }
    }

    try {
      serverSocket.close();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
}
