package ewm.error;

import ewm.error.exception.ConflictException;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.error.exception.ValidationException;
import ewm.error.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class})
    public ErrorResponse handleDateTimeParseException(MethodArgumentNotValidException e) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST,
                "Ошибка Валидации",
                e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        return new ErrorResponse(HttpStatus.NOT_FOUND, "Сущность не найдена", e.getMessage());
    }


    @ExceptionHandler({ExistException.class, ConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleParameterConflict(final Exception e) {
        return new ErrorResponse(HttpStatus.CONFLICT,
                "Ошибка уникальности",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception e) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Произошла непредвиденная ошибка.",
                e.getMessage()
        );
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                e.getMessage()
        );
    }
}
