package ewm.model;

import lombok.Data;

import java.util.Set;

@Data
public class Subscription {

    private Long userId;

    private Set<Subscriber> subscribed;

    private Set<BlackList> blackList;

}
