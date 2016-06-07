package com.wangp.base.parser.exception;

/**
 * Created by wangpeng627 on 16-3-10.
 */
public class FileParseException extends RuntimeException {
    public FileParseException() {
        super();
    }

    public FileParseException(String message) {
        super(message);
    }

    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileParseException(Throwable cause) {
        super(cause);
    }
}
