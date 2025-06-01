package io.effi.rpc.benchmark;

import io.effi.rpc.benchmark.model.ParentObject;
import io.effi.rpc.constant.Component;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.TypeToken;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 3)
@Fork(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SerializationTest {

    Serializer jsonSerializer;
    Serializer msgpackSerializer;

    Type type;

    byte[] jsonBytes;
    byte[] msgpackBytes;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        jsonSerializer = ExtensionLoader.loadExtension(Serializer.class, Component.Serialization.JSON);
        msgpackSerializer = ExtensionLoader.loadExtension(Serializer.class, Component.Serialization.MSGPACK);

        List<ParentObject> objList = ParentObject.getObjList();
        TypeToken<List<ParentObject>> typeToken = new TypeToken<>() {
        };
        type = typeToken.type();
        jsonBytes = jsonSerializer.serialize(objList);
        msgpackBytes = msgpackSerializer.serialize(objList);
    }

    @Benchmark
    public byte[] serializeByJson() {
        return jsonSerializer.serialize(ParentObject.getObjList());
    }

    @Benchmark
    public byte[] serializeByMsgpack() {
        return msgpackSerializer.serialize(ParentObject.getObjList());
    }

    @Benchmark
    public Object deserializeByJson() {
        return jsonSerializer.deserialize(jsonBytes, type);
    }

    @Benchmark
    public Object deserializeByMsgpack() {
        return msgpackSerializer.deserialize(msgpackBytes, type);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SerializationTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}

