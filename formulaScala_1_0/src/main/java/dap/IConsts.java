package dap;

/**
 * constant definitions
 */
public interface IConsts {
  // some Java types
  int BARRY = 0; // byte array
  int STRING = 1; // String
  int SHORT = 2; // short
  int INT = 3; // int
  int LONG = 4; //long
  int FLOAT = 5; // float
  int DOUBLE = 6; // double
  int DATE = 7; // java.sql.Date
  int TIME = 8; // java.sql.Time
  int TIMESTAMP = 9; // java.sql.Timestamp
  int MINTYPE = 0;
  int MAXTYPE = 9;

  // transaction commands
  int NOP = 0;
  int QUERY = 1;
  int GETROW = 2;
  int UPDATE = 3;
  int PREPARE = 4;
  int SETCOLTYPE = 5;
  int PQUERY = 6;
  int PUPDATE = 7;
  int CLOSERS = 8;
  int CLOSESTMT = 9;
  int CLOSECON = 10;
  int AUTOCOMMIT =11;
  int COMMIT = 12;
  int ROLLBACK = 13;
  int BYE = 14;
  int URL = 15; // connection request
  int SETCHARCONVERSION = 16;
  int GARBAGECOLLECTION = 17;

  // result codes
  int OK = 0;
  int SQLERROR = -1;
  int UNKNOWN_COMMAND = -2;
  int INVALID_CONNECTION = -3;
  int INVALID_STATEMENT = -4;
  int INVALID_RESULTSET = -5;
  int INVALID_ID = -6;
  int NO_TYPE_SPECIFIED = -7;
  int NO_DATA_REMAIN = -8;
  int PARMS_MISMATCH = -9;
  int IOERROR = -10;
}
