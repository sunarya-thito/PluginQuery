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
	
	public int available() {
		return instances.size();
	}
	
	public InstanceBuffer(byte[] byteArray) throws ClassNotFoundException, IOException {
		this(byteArray, ObjectInputStream::new);
	}
	
	public InstanceBuffer(byte[] byteArray, IOFunction<InputStream, ObjectInput> inputFactory) throws ClassNotFoundException, IOException {
		try (ObjectInput input = inputFactory.apply(new ByteArrayInputStream(byteArray))) {
			int size = input.readInt();
			for (int i = 0; i < size; i++) {
				instances.add(input.readObject());
			}
		}
	}
	
	@SuppressWarnings("all")
	public <T> T pullObject() {
		return (T)instances.remove(0);
	}
	
	public void copyTo(InstanceBuffer buffer) {
		for (int i = 0; i < instances.size(); i++) {
			buffer.pushObject(instances.get(i));
		}
		instances.clear();
	}
	
	public void pushObject(Object object) {
		instances.add(object);
	}
	
	public void finalize() {
		instances.clear();
	}
	
	public synchronized byte[] toByteArray() throws IOException {
		return toByteArray(ObjectOutputStream::new);
	}
	
	public synchronized byte[] toByteArray(IOFunction<OutputStream, ObjectOutput> outputFactory) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ObjectOutput out = outputFactory.apply(output);
		out.writeInt(instances.size());
		for (int i = 0; i < instances.size(); i++) {
			try {
				out.writeObject(instances.get(i));
			} catch (Throwable t) {
				System.out.println("Failed to write object: "+i);
			}
		}
		out.close();
		return output.toByteArray();
	}
	
	public static interface IOFunction<A,B> {
		public B apply(A object) throws IOException;
	}
	
}
