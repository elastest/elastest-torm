package io.elastest.etm.service.exception;

public class TJobStoppedException extends Exception {

    private static final long serialVersionUID = -5156214804587007246L;

    public TJobStoppedException() {
    }

    public TJobStoppedException(String message) {
        super(message);
    }

    public TJobStoppedException(Throwable cause) {
        super(cause);
    }

    public TJobStoppedException(String message, Throwable cause) {
        super(message, cause);
    }

}
