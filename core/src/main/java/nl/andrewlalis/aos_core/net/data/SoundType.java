package nl.andrewlalis.aos_core.net.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Encoding of all server-initiated game sounds with a unique byte value, for
 * efficient transmission to clients.
 */
public enum SoundType {
	SHOT_SMG(0, "ak47shot1.wav"),
	SHOT_RIFLE(1, "m1garand-shot1.wav"),
	SHOT_SHOTGUN(2, "shotgun-shot1.wav"),
	SHOT_MACHINE_GUN_1(11, "machine_gun-shot1.wav"),
	SHOT_MACHINE_GUN_2(12, "machine_gun-shot2.wav"),
	RELOAD(3, "reload.wav"),
	CHAT(4, "chat.wav"),
	DEATH(5, "death.wav"),
	BULLET_IMPACT_1(6, "bullet_impact_1.wav"),
	BULLET_IMPACT_2(7, "bullet_impact_2.wav"),
	BULLET_IMPACT_3(8, "bullet_impact_3.wav"),
	BULLET_IMPACT_4(9, "bullet_impact_4.wav"),
	BULLET_IMPACT_5(10, "bullet_impact_5.wav"),
	FOOTSTEPS_1(13, "footsteps1.wav");

	private final byte code;
	private final String soundName;

	SoundType(int code, String soundName) {
		this.code = (byte) code;
		this.soundName = soundName;
	}

	public byte getCode() {
		return code;
	}

	public String getSoundName() {
		return soundName;
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
