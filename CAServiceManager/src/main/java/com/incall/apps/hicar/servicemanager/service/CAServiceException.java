package com.incall.apps.hicar.servicemanager.service;

public class CAServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private int statusCode = -1;
    public CAServiceException(String msg) {
        super(msg);
    }

    public CAServiceException(Exception cause) {
        super(cause);
    }

    public CAServiceException(CAServiceErrorCode cause) {
        super(cause.toString());
    }

    public CAServiceException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public CAServiceException(String msg, Exception cause) {
        super(msg, cause);
    }

    public CAServiceException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
