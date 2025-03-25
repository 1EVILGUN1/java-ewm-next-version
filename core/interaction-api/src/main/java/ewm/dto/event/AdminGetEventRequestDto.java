package ewm.dto.event;

import ewm.enums.EventState;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminGetEventRequestDto {

	private List<Long> users;

	private List<EventState> states;

	private List<Long> categories;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime rangeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime rangeEnd;

	@PositiveOrZero
	@Builder.Default
	private int from = 0;

	@Positive
	@Builder.Default
	private int size = 10;
}
