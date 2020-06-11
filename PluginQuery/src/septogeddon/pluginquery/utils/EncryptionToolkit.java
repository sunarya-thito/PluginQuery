package septogeddon.pluginquery.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionToolkit {

	private Cipher encryptor;
	private Cipher decryptor;
	private Key key;
	public EncryptionToolkit(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		encryptor = Cipher.getInstance(key.getAlgorithm());
		decryptor = Cipher.getInstance(key.getAlgorithm());
		encryptor.init(Cipher.ENCRYPT_MODE, key);
		decryptor.init(Cipher.DECRYPT_MODE, key);
		this.key = key;
	}
	
	public Key getKey() {
		return key;
	}
	
	public byte[] encode() {
		DataBuffer buffer = new DataBuffer();
		String alg = key.getAlgorithm();
		buffer.write(alg.length());
		buffer.write(alg.getBytes());
		buffer.write(getKey().getEncoded());
		return buffer.toByteArray();
	}
	
	public Cipher getEncryptor() {
		return encryptor;
	}
	
	public Cipher getDecryptor() {
		return decryptor;
	}
	
	public static Key generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(256);
		SecretKey secret = keygen.generateKey();
		return secret;
	}
	
	public void writeKey(File file) throws IOException {
		try (FileOutputStream output = new FileOutputStream(file)) {
			String alg = key.getAlgorithm();
			// lets just assume that the algorithm name is less than the byte max value
			// wtf is an algorithm with 10000 length of string??
			output.write(alg.length());
			output.write(alg.getBytes());
			output.write(getKey().getEncoded());
		}
	}
	
	public static Key readKey(File file) throws IOException {
		byte[] key = QueryUtil.read(file);
		byte length = key[0];
		byte[] name = new byte[length];
		for (int i = 0; i < length; i++) {
			name[i] = key[i + 1];
		}
		key = Arrays.copyOfRange(key, length + 1, key.length);
		return new SecretKeySpec(key, new String(name));
	}
	
}
