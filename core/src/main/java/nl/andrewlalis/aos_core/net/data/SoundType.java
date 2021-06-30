package nl.andrewlalis.aos_core.net.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Encoding of all server-initiated game sounds with a unique byte value, for
 * efficient transmission to clients.
 */
public enum SoundType {
	SHOT_SMG(0, "ak47shot1.wav", 25),
	SHOT_RIFLE(1, "m1garand-shot1.wav", 25),
	SHOT_SHOTGUN(2, "shotgun-shot1.wav", 25),
	RELOAD(3, "reload.wav", 10),
	CHAT(4, "chat.wav", 5),
	DEATH(5, "death.wav", 5),
	BULLET_IMPACT_1(6, "bullet_impact_1.wav", 10),
	BULLET_IMPACT_2(7, "bullet_impact_2.wav", 10),
	BULLET_IMPACT_3(8, "bullet_impact_3.wav", 10),
	BULLET_IMPACT_4(9, "bullet_impact_4.wav", 10),
	BULLET_IMPACT_5(10, "bullet_impact_5.wav", 10);

	private final byte code;
	private final String soundName;
	private final int clipBufferCount;

	SoundType(int code, String soundName, int clipBufferCount) {
		this.code = (byte) code;
		this.soundName = soundName;
		this.clipBufferCount = clipBufferCount;
	}

	public byte getCode() {
		return code;
	}

	public String getSoundName() {
		return soundName;
	}

	public int getClipBufferCount() {
		return clipBufferCount;
	}

	private static final Map<Byte, SoundType> typeIndex = new HashMap<>();
	static {
		for (var val : values()) {
			typeIndex.put(val.getCode(), val);
		}
	}

	public static SoundType get(byte code) {
		return typeIndex.get(code);
	}
}
