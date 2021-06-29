package nl.andrewlalis.aos_core.model.tools;

public enum GunCategory {
	SHOTGUN(0),
	SMG(1),
	RIFLE(2);

	private final byte code;

	GunCategory(int code) {
		this.code = (byte) code;
	}

	public byte getCode() {
		return code;
	}

	public static GunCategory get(byte code) {
		for (var val : values()) {
			if (val.code == code) return val;
		}
		return null;
	}
}
