package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SoundData {
	public static final int BYTES = 3 * Float.BYTES + 1;

	private final Vec2 position;
	private final float volume;
	private final SoundType type;

	public SoundData(Vec2 position, float volume, SoundType type) {
		this.position = position;
		this.volume = volume;
		this.type = type;
	}

	public Vec2 getPosition() {
		return position;
	}

	public float getVolume() {
		return volume;
	}

	public SoundType getType() {
		return this.type;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(this.position.x());
		out.writeFloat(this.position.y());
		out.writeFloat(this.volume);
		out.writeByte(this.type.getCode());
	}

	public static SoundData read(DataInputStream in) throws IOException {
		return new SoundData(
			Vec2.read(in),
			in.readFloat(),
			SoundType.get(in.readByte())
		);
	}
}
