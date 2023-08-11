package org.fastprintf;

public class PrintfException extends RuntimeException {

  public PrintfException(String message) {
    super(message, null, false, false);
  }

  public PrintfException(String message, Throwable cause) {
    super(message, cause);
  }

  public PrintfException(Throwable cause) {
    super(cause);
  }

  public PrintfException() {
    super();
  }
}
