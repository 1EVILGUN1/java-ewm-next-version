package ru.practicum.serialize;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.exception.DeserializationException;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private DecoderFactory decoderFactory;
    private DatumReader<T> reader;


    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        reader = new SpecificDatumReader<>(schema);
    }

    // ...

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;

            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return this.reader.read(null, decoder);


        } catch (Exception e) {
            throw new DeserializationException("Ошибка десериализации данных из топика [" + topic + "]", e);
        }
    }
}