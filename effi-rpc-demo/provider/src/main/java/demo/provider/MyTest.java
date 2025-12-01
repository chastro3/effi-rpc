package demo.provider;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.compression.Compressor;
import io.effi.rpc.serialization.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * @Author WenBo Zhou
 * @Date 2025/4/12 14:04
 */
@Extension(value = "test", interfaces = Serializer.class)
public class MyTest implements Serializer, Compressor {

    public static void main(String[] args) {
        DynamicAccessor dynamicAccessor = DynamicAccessor.fetch(EmptyClass.class);
        System.out.println(dynamicAccessor.findMethodIndex("hhh"));
    }

    @Override
    public void serialize(Object obj, OutputStream out) {

    }

    @Override
    public <T> T deserialize(InputStream in, Type type) {
        return null;
    }

    @Override
    public void compress(OutputStream out, byte[] data) throws IOException {

    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return null;
    }
}
