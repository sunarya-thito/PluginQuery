package septogeddon.pluginquery.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QueryUtil {

	/***
	 * If the object is null, throw a {@link java.lang.NullPointerException}
	 * @param o
	 * @param name
	 */
	public static void nonNull(Object o, String name) {
		if (o == null) throw new NullPointerException(name);
	}
	
	/***
	 * If the condition is true, throw {@link java.lang.IllegalStateException}
	 * @param condition
	 * @param cause
	 */
	public static void illegalState(boolean condition, String cause) {
		if (condition) throw new IllegalStateException(cause);
	}
	
	/***
	 * If the condition is true, throw {@link java.lang.IllegalArgumentException}
	 * @param condition
	 * @param cause
	 */
	public static void illegalArgument(boolean condition, String cause) {
		if (condition) throw new IllegalArgumentException(cause);
	}

	/***
	 * Sneakily throw a Throwable without require your method to have "throws" keyword
	 * @param <T>
	 * @param t
	 * @return
	 * @throws T
	 */
	@SuppressWarnings("all")
	public static <T extends Throwable> T Throw(Throwable t) throws T {
		throw (T)t;
	}
	
	/***
	 * Read the input stream to memory. Will close the input.
	 * @param input
	 * @return
	 * @throws IOException
	 */
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
	
	/***
	 * Read the file to memory
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] read(File file) throws IOException {
		return read(new FileInputStream(file));
	}
	
}
