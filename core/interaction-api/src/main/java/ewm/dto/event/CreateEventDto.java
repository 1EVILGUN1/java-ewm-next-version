package ewm.dto.event;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class CreateEventDto {

	@NotBlank(message = "Краткое описание события не может быть пустым")
	@Size(min = 20, max = 2000, message = "Краткое описание не может быть меньше 20 или больше 2000 символов")
	private String annotation;

	@NotNull(message = "Категория должна быть указана")
	private Long category;

	@NotBlank(message = "Полное описание события не может быть пустым")
	@Size(min = 20, max = 7000, message = "Описание не можеть быть меньше 20 или больше 7000 символов")
	private String description;

	@NotNull(message = "Время события должно быть указано")
	private String eventDate;

	@NotNull(message = "Место проведения события должно быть указано")
	private LocationDto location;

	private Boolean paid;

	@PositiveOrZero
	private Integer participantLimit;

	private Boolean requestModeration;

	@NotBlank(message = "Заголовок события не может быть пустым")
	@Size(min = 3, max = 120, message = "Заголовок не может быть меньше 3 или больше 120 символов")
	private String title;
}
