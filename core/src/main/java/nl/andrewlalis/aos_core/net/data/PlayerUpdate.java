package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.GunType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerUpdate {
	public static final int BYTES = Integer.BYTES + 6 * Float.BYTES + 1;

	private final int id;
	private final Vec2 position;
	private final Vec2 orientation;
	private final Vec2 velocity;
	private final GunType gunType;

	public PlayerUpdate(Player player) {
		this.id = player.getId();
		this.position = player.getPosition();
		this.orientation = player.getOrientation();
		this.velocity = player.getVelocity();
		this.gunType = player.getGun().getType();
	}

	public PlayerUpdate(int id, Vec2 position, Vec2 orientation, Vec2 velocity, GunType gunType) {
		this.id = id;
		this.position = position;
		this.orientation = orientation;
		this.velocity = velocity;
		this.gunType = gunType;
	}

	public int getId() {
		return id;
	}

	public Vec2 getPosition() {
		return position;
	}

	public Vec2 getOrientation() {
		return orientation;
	}

	public Vec2 getVelocity() {
		return velocity;
	}

	public GunType getGunType() {
		return gunType;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.id);
		out.writeFloat(this.position.x());
		out.writeFloat(this.position.y());
		out.writeFloat(this.orientation.x());
		out.writeFloat(this.orientation.y());
		out.writeFloat(this.velocity.x());
		out.writeFloat(this.velocity.y());
		out.writeByte(this.gunType.getCode());
	}

	public static PlayerUpdate read(DataInputStream in) throws IOException {
		return new PlayerUpdate(
			in.readInt(),
			Vec2.read(in),
			Vec2.read(in),
			Vec2.read(in),
			GunType.get(in.readByte())
		);
	}
}
