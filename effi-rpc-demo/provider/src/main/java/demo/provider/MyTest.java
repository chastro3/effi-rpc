package demo.provider;

import io.effi.rpc.common.compile.DynamicAccessor;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.compress.Compressor;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/12 14:04
 */
@Extension(interfaces = Serializer.class)
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
    public byte[] compress(byte[] data) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] decompress(byte[] compressedData) throws IOException {
        return new byte[0];
    }

    public static void main(String[] args) {
        DynamicAccessor dynamicAccessor = DynamicAccessor.get(EmptyClass.class);
        System.out.println(dynamicAccessor.getMethodIndex("hhh"));
    }
}
