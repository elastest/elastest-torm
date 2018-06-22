package io.elastest.etm.service;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 3041175634082167400L;
    private int code = 0;
    
    public enum ExceptionCode {
        ERROR_PROVISIONING_VM(1),
        ERROR_DEPROVISIONING_VM(2),
        GENERIC_ERROR(10);

        private int code;

        public int getCode() {
            return code;
        }

        private ExceptionCode(int code) {
          this.code = code;
         }
    }

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message, ExceptionCode code) {
        super(message);
        this.code = code.getCode();
    }
    
    public ServiceException(String message, Throwable cause, ExceptionCode code) {
        super(message, cause);
        this.code = code.getCode();
    }    
    
    public int getCode() {
        return code;
    }

}
