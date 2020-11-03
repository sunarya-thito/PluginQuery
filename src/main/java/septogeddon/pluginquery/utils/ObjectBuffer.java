package septogeddon.pluginquery.utils;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectBuffer extends DataBuffer implements ObjectInput, ObjectOutput {

    final static public int NULL_TYPE = 0, INTEGER_TYPE = 1, LONG_TYPE = 2, DOUBLE_TYPE = 3, FLOAT_TYPE = 4,
            SHORT_TYPE = 5, CHAR_TYPE = 6, BYTE_TYPE = 7, BOOLEAN_TYPE = 8, ARRAY_TYPE = 9, OBJECT_TYPE = 10;
    private static sun.misc.Unsafe unsafe;

    static {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private final List<Object> hashed = new ArrayList<>();
    private final Map<Integer, Object> hashToObject = new HashMap<>();

    public ObjectBuffer() {
    }

    public ObjectBuffer(int initialSize) {
        super(initialSize);
    }

    public ObjectBuffer(InputStream input) throws IOException {
        super(input);
    }

    public ObjectBuffer(byte[] bytes) {
        super(bytes);
    }

    public void close() {
        this.flush();
    }

    private List<Field> collectFields(List<Field> fields, Class<?> cl) {
        if (cl != null) {
            for (Field f : cl.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
                    continue;
                fields.add(f);
            }
            collectFields(fields, cl.getSuperclass());
        }
        return fields;
    }

    public void flush() {
        hashed.clear();
        hashToObject.clear();
    }

    private String hash(Field f) {
        return f.getDeclaringClass().getName() + ":" + f.getName();
    }

    private Field hash(String s) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        String[] split = s.split(":", 2);
        if (split.length == 2) {
            Field field = Class.forName(split[0]).getDeclaredField(split[1]);
            field.setAccessible(true);
            return field;
        } else
            throw new IllegalStateException("invalid field hash");
    }

    @SuppressWarnings("unchecked")
    public <T> T readCasted() {
        return (T) readObject();
    }

    public Object readObject() {
        int type = readByte();
        if (type == NULL_TYPE)
            return null;
        if (type == INTEGER_TYPE)
            return readInt();
        if (type == LONG_TYPE)
            return readLong();
        if (type == DOUBLE_TYPE)
            return readDouble();
        if (type == FLOAT_TYPE)
            return readFloat();
        if (type == SHORT_TYPE)
            return readShort();
        if (type == CHAR_TYPE)
            return readChar();
        if (type == BYTE_TYPE)
            return readByte();
        if (type == BOOLEAN_TYPE)
            return readBoolean();
        if (type == ARRAY_TYPE) {
            try {
                Class<?> component = Class.forName(readBytes());
                int length = readInt();
                Object array = Array.newInstance(component.getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Object sub = readObject();
                    Array.set(array, i, sub);
                }
                return array;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        if (type == OBJECT_TYPE) {
            int hash = readInt();
            Object hashed = hashToObject.get(hash);
            if (hashed != null)
                return hashed;
            String className = readBytes();
            try {
                Class<?> cl = Class.forName(className);
                Object object = unsafe.allocateInstance(cl);
                hashToObject.put(hash, object);
                if (object instanceof Externalizable) {
                    ((Externalizable) object).readExternal(this);
                    return object;
                }
                int size = readInt();
                for (int i = 0; i < size; i++) {
                    Field field = hash(readBytes());
                    Object sub = readObject();
                    field.set(object, sub);
                }
                return object;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        throw new IllegalStateException("unknown type " + type);
    }

    public void writeObject(Object obj) {
        if (obj == null) {
            writeByte(NULL_TYPE);
            return;
        } else if (obj instanceof Integer) {
            writeByte(INTEGER_TYPE);
            writeInt((Integer) obj);
        } else if (obj instanceof Long) {
            writeByte(LONG_TYPE);
            writeLong((Long) obj);
        } else if (obj instanceof Double) {
            writeByte(DOUBLE_TYPE);
            writeDouble((Double) obj);
        } else if (obj instanceof Float) {
            writeByte(FLOAT_TYPE);
            writeFloat((Float) obj);
        } else if (obj instanceof Short) {
            writeByte(SHORT_TYPE);
            writeShort((Short) obj);
        } else if (obj instanceof Character) {
            writeByte(CHAR_TYPE);
            writeChar((Character) obj);
        } else if (obj instanceof Byte) {
            writeByte(BYTE_TYPE);
            writeByte((Byte) obj);
        } else if (obj.getClass().isArray()) {
            writeByte(ARRAY_TYPE);
            writeBytes(obj.getClass().getName());
            int length = Array.getLength(obj);
            writeInt(length);
            for (int i = 0; i < length; i++) {
                writeObject(Array.get(obj, i));
            }
        } else {
            writeByte(OBJECT_TYPE);
            int hash = Objects.hashCode(obj);
            writeInt(hash);
            if (!hashed.contains(obj)) {
                hashed.add(obj);
                Class<?> cl = obj.getClass();
                try {
                    if (obj instanceof Externalizable) {
                        ((Externalizable) obj).writeExternal(this);
                        return;
                    }
                    // no longer supports Serializable!
//					Method writer = find(obj.getClass(), "writeObject", ObjectOutputStream.class);
//					if (writer != null) {
//						writer.setAccessible(true);
//						writer.invoke(obj, this.writeObjectStream());
//						return;
//					}
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
                List<Field> fields = collectFields(new ArrayList<>(), cl);
                writeBytes(obj.getClass().getName());
                writeInt(fields.size());
                for (Field f : fields) {
                    f.setAccessible(true);
                    writeBytes(hash(f));
                    try {
                        Object o = f.get(obj);
                        writeObject(o);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }

}
