package run.buildspace.cryptoreader.domain.exception;

public class MessagePublishException extends RuntimeException {

    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
