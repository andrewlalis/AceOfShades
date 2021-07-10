package nl.andrewlalis.aos_core.net.data.tool;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class ToolData {
	private static final Map<Byte, Class<? extends ToolData>> dataMapping = new HashMap<>();
	static {
		dataMapping.put((byte) 0, GunData.class);
		dataMapping.put((byte) 1, GrenadeData.class);
	}

	private final byte toolType;

	public ToolData(byte toolType) {
		this.toolType = toolType;
	}

	public void write(ByteBuffer buffer) {
		buffer.put(this.toolType);
		this.putData(buffer);
	}

	public static ToolData read(ByteBuffer buffer) {
		byte type = buffer.get();
		var dataClass = dataMapping.get(type);
		if (dataClass == null) {
			System.err.println("Invalid tool data type byte.");
			return null;
		}
		try {
			var data = dataClass.getConstructor().newInstance();
			data.getData(buffer);
			return data;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract int getByteSize();

	/**
	 * Writes data for this tool data object into the given buffer.
	 * @param buffer The byte buffer to write data into.
	 */
	protected abstract void putData(ByteBuffer buffer);

	/**
	 * Reads data for this tool data object from the given buffer.
	 * @param buffer The byte buffer to read data from.
	 */
	protected abstract void getData(ByteBuffer buffer);
}
