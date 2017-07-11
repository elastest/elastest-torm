package io.elastest.etm.commons;

public class ElasTestException extends RuntimeException {

  private static final long serialVersionUID = 2319005818919493142L;

  /**
   * default constructor.
   */
  public ElasTestException() {
    // Default constructor
  }

  /**
   * Constructs a new runtime exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to initCause.
   *
   * @param msg
   *          the detail message. The detail message is saved for later retrieval by the
   *          {@link #getMessage()} method.
   */
  public ElasTestException(final String msg) {
    super(msg);
  }

  /**
   *
   * @param msg
   *          the detail message. The detail message is saved for later retrieval by the
   *          {@link #getMessage()} method.
   * @param throwable
   *          the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
   *          null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public ElasTestException(final String msg, final Throwable throwable) {
    super(msg, throwable);
  }

  /**
   *
   * @param throwable
   *          the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
   *          null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public ElasTestException(final Throwable throwable) {
    super(throwable);
  }

}
