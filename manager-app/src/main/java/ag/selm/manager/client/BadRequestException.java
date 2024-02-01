package ag.selm.manager.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BadRequestException extends RuntimeException {

    private final List<String> errors;

    public BadRequestException(List<String> errors) {
        this.errors = errors;
    }

    public BadRequestException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public BadRequestException(String message, Throwable cause, List<String> errors) {
        super(message, cause);
        this.errors = errors;
    }

    public BadRequestException(Throwable cause, List<String> errors) {
        super(cause);
        this.errors = errors;
    }

    public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<String> errors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errors = errors;
    }
}
