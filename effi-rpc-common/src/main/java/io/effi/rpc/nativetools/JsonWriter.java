package io.effi.rpc.nativetools;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * High-performance JSON writer with optional formatting output.
 */
public class JsonWriter implements Closeable {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private final Writer out;

    private final String indentUnit;

    private final boolean pretty;

    private int level = 0;

    public JsonWriter(Writer writer, String indentUnit, boolean pretty) {
        this.out = writer;
        this.indentUnit = indentUnit;
        this.pretty = pretty;
    }

    public JsonWriter(Writer writer) {
        this(writer, "  ", true);
    }

    public JsonWriter write(Object obj) throws IOException {
        if (obj == null) {
            out.write("null");
        } else if (obj instanceof Map) {
            writeObject((Map<?, ?>) obj);
        } else if (obj instanceof List) {
            writeArray((List<?>) obj);
        } else if (obj instanceof String) {
            writeString((String) obj);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            out.write(obj.toString());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + obj.getClass() + " is not allowed in JSON output");
        }
        return this;
    }

    public void writeObject(Map<?, ?> map) throws IOException {
        out.write('{');
        if (!map.isEmpty()) {
            level++;
            int count = 0, size = map.size();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                count++;
                newlineAndIndent();
                writeString(e.getKey().toString());
                out.write(':');
                if (pretty) out.write(' ');
                write(e.getValue());
                if (count < size) out.write(',');
            }
            level--;
            newlineAndIndent();
        }
        out.write('}');
    }

    public void writeArray(List<?> list) throws IOException {
        out.write('[');
        if (!list.isEmpty()) {
            level++;
            int size = list.size();
            for (int i = 0; i < size; i++) {
                newlineAndIndent();
                write(list.get(i));
                if (i < size - 1) out.write(',');
            }
            level--;
            newlineAndIndent();
        }
        out.write(']');
    }

    private void writeString(String s) throws IOException {
        out.write('"');
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    out.write("\\\"");
                    break;
                case '\\':
                    out.write("\\\\");
                    break;
                case '\b':
                    out.write("\\b");
                    break;
                case '\f':
                    out.write("\\f");
                    break;
                case '\n':
                    out.write("\\n");
                    break;
                case '\r':
                    out.write("\\r");
                    break;
                case '\t':
                    out.write("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.write('\\');
                        out.write('u');
                        out.write('0');
                        out.write('0');
                        out.write(HEX_CHARS[(c >> 4) & 0xF]);
                        out.write(HEX_CHARS[c & 0xF]);
                    } else {
                        out.write(c);
                    }
            }
        }
        out.write('"');
    }

    private void newlineAndIndent() throws IOException {
        if (pretty) {
            out.write(System.lineSeparator());
            for (int i = 0; i < level; i++) {
                out.write(indentUnit);
            }
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
