package septogeddon.pluginquery.message;

import java.io.*;

public class QueryObject implements Serializable {
    public static <T extends QueryObject> T fromByteArraySafe(byte[] bytes) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException ignored) {
            return null;
        }
    }
    public static <T extends QueryObject> T fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) objectInputStream.readObject();
        }
    }
    public byte[] toByteArraySafe() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutput.writeObject(this);
        } catch (IOException ignored) {
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutput.writeObject(this);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
