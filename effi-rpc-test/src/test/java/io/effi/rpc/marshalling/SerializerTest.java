package io.effi.rpc.marshalling;

import com.google.protobuf.StringValue;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.serialization.jdk.JdkSerializer;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.serialization.kryo.KryoSerializer;
import io.effi.rpc.serialization.protobuf.ProtobufSerializer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializerTest {

    @Test
    void jacksonDeserializesPositionalArgumentsByDeclaredType() throws IOException {
        JacksonSerializer serializer = new JacksonSerializer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(new Object[]{"tom", 18}, out);

        Object[] values = serializer.deserialize(
                new ByteArrayInputStream(out.toByteArray()),
                new Type[]{String.class, Integer.class}
        );

        assertArrayEquals(new Object[]{"tom", 18}, values);
    }

    @Test
    void jdkRoundTripsAllowedType() throws IOException {
        JdkSerializer serializer = new JdkSerializer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(new Payload("tom", 18), out);

        Payload payload = serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), Payload.class);

        assertEquals(new Payload("tom", 18), payload);
    }

    @Test
    void jdkRejectsTypeOutsideAllowedPackages() throws IOException {
        JdkSerializer serializer = new JdkSerializer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(new java.awt.Point(1, 2), out);

        EffiRpcException failure = assertThrows(
                EffiRpcException.class,
                () -> serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), java.awt.Point.class)
        );

        assertEquals(MarshallingErrorCodes.DESERIALIZE, failure.errorCode());
    }

    @Test
    void kryoRoundTripsRegisteredType() throws IOException {
        KryoSerializer serializer = new KryoSerializer();
        serializer.register(Payload.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(new Payload("tom", 18), out);

        Payload payload = serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), Payload.class);

        assertEquals(new Payload("tom", 18), payload);
    }

    @Test
    void protobufRoundTripsMessageLite() throws IOException {
        ProtobufSerializer serializer = new ProtobufSerializer();
        StringValue message = StringValue.of("tom");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(message, out);

        StringValue value = serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), StringValue.class);

        assertEquals(message, value);
    }

    public static final class Payload implements Serializable {

        private String name;

        private int age;

        public Payload() {
        }

        public Payload(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Payload payload)) {
                return false;
            }
            return age == payload.age && Objects.equals(name, payload.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
