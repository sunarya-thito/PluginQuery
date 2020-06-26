package septogeddon.pluginquery.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class InstanceBuffer {

	private ArrayList<Object> instances = new ArrayList<>();
	
	public InstanceBuffer() {}
	
	public InstanceBuffer(Object...objects) {
		instances.ensureCapacity(objects.length);
		for (Object o : objects) instances.add(o);
	}
	
	public InstanceBuffer(Collection<?> collection) {
		instances.addAll(collection);
	}
	
	public InstanceBuffer(byte[] byteArray) throws ClassNotFoundException, IOException {
		this(byteArray, ObjectInputStream::new);
	}
	
	public InstanceBuffer(byte[] byteArray, IOFunction<InputStream, ObjectInput> inputFactory) throws ClassNotFoundException, IOException {
		try (ObjectInput input = inputFactory.apply(new ByteArrayInputStream(byteArray))) {
			while (input.available() > 0) {
				instances.add(input.readObject());
			}
		}
	}
	
	@SuppressWarnings("all")
	public <T> T pullObject() {
		return (T)instances.remove(0);
	}
	
	public void pushObject(Object object) {
		instances.add(object);
	}
	
	public void finalize() {
		instances.clear();
	}
	
	public byte[] toByteArray() throws IOException {
		return toByteArray(ObjectOutputStream::new);
	}
	
	public byte[] toByteArray(IOFunction<OutputStream, ObjectOutput> outputFactory) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ObjectOutput out = outputFactory.apply(output);
		for (int i = 0; i < instances.size(); i++) {
			out.writeObject(instances.get(i));
		}
		out.close();
		return output.toByteArray();
	}
	
	public static interface IOFunction<A,B> {
		public B apply(A object) throws IOException;
	}
	
}
