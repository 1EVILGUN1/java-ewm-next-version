package ewm.dto.request;

import ewm.model.request.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private RequestStatus status;
}
