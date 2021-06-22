package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Bullet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BulletUpdate {
	public static final int BYTES = 4 * Float.BYTES;

	private final Vec2 position;
	private final Vec2 velocity;
	public BulletUpdate(Bullet bullet) {
		this.position = bullet.getPosition();
		this.velocity = bullet.getVelocity();
	}

	private BulletUpdate(Vec2 position, Vec2 velocity) {
		this.position = position;
		this.velocity = velocity;
	}

	public Vec2 getPosition() {
		return position;
	}

	public Vec2 getVelocity() {
		return velocity;
	}

	public Bullet toBullet() {
		return new Bullet(this.position, this.velocity);
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(this.position.x());
		out.writeFloat(this.position.y());
		out.writeFloat(this.velocity.x());
		out.writeFloat(this.velocity.y());
	}

	public static BulletUpdate read(DataInputStream in) throws IOException {
		return new BulletUpdate(
			Vec2.read(in),
			Vec2.read(in)
		);
	}
}
