package ewm.dto.subscription;

import ewm.dto.user.UserDto;
import lombok.Data;

import java.util.Set;

@Data
public class SubscriptionDto {
    private Set<UserDto> subscribers;
    private Set<UserDto> blackList;
}
