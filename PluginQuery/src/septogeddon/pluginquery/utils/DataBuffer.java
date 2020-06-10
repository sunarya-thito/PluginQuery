package septogeddon.pluginquery.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.Arrays;

public class DataBuffer implements DataInput, DataOutput {

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	public final static String readUTF(DataBuffer in) {
		int utflen = in.readUnsignedShort();
		byte[] bytearr = new byte[utflen];
		char[] chararr = new char[utflen];
		int c, char2, char3;
		int count = 0;
		int chararr_count = 0;

		in.readFully(bytearr, 0, utflen);

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			if (c > 127)
				break;
			count++;
			chararr[chararr_count++] = (char) c;
		}

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				count++;
				chararr[chararr_count++] = (char) c;
				break;
			case 12:
			case 13:
				count += 2;
				if (count > utflen)
					throw new RuntimeException(new UTFDataFormatException("malformed input: partial character at end"));
				char2 = (int) bytearr[count - 1];
				if ((char2 & 0xC0) != 0x80)
					throw new RuntimeException(new UTFDataFormatException("malformed input around byte " + count));
				chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
				break;
			case 14:
				count += 3;
				if (count > utflen)
					throw new RuntimeException(new UTFDataFormatException("malformed input: partial character at end"));
				char2 = (int) bytearr[count - 2];
				char3 = (int) bytearr[count - 1];
				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
					throw new RuntimeException(
							new UTFDataFormatException("malformed input around byte " + (count - 1)));
				chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
				break;
			default:
				throw new RuntimeException(new UTFDataFormatException("malformed input around byte " + count));
			}
		}
		return new String(chararr, 0, chararr_count);
	}

	public static int writeUTF(String str, DataBuffer out) {
		int strlen = str.length();
		int utflen = 0;
		int c, count = 0;

		for (int i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}

		if (utflen > 65535)
			throw new RuntimeException(new UTFDataFormatException("encoded string too long: " + utflen + " bytes"));

		byte[] bytearr = new byte[utflen + 2];
		;

		bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
		bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

		int i = 0;
		for (i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if (!((c >= 0x0001) && (c <= 0x007F)))
				break;
			bytearr[count++] = (byte) c;
		}

		for (; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte) c;

			} else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			} else {
				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		out.write(bytearr);
		return utflen + 2;
	}

	protected byte[] buf;
	private byte readBuffer[] = new byte[8];
	
	private int count;

	private int pos;

	private char lineBuffer[];

	private byte writeBuffer[] = new byte[8];

	public DataBuffer() {
		buf = new byte[32];
	}
	
	public DataBuffer(byte[] buff) {
		buf = buff;
		pos = 0;
		count = buff.length;
	}
	
	public DataBuffer(InputStream input) throws IOException {
		int len;
		byte[] buffer = new byte[8 * 1024];
		while ((len = input.read(buffer, 0, buffer.length)) != -1) {
			write(buffer, 0, len);
		}
	}

	public DataBuffer(int initialSize) {
		buf = new byte[initialSize];
	}
	
	public synchronized void copyTo(OutputStream output) throws IOException {
		output.write(toByteArray());
	}

	public int available() {
		return count - pos;
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity - buf.length > 0)
			grow(minCapacity);
	}

	private void grow(int minCapacity) {
		count -= pos;
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		if (newCapacity - MAX_ARRAY_SIZE > 0)
			newCapacity = hugeCapacity(minCapacity);
		buf = Arrays.copyOfRange(buf, pos, newCapacity);
		pos = 0;
	}

	public synchronized int read() {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

	public synchronized int read(byte b[], int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		}

		if (pos >= count) {
			return -1;
		}

		int avail = count - pos;
		if (len > avail) {
			len = avail;
		}
		if (len <= 0) {
			return 0;
		}
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
		return len;
	}

	public int read(byte[] bytes) {
		return this.read(bytes, 0, bytes.length);
	}

	public final boolean readBoolean() {
		int ch = read();
		return (ch != 0);
	}

	public final byte readByte() {
		int ch = read();
		return (byte) (ch);
	}

	//

	public String readBytes() {
		int length = readInt();
		byte[] uf = new byte[length];
		read(uf);
		return new String(uf);
	}

	public final char readChar() {
		int ch1 = read();
		int ch2 = read();
		return (char) ((ch1 << 8) + (ch2 << 0));
	}

	public String readChars() {
		int length = readInt();
		char[] uf = new char[length];
		for (int i = 0; i < length; i++) {
			uf[i] = readChar();
		}
		return new String(uf);
	}

	public final double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	public final float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	public final void readFully(byte b[]) {
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			n += count;
		}
	}

	public final int readInt() {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	@Deprecated
	public final String readLine() {
		char buf[] = lineBuffer;

		if (buf == null) {
			buf = lineBuffer = new char[128];
		}

		int room = buf.length;
		int offset = 0;
		int c;

		loop: while (true) {
			switch (c = read()) {
			case -1:
			case '\n':
				break loop;

			case '\r':
				int c2 = read();
				if ((c2 != '\n') && (c2 != -1)) {
					throw new UnsupportedOperationException();
//                    if (!(in instanceof PushbackInputStream)) {
//                        this.in = new PushbackInputStream(in);
//                    }
//                    ((PushbackInputStream)in).unread(c2);
				}
				break loop;

			default:
				if (--room < 0) {
					buf = new char[offset + 128];
					room = buf.length - offset - 1;
					System.arraycopy(lineBuffer, 0, buf, 0, offset);
					lineBuffer = buf;
				}
				buf[offset++] = (char) c;
				break;
			}
		}
		if ((c == -1) && (offset == 0)) {
			return null;
		}
		return String.copyValueOf(buf, 0, offset);
	}

	public final long readLong() {
		readFully(readBuffer, 0, 8);
		return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48)
				+ ((long) (readBuffer[2] & 255) << 40) + ((long) (readBuffer[3] & 255) << 32)
				+ ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16) + ((readBuffer[6] & 255) << 8)
				+ ((readBuffer[7] & 255) << 0));
	}

	public final short readShort() {
		int ch1 = read();
		int ch2 = read();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	public final int readUnsignedByte() {
		int ch = read();
		return ch;
	}

	public final int readUnsignedShort() {
		int ch1 = read();
		int ch2 = read();
		return (ch1 << 8) + (ch2 << 0);
	}

	public final String readUTF() {
		return readUTF(this);
	}

	public long skip(long n) {
		return this.skipBytes((int) n);
	}

	public final int skipBytes(int n) {
		int diff = count - pos;
		pos = Math.min(pos + n, count);
		return n - diff;
	}

	public synchronized byte toByteArray()[] {
		return Arrays.copyOf(buf, count);
	}

	public synchronized void write(byte b[], int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
			throw new IndexOutOfBoundsException();
		}
		ensureCapacity(count + len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	public synchronized void write(int b) {
		ensureCapacity(count + 1);
		buf[count] = (byte) b;
		count += 1;
	}

	//
	public final void writeBoolean(boolean v) {
		write(v ? 1 : 0);
	}

	public final void writeByte(int v) {
		write(v);
	}

	public final void writeBytes(String s) {
		int len = s.length();
		writeInt(len);
		write(s.getBytes());
	}

	public final void writeChar(int v) {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);

	}

	public final void writeChars(String s) {
		int len = s.length();
		writeInt(len);
		for (int i = 0; i < len; i++) {
			int v = s.charAt(i);
			writeChar(v);
		}
	}

	public final void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	public final void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}

	public final void writeInt(int v) {
		write((v >>> 24) & 0xFF);
		write((v >>> 16) & 0xFF);
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);

	}

	public final void writeLong(long v) {
		writeBuffer[0] = (byte) (v >>> 56);
		writeBuffer[1] = (byte) (v >>> 48);
		writeBuffer[2] = (byte) (v >>> 40);
		writeBuffer[3] = (byte) (v >>> 32);
		writeBuffer[4] = (byte) (v >>> 24);
		writeBuffer[5] = (byte) (v >>> 16);
		writeBuffer[6] = (byte) (v >>> 8);
		writeBuffer[7] = (byte) (v >>> 0);
		write(writeBuffer, 0, 8);

	}

	public final void writeShort(int v) {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	public final void writeUTF(String str) {
		writeUTF(str, this);
	}

}
