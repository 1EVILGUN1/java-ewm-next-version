package ewm.event.service.validate;

import ewm.dto.event.CreateEventDto;
import ewm.dto.event.UpdateEventDto;
import ewm.error.exception.ValidationException;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventValidate {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final String EVENT_DATE_ERROR = "Событие должно начинаться не раньше чем через 2 часа.";

    public static void eventDateValidate(CreateEventDto dto, Logger log) {
        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), DATE_TIME_FORMATTER);
        validateEventDate(eventDate, log);
    }

    public static void updateEventDateValidate(UpdateEventDto dto, Logger log) {
        if (dto.getEventDate() != null) {
            validateEventDate(dto.getEventDate(), log);
        }
    }

    public static void textLengthValidate(UpdateEventDto dto, Logger log) {
        checkTextLength(dto.getDescription(), 20, 7000, "Описание", log);
        checkTextLength(dto.getAnnotation(), 20, 2000, "Краткое описание", log);
        checkTextLength(dto.getTitle(), 3, 120, "Заголовок", log);
    }

    private static void validateEventDate(LocalDateTime eventDate, Logger log) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            log.error(EVENT_DATE_ERROR);
            throw new ValidationException(EVENT_DATE_ERROR);
        }
    }

    private static void checkTextLength(String text, int min, int max, String fieldName, Logger log) {
        if (text != null && (text.length() < min || text.length() > max)) {
            String errorMessage = String.format("%s не может быть меньше %d или больше %d символов", fieldName, min, max);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}