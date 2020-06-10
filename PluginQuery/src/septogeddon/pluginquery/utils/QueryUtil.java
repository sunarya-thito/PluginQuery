package septogeddon.pluginquery.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QueryUtil {

	public static void nonNull(Object o, String name) {
		if (o == null) throw new NullPointerException(name);
	}
	
	public static void illegalState(boolean condition, String cause) {
		if (condition) throw new IllegalStateException(cause);
	}
	
	public static void illegalArgument(boolean condition, String cause) {
		if (condition) throw new IllegalArgumentException(cause);
	}

	@SuppressWarnings("all")
	public static <T extends Throwable> T Throw(Throwable t) throws T {
		throw (T)t;
	}
	
	public static byte[] read(InputStream input) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len;
		byte[] buffer = new byte[1024 * 8];
		while ((len = input.read(buffer, 0, buffer.length)) != -1) {
			out.write(buffer, 0, len);
		}
		input.close();
		return out.toByteArray();
	}
	
	public static byte[] read(File file) throws IOException {
		return read(new FileInputStream(file));
	}
	
}
