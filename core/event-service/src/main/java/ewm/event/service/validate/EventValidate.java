package ewm.event.service.validate;

import ewm.dto.event.CreateEventDto;
import ewm.dto.event.UpdateEventDto;
import ewm.error.exception.ValidationException;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventValidate {

    public static void eventDateValidate(CreateEventDto dto, Logger log) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dto.getEventDate(), formatter);
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            String messageError = "Событие должно начинаться не раньше чем через 2 часа.";
            log.error(messageError);
            throw new ValidationException(messageError);
        }
    }

    public static void updateEventDateValidate(UpdateEventDto dto, Logger log) {
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                String messageError = "Событие должно начинаться не раньше чем через 2 часа.";
                log.error(messageError);
                throw new ValidationException(messageError);
            }
        }
    }

    public static void textLengthValidate(UpdateEventDto dto, Logger log) {
        if (dto.getDescription() != null) {
            checkLength(dto.getDescription(), 20, 7000, "Описание", log);
        }
        if (dto.getAnnotation() != null) {
            checkLength(dto.getAnnotation(), 20, 2000, "Краткое описание", log);
        }
        if (dto.getTitle() != null) {
            checkLength(dto.getTitle(), 3, 120, "Заголовок", log);
        }
    }

    private static void checkLength(String text, int min, int max, String name, Logger log) {
        if (text.length() < min || text.length() > max) {
            String messageError = String.format("%s не может быть меньше %d или больше %d символов", name, min, max);
            log.error(messageError);
            throw new ValidationException(messageError);
        }
    }
}
