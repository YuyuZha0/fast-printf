package io.fastprintf;

public class PrintfSyntaxException extends IllegalArgumentException {

  private static final String nl = System.lineSeparator();
  private final String desc;
  private final String pattern;
  private final int index;

  /**
   * Constructs a new instance of this class.
   *
   * @param desc A description of the error
   * @param pattern The erroneous pattern
   * @param index The approximate index in the pattern of the error, or -1 if the index is not known
   */
  public PrintfSyntaxException(String desc, String pattern, int index) {
    this.desc = desc;
    this.pattern = pattern;
    this.index = index;
  }

  /**
   * Retrieves the error index.
   *
   * @return The approximate index in the pattern of the error, or -1 if the index is not known
   */
  public int getIndex() {
    return index;
  }

  /**
   * Retrieves the description of the error.
   *
   * @return The description of the error
   */
  public String getDescription() {
    return desc;
  }

  /**
   * Retrieves the erroneous regular-expression pattern.
   *
   * @return The erroneous pattern
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Returns a multi-line string containing the description of the syntax error and its index, the
   * erroneous regular-expression pattern, and a visual indication of the error index within the
   * pattern.
   *
   * @return The full detail message
   */
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append(desc);
    if (index >= 0) {
      sb.append(" near index ");
      sb.append(index);
    }
    sb.append(nl);
    sb.append(pattern);
    if (index >= 0 && pattern != null && index < pattern.length()) {
      sb.append(nl);
      for (int i = 0; i < index; i++) sb.append(' ');
      sb.append('^');
    }
    return sb.toString();
  }
}
