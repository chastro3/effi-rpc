package demo.provider;

import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.compression.Compressor;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.annotation.spi.Extension;

import java.lang.reflect.Type;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/12 14:04
 */
@Extension(value = "test", interfaces = Serializer.class)
public class MyTest implements Serializer, Compressor {
    @Override
    public byte[] serialize(Object input) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Type type) {
        return null;
    }

    @Override
    public byte[] compress(byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] decompress(byte[] compressedData) {
        return new byte[0];
    }

    public static void main(String[] args) {
        DynamicAccessor dynamicAccessor = DynamicAccessor.get(EmptyClass.class);
        System.out.println(dynamicAccessor.getMethodIndex("hhh"));
    }
}
