package ewm.error.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorResponse {

    HttpStatus status;
    String reason;
    String message;
    LocalDateTime timestamp;
    List<String> errors;

    public ErrorResponse(HttpStatus status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(HttpStatus status, String reason, String message, List<String> errors) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
