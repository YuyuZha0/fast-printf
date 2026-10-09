package io.fastprintf;

import io.fastprintf.util.Utils;

public class PrintfException extends RuntimeException {

  public PrintfException(String message, Object... args) {
    super(Utils.lenientFormat(message, args), null, false, false);
  }

  public PrintfException(String message) {
    super(message, null, false, false);
  }

  public PrintfException(Throwable cause, String message, Object... args) {
    super(Utils.lenientFormat(message, args), cause);
  }

  public PrintfException(Throwable cause, String message) {
    super(message, cause);
  }

  public PrintfException(Throwable cause) {
    super(cause);
  }

  public PrintfException() {
    super();
  }
}
