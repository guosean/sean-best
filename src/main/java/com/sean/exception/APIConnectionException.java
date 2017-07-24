package com.sean.exception;

public class APIConnectionException extends Exception {
    private static final long serialVersionUID = 1L;
    private boolean readTimedout = false;
    private int doneRetriedTimes = 0;

    public APIConnectionException(String message, Throwable e) {
        super(message, e);
    }

    public APIConnectionException(String message, Throwable e, int doneRetriedTimes) {
        super(message, e);
        this.doneRetriedTimes = doneRetriedTimes;
    }

    public APIConnectionException(String message, Throwable e, boolean readTimedout) {
        super(message, e);
        this.readTimedout = readTimedout;
    }

    public boolean isReadTimedout() {
        return readTimedout;
    }

    public int getDoneRetriedTimes() {
        return this.doneRetriedTimes;
    }
}
