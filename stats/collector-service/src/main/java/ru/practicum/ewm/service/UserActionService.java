package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.action.UserActionProto;
import ru.practicum.ewm.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionService {

    @Value("${kafka.topic}")
    private String topic;

    private final KafkaTemplate kafkaTemplate;

    public void collectUserAction(UserActionProto request) {
        UserActionAvro userActionAvro = UserActionMapper.mapProtoToUserActionAvro(request);
        ProducerRecord<String, UserActionAvro> userActionRecord = new ProducerRecord<>(topic, userActionAvro);
        kafkaTemplate.send(userActionRecord);
    }
}
