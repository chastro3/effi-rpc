package io.effi.rpc.benchmark;

import io.effi.rpc.benchmark.model.ParentObject;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.util.TypeCapture;
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

import java.io.ByteArrayOutputStream;
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

    Type type;

    byte[] jsonBytes;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SerializationTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //    @Benchmark
    //    public byte[] serializeByJson() {
    //        return jsonSerializer.serialize(ParentObject.getObjList());
    //    }
    //
    //    @Benchmark
    //    public Object deserializeByJson() {
    //        return jsonSerializer.deserialize(jsonBytes, type);
    //    }

    @Setup(Level.Trial)
    public void setup() throws Exception {
        ScopedPlatform platform = ScopedPlatform.defaultInstance();
        jsonSerializer = platform.namedExtension(Serializer.class, JacksonSerializer.NAME);

        List<ParentObject> objList = ParentObject.getObjList();
        TypeCapture<List<ParentObject>> typeCapture = new TypeCapture<>() {
        };
        type = typeCapture.type();
        ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
        jsonSerializer.serialize(objList, jsonOut);
    }
}

