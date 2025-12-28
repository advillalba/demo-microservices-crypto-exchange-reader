package run.buildspace.cryptoreader.domain.exception;

public class WSException extends RuntimeException {
    public WSException(String message) {
        super(message);
    }
    public WSException(String message, Throwable cause) {
        super(message, cause);
    }
}
