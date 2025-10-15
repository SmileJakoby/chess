package service;

/**
 * Indicates the request is missing required fields
 */
public class UnauthorizedException extends Exception{
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(String message, Throwable ex) {
        super(message, ex);
    }
}
