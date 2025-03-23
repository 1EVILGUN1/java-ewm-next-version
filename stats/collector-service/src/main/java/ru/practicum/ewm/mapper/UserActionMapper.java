package ru.practicum.ewm.mapper;

import ru.practicum.ewm.grpc.stats.action.ActionTypeProto;
import ru.practicum.ewm.grpc.stats.action.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

public class UserActionMapper {

    public static UserActionAvro mapProtoToUserActionAvro(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = new UserActionAvro();
        userActionAvro.setUserId(userActionProto.getUserId());
        userActionAvro.setEventId(userActionProto.getEventId());
        Instant instant = Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(), userActionProto.getTimestamp().getNanos());
        userActionAvro.setTimestamp(instant);
        userActionAvro.setActionType(mapProtoToActionTypeAvro(userActionProto.getActionType()));
        return userActionAvro;
    }

    public static ActionTypeAvro mapProtoToActionTypeAvro(ActionTypeProto typeProto) {
        switch (typeProto) {
            case ActionTypeProto.ACTION_VIEW:
                return ActionTypeAvro.VIEW;
            case ActionTypeProto.ACTION_LIKE:
                return ActionTypeAvro.LIKE;
            case ActionTypeProto.ACTION_REGISTER:
                return ActionTypeAvro.REGISTER;
        }
        return null;
    }
}