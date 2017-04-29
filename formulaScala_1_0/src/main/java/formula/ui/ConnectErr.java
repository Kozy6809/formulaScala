package formula.ui;

/**
 * この型は VisualAge で作成されました。
 */
public class ConnectErr extends java.awt.Frame implements java.awt.event.ActionListener, java.awt.event.WindowListener {
  private java.awt.Label ivjLabel1 = null;
  private java.awt.Label ivjLabel2 = null;
  private java.awt.Button ivjOK = null;
  private java.awt.Panel ivjPanel1 = null;
  private java.awt.Panel ivjPanel2 = null;
  private java.awt.Button ivjShit = null;
  /**
   * コンストラクター
   */
  /* 警告 : このメソッドは再生成されます。 */
  public ConnectErr() {
    super();
    initialize();
  }
  /**
   * ConnectErr コンストラクター・コメント。
   * @param title java.lang.String
   */
  public ConnectErr(String title) {
    super(title);
  }
  /**
   * ActionListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.ActionEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void actionPerformed(java.awt.event.ActionEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    if ((e.getSource() == getOK()) ) {
      connEtoC2(e);
    }
    if ((e.getSource() == getShit()) ) {
      connEtoC3(e);
    }
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * connEtoC1:  (ConnectErr.window.windowClosing(java.awt.event.WindowEvent) --> ConnectErr.dispose()V)
   * @param arg1 java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  private void connEtoC1(java.awt.event.WindowEvent arg1) {
    try {
      // ユーザー・コード開始 {1}
      // ユーザー・コード終了
      this.dispose();
      // ユーザー・コード開始 {2}
      // ユーザー・コード終了
    } catch (java.lang.Throwable ivjExc) {
      // ユーザー・コード開始 {3}
      // ユーザー・コード終了
      handleException(ivjExc);
    }
  }
  /**
   * connEtoC2:  (OK.action.actionPerformed(java.awt.event.ActionEvent) --> ConnectErr.exit(I)V)
   * @param arg1 java.awt.event.ActionEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  private void connEtoC2(java.awt.event.ActionEvent arg1) {
    try {
      // ユーザー・コード開始 {1}
      // ユーザー・コード終了
      this.exit(1);
      // ユーザー・コード開始 {2}
      // ユーザー・コード終了
    } catch (java.lang.Throwable ivjExc) {
      // ユーザー・コード開始 {3}
      // ユーザー・コード終了
      handleException(ivjExc);
    }
  }
  /**
   * connEtoC3:  (Shit.action.actionPerformed(java.awt.event.ActionEvent) --> ConnectErr.exit(I)V)
   * @param arg1 java.awt.event.ActionEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  private void connEtoC3(java.awt.event.ActionEvent arg1) {
    try {
      // ユーザー・コード開始 {1}
      // ユーザー・コード終了
      this.exit(2);
      // ユーザー・コード開始 {2}
      // ユーザー・コード終了
    } catch (java.lang.Throwable ivjExc) {
      // ユーザー・コード開始 {3}
      // ユーザー・コード終了
      handleException(ivjExc);
    }
  }
  /**
   * システムを終了する。従ってConnectErrが表示されるまでには、その他の全ての終了処理が
   * 終わっていなければならない。
   * @param rc int
   */
  private void exit(int rc) {
    System.exit(rc);
  }
  /**
   * Label1 のプロパティー値を戻します。
   * @return java.awt.Label
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Label getLabel1() {
    if (ivjLabel1 == null) {
      try {
	ivjLabel1 = new java.awt.Label();
	ivjLabel1.setName("Label1");
	ivjLabel1.setFont(new java.awt.Font("dialog", 0, 18));
	ivjLabel1.setText("接続できませんでした");
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjLabel1;
  }
  /**
   * Label2 のプロパティー値を戻します。
   * @return java.awt.Label
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Label getLabel2() {
    if (ivjLabel2 == null) {
      try {
	ivjLabel2 = new java.awt.Label();
	ivjLabel2.setName("Label2");
	ivjLabel2.setFont(new java.awt.Font("dialog", 0, 14));
	ivjLabel2.setText("原因を確認の上、もう一度実行して下さい");
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjLabel2;
  }
  /**
   * OK のプロパティー値を戻します。
   * @return java.awt.Button
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Button getOK() {
    if (ivjOK == null) {
      try {
	ivjOK = new java.awt.Button();
	ivjOK.setName("OK");
	ivjOK.setFont(new java.awt.Font("dialog", 0, 14));
	ivjOK.setLabel(" OK ");
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjOK;
  }
  /**
   * Panel1 のプロパティー値を戻します。
   * @return java.awt.Panel
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Panel getPanel1() {
    if (ivjPanel1 == null) {
      try {
	ivjPanel1 = new java.awt.Panel();
	ivjPanel1.setName("Panel1");
	ivjPanel1.setLayout(new java.awt.FlowLayout());
	getPanel1().add(getOK(), getOK().getName());
	getPanel1().add(getShit(), getShit().getName());
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjPanel1;
  }
  /**
   * Panel2 のプロパティー値を戻します。
   * @return java.awt.Panel
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Panel getPanel2() {
    java.awt.GridBagConstraints constraintsLabel1 = new java.awt.GridBagConstraints();
    java.awt.GridBagConstraints constraintsLabel2 = new java.awt.GridBagConstraints();
    if (ivjPanel2 == null) {
      try {
	ivjPanel2 = new java.awt.Panel();
	ivjPanel2.setName("Panel2");
	ivjPanel2.setLayout(new java.awt.GridBagLayout());

	constraintsLabel1.gridx = 0; constraintsLabel1.gridy = 0;
	constraintsLabel1.gridwidth = 1; constraintsLabel1.gridheight = 1;
	constraintsLabel1.anchor = java.awt.GridBagConstraints.CENTER;
	constraintsLabel1.weightx = 1.0;
	constraintsLabel1.weighty = 1.0;
	getPanel2().add(getLabel1(), constraintsLabel1);

	constraintsLabel2.gridx = 0; constraintsLabel2.gridy = 1;
	constraintsLabel2.gridwidth = 1; constraintsLabel2.gridheight = 1;
	constraintsLabel2.anchor = java.awt.GridBagConstraints.CENTER;
	constraintsLabel2.weightx = 1.0;
	constraintsLabel2.weighty = 1.0;
	getPanel2().add(getLabel2(), constraintsLabel2);
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjPanel2;
  }
  /**
   * Shit のプロパティー値を戻します。
   * @return java.awt.Button
   */
  /* 警告 : このメソッドは再生成されます。 */
  private java.awt.Button getShit() {
    if (ivjShit == null) {
      try {
	ivjShit = new java.awt.Button();
	ivjShit.setName("Shit");
	ivjShit.setFont(new java.awt.Font("dialog", 0, 14));
	ivjShit.setLabel("SHIT!");
	// ユーザー・コード開始 {1}
	// ユーザー・コード終了
      } catch (java.lang.Throwable ivjExc) {
	// ユーザー・コード開始 {2}
	// ユーザー・コード終了
	handleException(ivjExc);
      }
    };
    return ivjShit;
  }
  /**
   * パーツが例外を送出するたびに呼び出されます。
   * @param exception java.lang.Throwable
   */
  private void handleException(Throwable exception) {

    /* キャッチされていない例外を標準出力に出力するには、次の行をコメント解除します */
    // System.out.println("--------- キャッチされていない例外 ---------");
    // exception.printStackTrace(System.out);
  }
  /**
   * 接続の初期化
   */
  /* 警告 : このメソッドは再生成されます。 */
  private void initConnections() {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    this.addWindowListener(this);
    getOK().addActionListener(this);
    getShit().addActionListener(this);
  }
  /**
   * クラスを初期化します。
   */
  /* 警告 : このメソッドは再生成されます。 */
  private void initialize() {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    setName("ConnectErr");
    setLayout(new java.awt.BorderLayout());
    setSize(520, 330);
    setTitle("処方データベース：エラー");
    add(getPanel1(), "South");
    add(getPanel2(), "Center");
    initConnections();
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * メイン・エントリーポイント - アプリケーションとして実行された場合にパーツを始動
   * @param args java.lang.String[]
   */
  public static void main(java.lang.String[] args) {
    try {
      ConnectErr aConnectErr;
      aConnectErr = new ConnectErr();
      try {
	Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
	Class parmTypes[] = { java.awt.Window.class };
	Object parms[] = { aConnectErr };
	java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
	aCtor.newInstance(parms);
      } catch (java.lang.Throwable exc) {};
      aConnectErr.setVisible(true);
    } catch (Throwable exception) {
      System.err.println("java.awt.Frame の main() で例外が発生しました");
      exception.printStackTrace(System.out);
    }
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowActivated(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowClosed(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowClosing(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    if ((e.getSource() == this) ) {
      connEtoC1(e);
    }
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowDeactivated(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowDeiconified(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowIconified(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
  /**
   * WindowListener インターフェースのイベントを処理するメソッド。
   * @param e java.awt.event.WindowEvent
   */
  /* 警告 : このメソッドは再生成されます。 */
  public void windowOpened(java.awt.event.WindowEvent e) {
    // ユーザー・コード開始 {1}
    // ユーザー・コード終了
    // ユーザー・コード開始 {2}
    // ユーザー・コード終了
  }
}
