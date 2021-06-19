package nl.andrewlalis.aos_core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ByteUtils {
	public static byte[] toBytes(int x) {
		return new byte[] {
			(byte) (x >> 24),
			(byte) (x >> 16),
			(byte) (x >> 8),
			(byte) x
		};
	}

	public static int intFromBytes(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) |
			((bytes[1] & 0xFF) << 16) |
			((bytes[2] & 0xFF) << 8 ) |
			((bytes[3] & 0xFF));
	}

	public static void write(int x, OutputStream os) throws IOException {
		os.write(toBytes(x));
	}

	public static int readInt(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		int n = is.read(bytes);
		if (n < bytes.length) throw new IOException("Could not read enough bytes to read an integer.");
		return intFromBytes(bytes);
	}

	public static void write(String s, OutputStream os) throws IOException {
		write(s.length(), os);
		os.write(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String readString(InputStream is) throws IOException {
		int length = readInt(is);
		byte[] strBytes = new byte[length];
		int n = is.read(strBytes);
		if (n != length) throw new IOException("Could not read enough bytes to read string.");
		return new String(strBytes, StandardCharsets.UTF_8);
	}
}
